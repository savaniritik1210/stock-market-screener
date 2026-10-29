import { networkInterfaces } from 'node:os';
import { isIPv4 } from 'node:net';
import { pathToFileURL } from 'node:url';
import { emitKeypressEvents } from 'node:readline';
import WebSocket from 'ws';

const broker = 'https://apiconnect.angelone.in';
const streamUrl = 'wss://smartapisocket.angelone.in/smart-stream';

class CheckError extends Error {}

export function readSecret(label, input = process.stdin, output = process.stdout) {
  if (!input.isTTY || !output.isTTY) throw new CheckError('Private prompts require a local interactive terminal.');
  return new Promise((resolve, reject) => {
    let value = '';
    const wasRaw = Boolean(input.isRaw);
    const wasPaused = input.isPaused();
    emitKeypressEvents(input);
    const finish = (error) => {
      input.removeListener('keypress', onKey);
      input.setRawMode(wasRaw);
      if (wasPaused) input.pause();
      output.write('\n');
      if (error) reject(error);
      else resolve(value);
      value = '';
    };
    const onKey = (text, key = {}) => {
      if (key.ctrl && (key.name === 'c' || key.name === 'd')) {
        finish(new CheckError('Credential entry cancelled.'));
      } else if (key.name === 'return' || key.name === 'enter') {
        finish();
      } else if (key.name === 'backspace') {
        if (value) { value = value.slice(0, -1); output.write('\b \b'); }
      } else if (!key.ctrl && !key.meta && text && /^[A-Za-z0-9]+$/.test(text)) {
        if (value.length + text.length > 128) {
          finish(new CheckError('Credential exceeds the allowed length.'));
        } else {
          value += text;
          output.write('*'.repeat(text.length));
        }
      } else if (text) {
        finish(new CheckError('Unsupported credential character. Use letters and digits only.'));
      }
    };
    input.on('keypress', onKey);
    input.setRawMode(true);
    input.resume();
    output.write(`${label}: `);
  });
}

function assertPrivateRuntime() {
  if (process.execArgv.some((arg) => /^--(?:inspect|debug|tls-keylog)/.test(arg))
      || ['NODE_OPTIONS', 'NODE_DEBUG', 'NODE_DEBUG_NATIVE', 'SSLKEYLOGFILE']
        .some((name) => Boolean(process.env[name]))) {
    throw new CheckError('Run the checker without Node debugging, NODE_OPTIONS or TLS key logging enabled.');
  }
  if (process.env.NODE_TLS_REJECT_UNAUTHORIZED === '0') {
    throw new CheckError('TLS verification must not be disabled.');
  }
}

async function promptCredentials() {
  let publicIp;
  try {
    const response = await fetch('https://api4.ipify.org', {
      redirect: 'error', signal: AbortSignal.timeout(15000)
    });
    if (!response.ok) throw new CheckError('Public IPv4 lookup failed.');
    publicIp = (await response.text()).trim();
  } catch {
    throw new CheckError('Public IPv4 lookup failed. Check your network before signing in.');
  }
  if (!isIPv4(publicIp)) throw new CheckError('Public IPv4 lookup returned an invalid address.');
  console.log('Local read-only check. No browser, tunnel, saved credentials or order placement.');
  console.log('Use a regenerated key. The temporary SmartAPI session is logged out after the check.');
  console.log(`Outbound public IPv4: ${publicIp} (static-IP status is not verified)`);
  return {
    apiKey: await readSecret('Angel One API key (hidden)'),
    clientCode: await readSecret('Angel One client code (hidden)'),
    pin: await readSecret('Angel One PIN (hidden)'),
    totp: await readSecret('Current six-digit TOTP (hidden)'),
    publicIp
  };
}

export function safeMessage(error) {
  return error instanceof CheckError ? error.message
    : 'Connection check failed. Details withheld to avoid exposing credentials.';
}

export function validateCredentials(credentials) {
  if (!credentials || typeof credentials !== 'object') throw new CheckError('Credentials are required.');
  for (const field of ['apiKey', 'clientCode', 'pin', 'totp']) {
    if (typeof credentials[field] !== 'string' || !/^[A-Za-z0-9]{1,128}$/.test(credentials[field])) {
      throw new CheckError(`Missing or invalid ${field}. Enter it in the private local prompt.`);
    }
  }
  if (!/^\d{6}$/.test(credentials.totp)) throw new CheckError('TOTP must contain six digits.');
  if (!isIPv4(credentials.publicIp || '')) throw new CheckError('A valid outbound public IPv4 is required.');
}

export function parseTick(data, now = Date.now()) {
  const packet = Buffer.from(data);
  if (packet.length < 51 || packet[0] !== 1 || packet[1] !== 1) {
    throw new CheckError('Unexpected SmartAPI LTP packet.');
  }
  const token = packet.subarray(2, 27).toString('ascii').replace(/\0.*$/, '');
  if (token !== '3045') throw new CheckError('Unexpected instrument in SmartAPI tick.');
  const timestamp = Number(packet.readBigInt64LE(35));
  const price = Number(packet.readBigInt64LE(43)) / 100;
  if (!Number.isSafeInteger(timestamp) || timestamp <= 0 || timestamp > 8640000000000000
      || !Number.isFinite(price) || price <= 0) {
    throw new CheckError('Invalid price or timestamp in SmartAPI tick.');
  }
  return {
    symbol: 'SBIN-EQ',
    ltp: price,
    exchangeTime: new Date(timestamp).toISOString(),
    fresh: now - timestamp >= -5000 && now - timestamp <= 60000
  };
}

export function checkStream(credentials, session, { Socket = WebSocket, durationMs = 20000 } = {}) {
  return new Promise((resolve) => {
    const result = { connected: false, ticksReceived: 0, freshTicksReceived: 0 };
    const socket = new Socket(streamUrl, {
      headers: {
        Authorization: session.jwtToken,
        'x-api-key': credentials.apiKey,
        'x-client-code': credentials.clientCode,
        'x-feed-token': session.feedToken
      },
      followRedirects: false,
      rejectUnauthorized: true,
      handshakeTimeout: 10000,
      maxPayload: 65536
    });
    let finished = false;
    let heartbeat;
    const finish = () => {
      if (finished) return;
      finished = true;
      clearTimeout(timeout);
      clearInterval(heartbeat);
      socket.terminate();
      resolve(result);
    };
    const timeout = setTimeout(finish, durationMs);
    socket.on('open', () => {
      if (finished) return;
      result.connected = true;
      socket.send(JSON.stringify({
        correlationID: 'localcheck',
        action: 1,
        params: { mode: 1, tokenList: [{ exchangeType: 1, tokens: ['3045'] }] }
      }));
      heartbeat = setInterval(() => {
        if (socket.readyState === WebSocket.OPEN) socket.send('ping');
      }, 10000);
    });
    socket.on('message', (data, isBinary) => {
      if (finished || (!isBinary && data.toString() === 'pong')) return;
      try {
        if (!isBinary) throw new CheckError('SmartAPI did not return a binary tick; subscription was not verified.');
        const tick = parseTick(data);
        result.ticksReceived++;
        if (tick.fresh) result.freshTicksReceived++;
        result.lastTick = tick;
      } catch (error) {
        result.error = safeMessage(error);
        finish();
      }
    });
    socket.on('error', () => {
      if (finished) return;
      result.error = 'SmartAPI WebSocket failed. Check session, API permissions and IP restrictions.';
      finish();
    });
    socket.on('close', () => {
      if (!finished) result.error = 'SmartAPI closed the connection before the check completed.';
      finish();
    });
  });
}

export async function runCheck(credentials, {
  fetchImpl = fetch,
  streamCheck = checkStream,
  network = Object.values(networkInterfaces()).flat()
    .find((entry) => entry && entry.family === 'IPv4' && !entry.internal)
} = {}) {
  validateCredentials(credentials);
  if (process.env.NODE_TLS_REJECT_UNAUTHORIZED === '0') {
    throw new CheckError('TLS verification must not be disabled.');
  }
  if (!network) throw new CheckError('No external IPv4 network interface is available.');
  let session;
  const result = { status: 'failed', authenticated: false, liveDataVerified: false };

  async function request(path, body, authenticated = true) {
    let response;
    try {
      response = await fetchImpl(broker + path, {
        method: 'POST',
        redirect: 'error',
        headers: {
          'Content-Type': 'application/json',
          Accept: 'application/json',
          ...(authenticated ? { Authorization: `Bearer ${session.jwtToken.replace(/^Bearer\s+/i, '')}` } : {}),
          'X-PrivateKey': credentials.apiKey,
          'X-UserType': 'USER',
          'X-SourceID': 'WEB',
          'X-ClientLocalIP': network.address,
          'X-ClientPublicIP': credentials.publicIp,
          'X-MACAddress': network.mac
        },
        body: JSON.stringify(body),
        signal: AbortSignal.timeout(15000)
      });
    } catch {
      throw new CheckError('SmartAPI request failed, redirected or timed out. Check network connectivity.');
    }
    if (!response.ok) throw new CheckError(`SmartAPI returned HTTP ${response.status}.`);
    let payload;
    try {
      payload = await response.json();
    } catch {
      throw new CheckError('SmartAPI returned an invalid JSON response.');
    }
    if (payload?.status !== true) {
      throw new CheckError('SmartAPI rejected the request. Check credentials, API permissions and registered IP.');
    }
    return payload.data;
  }

  try {
    session = await request('/rest/auth/angelbroking/user/v1/loginByPassword', {
      clientcode: credentials.clientCode, password: credentials.pin, totp: credentials.totp
    }, false);
    for (const field of ['jwtToken', 'feedToken']) {
      if (typeof session?.[field] !== 'string' || !session[field] || session[field].length > 16384
          || /[\r\n]/.test(session[field])) {
        throw new CheckError('SmartAPI returned an invalid session.');
      }
      if (field === 'jwtToken') result.authenticated = true;
    }
    const quotes = await request('/rest/secure/angelbroking/market/v1/quote',
      { mode: 'FULL', exchangeTokens: { NSE: ['3045'] } });
    const quote = quotes.fetched?.find((item) => item.symbolToken === '3045' && item.exchange === 'NSE');
    if (!quote || typeof quote.ltp !== 'number' || !Number.isFinite(quote.ltp) || quote.ltp <= 0) {
      throw new CheckError('SmartAPI did not return a valid SBIN quote.');
    }
    result.quote = { symbol: 'SBIN-EQ', ltp: quote.ltp };
    result.stream = await streamCheck(credentials, session);
    result.liveDataVerified = result.stream.freshTicksReceived > 0;
    result.status = result.stream.error ? 'partial-failure' : 'checked';
    result.note = 'A quote or connected socket alone does not prove live data. Outside market hours, fresh ticks may not arrive.';
  } catch (error) {
    result.error = safeMessage(error);
  } finally {
    if (result.authenticated) {
      try {
        await request('/rest/secure/angelbroking/user/v1/logout', { clientcode: credentials.clientCode });
        result.loggedOut = true;
      } catch (error) {
        result.loggedOut = false;
        result.logoutError = safeMessage(error);
        result.status = 'partial-failure';
      }
    }
    session = undefined;
  }
  return result;
}

async function main() {
  let input = '';
  let credentials;
  try {
    assertPrivateRuntime();
    if (process.stdin.isTTY) {
      credentials = await promptCredentials();
    } else {
      for await (const chunk of process.stdin) {
        input += chunk;
        if (input.length > 4096) throw new CheckError('Credential input exceeded the allowed size.');
      }
      try {
        credentials = JSON.parse(input);
      } catch {
        throw new CheckError('Run the checker in a local terminal to enter credentials privately.');
      }
    }
    input = '';
    const result = await runCheck(credentials);
    console.log(JSON.stringify(result, null, 2));
    if (result.status !== 'checked') process.exitCode = 1;
  } catch (error) {
    console.error(JSON.stringify({ status: 'failed', error: safeMessage(error) }));
    process.exitCode = 1;
  } finally {
    input = '';
    if (credentials && typeof credentials === 'object') {
      for (const field of Object.keys(credentials)) delete credentials[field];
    }
  }
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) await main();
