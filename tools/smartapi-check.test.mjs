import { test } from 'node:test';
import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';
import { EventEmitter } from 'node:events';
import { spawnSync } from 'node:child_process';
import { PassThrough, Writable } from 'node:stream';
import { fileURLToPath } from 'node:url';
import { parseTick, validateCredentials, safeMessage, checkStream, runCheck, readSecret } from './smartapi-check.mjs';

const credentials = { apiKey: 'TestKeyOnly123', clientCode: 'TEST123', pin: '1234', totp: '123456', publicIp: '192.0.2.1' };
const session = { jwtToken: 'test.jwt.token', feedToken: 'testFeedToken' };
const network = { address: '192.0.2.2', mac: '00:11:22:33:44:55' };
const success = (data) => ({ ok: true, json: async () => ({ status: true, data }) });
const quote = { fetched: [{ exchange: 'NSE', symbolToken: '3045', ltp: 825.5 }] };
const secrets = [...Object.values(credentials).slice(0, 4), ...Object.values(session)];
function assertNoSecrets(value) {
  for (const secret of secrets) assert.equal(JSON.stringify(value).includes(secret), false);
}
function packetAt(timestamp) {
  const packet = Buffer.alloc(51);
  packet[0] = 1;
  packet[1] = 1;
  packet.write('3045', 2);
  packet.writeBigInt64LE(BigInt(timestamp), 35);
  packet.writeBigInt64LE(82550n, 43);
  return packet;
}

test('credentials are validated without echoing their values', () => {
  assert.doesNotThrow(() => validateCredentials(credentials));
  assert.throws(() => validateCredentials({ ...credentials, apiKey: 'bad\r\nheader' }), /invalid apiKey/);
  assert.throws(() => validateCredentials({ ...credentials, totp: '123' }), /six digits/);
  assert.throws(() => validateCredentials({ ...credentials, publicIp: 'invalid' }), /IPv4/);
  assert.throws(() => validateCredentials(null), /required/);
  assertNoSecrets(safeMessage(new Error(secrets.join(' '))));
});

test('interactive prompt masks input and restores terminal mode', async () => {
  const input = new PassThrough();
  input.isTTY = true;
  input.isRaw = false;
  input.setRawMode = (mode) => { input.isRaw = mode; };
  let written = '';
  const output = new Writable({ write(chunk, encoding, done) { written += chunk; done(); } });
  output.isTTY = true;
  const pending = readSecret('API key', input, output);
  input.emit('keypress', credentials.apiKey, {});
  input.emit('keypress', '', { name: 'return' });
  assert.equal(await pending, credentials.apiKey);
  assert.equal(input.isRaw, false);
  assert.equal(input.listenerCount('keypress'), 0);
  assert.equal(written.includes(credentials.apiKey), false);
  assert.equal(written.includes('*'.repeat(credentials.apiKey.length)), true);
  input.destroy();
});

test('cancelled private input restores terminal state without exposing typed characters', async () => {
  const input = new PassThrough();
  input.isTTY = true;
  input.setRawMode = (mode) => { input.isRaw = mode; };
  let written = '';
  const output = new Writable({ write(chunk, encoding, done) { written += chunk; done(); } });
  output.isTTY = true;
  const pending = readSecret('API key', input, output);
  input.emit('keypress', credentials.apiKey, {});
  input.emit('keypress', '', { ctrl: true, name: 'c' });
  await assert.rejects(pending, /cancelled/);
  assert.equal(input.isRaw, false);
  assert.equal(written.includes(credentials.apiKey), false);
  input.destroy();
});

test('LTP parser checks token, timestamp, paise conversion and exact freshness boundary', () => {
  const now = 1800000000000;
  const packet = packetAt(now - 60000);
  assert.equal(parseTick(packet, now).ltp, 825.5);
  assert.equal(parseTick(packet, now).fresh, true);
  assert.equal(parseTick(packet, now + 1).fresh, false);
  assert.equal(parseTick(packet, now - 65001).fresh, false);
  assert.throws(() => parseTick(Buffer.alloc(10), now), /Unexpected/);
  packet.write('9999', 2);
  assert.throws(() => parseTick(packet, now), /instrument/);
  const outOfRange = packetAt(8640000000000001);
  assert.throws(() => parseTick(outOfRange, now), /timestamp/);
});

test('WebSocket credentials use headers only, enforce TLS and disable redirects', async () => {
  let connection;
  class Socket extends EventEmitter {
    constructor(url, options) {
      super();
      connection = { url, options };
      queueMicrotask(() => {
        this.emit('open');
        this.emit('message', packetAt(Date.now()), true);
      });
    }
    send(message) { assertNoSecrets(message); }
    terminate() { this.emit('close'); }
  }
  const result = await checkStream(credentials, session, { Socket, durationMs: 20 });
  assert.equal(connection.url, 'wss://smartapisocket.angelone.in/smart-stream');
  assert.equal(connection.options.headers['x-api-key'], credentials.apiKey);
  assert.equal(connection.options.headers.Authorization, session.jwtToken);
  assert.equal(connection.options.headers['x-feed-token'], session.feedToken);
  assert.equal(connection.options.rejectUnauthorized, true);
  assert.equal(connection.options.followRedirects, false);
  assert.equal(result.freshTicksReceived, 1);
  assertNoSecrets(result);
});

test('WebSocket errors do not expose raw library messages', async () => {
  class Socket extends EventEmitter {
    constructor() {
      super();
      queueMicrotask(() => this.emit('error', new Error(secrets.join(' '))));
    }
    terminate() { this.emit('close'); }
  }
  const result = await checkStream(credentials, session, { Socket, durationMs: 20 });
  assert.match(result.error, /WebSocket failed/);
  assertNoSecrets(result);
});

test('login, quote and logout use fixed HTTPS endpoints without credential URLs', async () => {
  const calls = [];
  const result = await runCheck(credentials, {
    network,
    fetchImpl: async (url, options) => {
      calls.push({ url, options });
      if (url.endsWith('loginByPassword')) return success(session);
      if (url.endsWith('/quote')) return success(quote);
      if (url.endsWith('/logout')) return success(null);
      assert.fail('Unexpected broker request');
    },
    streamCheck: async () => ({ connected: true, ticksReceived: 0, freshTicksReceived: 0 })
  });
  assert.equal(result.status, 'checked');
  assert.equal(result.authenticated, true);
  assert.equal(result.loggedOut, true);
  assert.equal(result.liveDataVerified, false);
  assert.equal(result.quote.ltp, 825.5);
  assert.equal(calls.length, 3);
  for (const { url, options } of calls) {
    assert.equal(new URL(url).origin, 'https://apiconnect.angelone.in');
    assert.equal(new URL(url).search, '');
    assertNoSecrets(url);
    assert.equal(options.redirect, 'error');
    assert.equal(options.headers['X-PrivateKey'], credentials.apiKey);
  }
  assert.equal(calls[0].options.headers.Authorization, undefined);
  assert.equal(JSON.parse(calls[0].options.body).password, credentials.pin);
  assert.equal(calls[1].options.headers.Authorization, `Bearer ${session.jwtToken}`);
  assertNoSecrets(result);
});

test('broker response errors are sanitized and authenticated sessions are logged out', async () => {
  let loggedOut = false;
  const result = await runCheck(credentials, {
    network,
    fetchImpl: async (url) => {
      if (url.endsWith('loginByPassword')) return success(session);
      if (url.endsWith('/logout')) { loggedOut = true; return success(null); }
      return { ok: true, json: async () => ({ status: false, message: secrets.join(' '), errorcode: credentials.apiKey }) };
    }
  });
  assert.equal(result.status, 'failed');
  assert.equal(loggedOut, true);
  assert.equal(result.loggedOut, true);
  assertNoSecrets(result);
});

test('logout failure is reported rather than silently claiming cleanup', async () => {
  const result = await runCheck(credentials, {
    network,
    fetchImpl: async (url) => {
      if (url.endsWith('loginByPassword')) return success(session);
      if (url.endsWith('/quote')) return success(quote);
      throw new Error(secrets.join(' '));
    },
    streamCheck: async () => ({ connected: true, ticksReceived: 0, freshTicksReceived: 0 })
  });
  assert.equal(result.status, 'partial-failure');
  assert.equal(result.loggedOut, false);
  assert.match(result.logoutError, /failed/);
  assertNoSecrets(result);
});

test('a malformed feed token still triggers logout when the JWT is valid', async () => {
  let loggedOut = false;
  const result = await runCheck(credentials, {
    network,
    fetchImpl: async (url) => {
      if (url.endsWith('loginByPassword')) return success({ ...session, feedToken: '' });
      if (url.endsWith('/logout')) { loggedOut = true; return success(null); }
      assert.fail('Unexpected broker request');
    }
  });
  assert.equal(loggedOut, true);
  assert.equal(result.status, 'failed');
});

test('public frontend no longer contains the login callback bridge', () => {
  const index = readFileSync(new URL('../frontend/src/index.html', import.meta.url), 'utf8');
  assert.equal(index.includes('smartapi-callback'), false);
  assert.equal(existsSync(new URL('../frontend/src/assets/smartapi-callback.js', import.meta.url)), false);
  assert.match(index, /name="referrer" content="no-referrer"/);
});

test('CLI rejects invalid stdin without printing its contents or a stack trace', () => {
  const child = spawnSync(process.execPath, [fileURLToPath(new URL('./smartapi-check.mjs', import.meta.url))],
    { input: secrets.join(' '), encoding: 'utf8' });
  assert.equal(child.status, 1);
  assertNoSecrets(child.stdout + child.stderr);
  const output = JSON.parse(child.stderr);
  assert.equal(output.status, 'failed');
  assert.match(output.error, /privately/);
});
