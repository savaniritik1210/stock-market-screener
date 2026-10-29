# Getting Started

### Angel One read-only connection check

The optional [connection checker](tools/smartapi-check.mjs) uses local-only login,
fetches an NSE SBIN quote, and subscribes to SBIN LTP ticks for 20 seconds. It does
not place orders or replace the screener's Yahoo data provider.

Requires Node.js 22+. Install dependencies with
`npm.cmd --prefix tools ci`, then run:

```powershell
node .\tools\smartapi-check.mjs
```

Enter the API key, client code, PIN and current TOTP at the masked prompts, never
in chat, source code, environment files or command arguments. Regenerate any key
previously shared in chat before using this tool. No credentials are saved;
they exist only in the local process's memory. The checker refuses to start with
Node debug, `NODE_OPTIONS`, TLS-disable or TLS-key-log environment settings.
Use a regular terminal, not the JavaScript debugger or a recorded terminal session.
REST requests use HTTPS headers/body; WebSocket authentication uses WSS headers,
not URL query parameters. TLS verification remains enabled and redirects are
rejected. Only allowlisted results and fixed error messages are printed.

The checker opens no HTTP listener and uses no browser, frontend or public tunnel.
The former public callback bridge has been removed. Stop public port forwarding
in VS Code when it is not needed. Angel One may still require a redirect URL during
app registration, but this login flow does not use it.

The checker retrieves the current outbound IPv4 from api4.ipify.org before prompting
for credentials; no Angel One credentials are sent to that service.
Confirm applicable static-IP requirements with Angel One and
your ISP; discovering an IP does not prove that it is static.

Results appear only in the local terminal. The checker attempts to log out its
SmartAPI session after success or failure; failed logout is explicitly reported.
Do not run it against a session used by another trading application. Forced
termination can prevent logout; use Angel One's session controls if necessary.
A quote or connected socket
alone is not evidence of live data: `liveDataVerified` requires a tick with an
exchange timestamp no older than 60 seconds (up to 5 seconds future clock skew).
After NSE market hours, a quote may be the last traded price and no ticks may arrive.

This reduces exposure, not a guarantee of absolute secrecy: credentials must exist
in process memory and be sent to Angel One. Malware, privileged inspection, memory
dumps, or broker-side logging cannot be prevented by this tool. JavaScript
strings cannot be reliably zeroed. Revocation/rotation is still required for any
previously disclosed key.

Offline checks: `npm.cmd --prefix tools test` and
`npm.cmd --prefix frontend run build`.

### Evidence-based comparison backend

`GET /api/analysis/RELIANCE/comparison` returns intraday (15-minute bars, NSE session close)
and swing (daily bars, five observed trading sessions) research assessments. The legacy
`GET /api/analysis/RELIANCE` returns the intraday `StockSignal` shape.
Each comparison assessment includes `candles`: the last at most 200 completed bars of
that mode, from the same provider fetch used for analysis (empty when unavailable).
Intraday never carries overnight. Swing includes the next five daily session bars
starting with the entry session and exits at the fifth session close.

The fixed, long-only strategy uses a 20-bar breakout, SMA20/SMA50 trend, volume confirmation,
and ATR14-based stops. Prices and returns use decimal arithmetic. The first 30% of history
(at least 60 bars) is reserved for warmup; no parameter fitting uses held-out outcomes.
Historical trades enter no earlier than the next bar, use stop-first ambiguity handling,
gap-aware fills, non-overlap, slippage and fees. Intraday sampling allows at most one
trade per session. Timeouts are failures for the target-before-stop success metric.
Wins also require positive net return after costs; hitting target 1 without net profit
counts as unsuccessful. A profitable timeout is still unsuccessful for this metric.
The evidence method exposes strategy version, parameters, per-side fee/slippage,
sample threshold, and symbol/mode/setup/direction grouping without cross-group pooling.

Success estimates and 95% Wilson intervals are withheld below 30 completed trades.
Non-overlap does not prove statistical independence. The comparison requires separated
descriptive intervals; serial dependence and multiple comparisons across symbols,
modes or parameters are not adjusted, so nominal statistical coverage is not assured.
A fixed untrained strategy is not automatically validated or calibrated.
This chronological holdout-style simulation has not been externally validated.
Strategy provenance is unaudited: if retrospective strategy design was informed by
the same dataset, the resulting estimate is not genuinely out of sample. The percentage
is historical success frequency, not calibrated probability for the current entry.
The comparison requires separated
intervals and positive after-cost expectancy in both modes; otherwise it has no clear
winner. This is not a calibrated probability for an individual entry, profitability
guarantee, or production-readiness claim. Different horizons and observation windows
are not directly equivalent. Yahoo history and corporate actions require independent
verification; exchange holidays and execution liquidity are not modeled.

Yahoo is explicitly delayed; entries remain disabled. Qualifying plans are `WATCH`,
not executable signals. Legacy `score` measures rule completion, not probability;
legacy `confidence=0` means uncalibrated. Targets 2/3 are illustrative R levels, not
historically evaluated. Provider failures surface per mode; no automatic mock fallback
exists. `MARKET_DATA_MOCK_ENABLED=true` is explicit synthetic development mode and
synthetic candles never count as evidence.

Production runtime uses PostgreSQL and requires `DB_PASSWORD`; optional `DB_URL` and
`DB_USERNAME` override connection settings. H2 is test-scoped only. Tests use the `test`
profile and require neither credentials nor external market-data requests.

Configuration is in `src\main\resources\application.properties`: minimum samples (floor
30), bounded cache size/TTL, provider calls per minute, fees/slippage in basis points per
side, ATR stop multiplier, target R and mode risk bounds. Default comparison requests
fetch 55 days of intraday and five years of daily data once each, cached per normalized
symbol for 60 seconds. Parameter changes after inspecting results invalidate held-out
interpretation.

Targeted offline verification:

```powershell
.\mvnw.cmd -q "-Dtest=TradeAnalysisTest,TradeComparisonApiTest,YahooFinanceMarketDataClientTest,StockMarketScreenerApplicationTests" test
```

### Reference Documentation

For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/4.1.1/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/4.1.1/maven-plugin/build-image.html)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/4.1.1/reference/actuator/index.html)
* [Spring Data JPA](https://docs.spring.io/spring-boot/4.1.1/reference/data/sql.html#data.sql.jpa-and-spring-data)
* [Validation](https://docs.spring.io/spring-boot/4.1.1/reference/io/validation.html)
* [Spring Web](https://docs.spring.io/spring-boot/4.1.1/reference/web/servlet.html)
* [WebSocket](https://docs.spring.io/spring-boot/4.1.1/reference/messaging/websockets.html)

### Guides

The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service with Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
* [Validation](https://spring.io/guides/gs/validating-form-input/)
* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Using WebSocket to build an interactive web application](https://spring.io/guides/gs/messaging-stomp-websocket/)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the
parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.
