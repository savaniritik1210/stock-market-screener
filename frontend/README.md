# Stock Market Screener

Angular 17 research dashboard on **http://localhost:4300**, connected to the existing
Spring Boot backend on port **8085**.

## Run

From the repository root, configure `DB_URL`, `DB_USERNAME` and `DB_PASSWORD`
for your PostgreSQL database, then run `mvn spring-boot:run`.
In another terminal:

```powershell
Set-Location frontend
npm.cmd ci
npm.cmd start
```

`npm.cmd run build` creates the production frontend bundle. Serve it with a
web server configured to return `index.html` for Angular routes. Backend origin
is configured in `src/environments/environment.ts`; CORS uses `FRONTEND_ORIGIN`.
Do not put database passwords or provider credentials in frontend configuration.

## Entry research

Enter an NSE symbol such as `RELIANCE`. The analysis page calls
`GET /api/analysis/RELIANCE/comparison` to compare:

- Intraday: Target 1 before the stop-loss, with exit at session close.
- Swing: Target 1 before the stop-loss, within five trading days.

Both modes use the backend strategy for historical and current assessments.
The screen separates the rule score from historical target-before-stop frequency.
It shows the sample size, uncertainty interval, timeouts, profit factor, and
modeled net expectancy in units of planned risk (R). Expand each card for the
evaluation method and limitations. Historical frequency is **not** a calibrated
probability for the next entry.

If evidence is insufficient or the provider is unavailable, no percentage is
displayed. The comparison can legitimately return **NO_CLEAR_WINNER**.
A higher success rate alone does not establish profitability. The two horizons
are different experiments, not interchangeable risk exposures.

Yahoo data is not a verified executable real-time feed. Delayed/stale data must
not authorize live entries, and provider failures must not become simulated
trades. Conditional levels are for research and paper trading, not execution.
Mock data is only appropriate for explicit development/testing.

## Limitations

This is not a certified or independently validated trading system. Costs and
slippage are assumptions; corporate actions, incomplete history, serially
correlated trades, changing regimes, and repeated comparisons can bias results.
A statistical interval does not account for all those risks. Reliable licensed
live data, broader walk-forward evaluation and paper-trading monitoring are
required before considering deployment with real capital. No order execution is
implemented.

## Frontend tests

```powershell
npm.cmd test -- --watch=false --browsers=ChromeHeadless
```

The comparison tests use deterministic HTTP fixtures, not live exchange data.
