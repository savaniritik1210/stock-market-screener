import { TradeAssessment, TradeComparison, TradeMode } from '../core/models/market.models';

export function assessment(mode: TradeMode = 'SWING'): TradeAssessment {
  return {
    mode, timeframe: mode === 'SWING' ? 'ONE_DAY' : 'FIVE_MINUTE',
    holdingPeriod: mode === 'SWING' ? 'Five trading days' : 'Current session',
    signal: {
      symbol: 'RELIANCE', instrument: 'RELIANCE', signalType: 'NO_SETUP',
      direction: 'NONE', setup: 'NO_SETUP', entry: null, stopLoss: null,
      target1: null, target2: null, target3: null, riskReward: null, score: 0,
      confidence: 0, summary: 'No valid setup', reasons: [], timeframeAnalysis: [],
      dataFreshness: 'Delayed'
    },
    dataSource: 'YAHOO_FINANCE_DELAYED', isLive: false, currentPrice: 100,
    asOf: '2026-09-29T15:25:00+05:30', entryEligible: false,
    dataWarning: 'Delayed data cannot confirm a live entry.', candles: [],
    evidence: {
      status: 'INSUFFICIENT', successRate: null, confidenceLow: null, confidenceHigh: null,
      sampleSize: 3, wins: 1, losses: 1, timeouts: 1, netExpectancyR: 0.1,
      profitFactor: 1.2, from: '2025-01-01', to: '2026-09-29',
      method: 'Fixed rules; chronological evaluation after warmup.',
      warnings: ['Too few trades to estimate a success rate.']
    }
  };
}

export function comparison(): TradeComparison {
  return {
    symbol: 'RELIANCE', generatedAt: '2026-09-30T12:00:00+05:30',
    intraday: assessment('INTRADAY'), swing: assessment(),
    preferredMode: 'NO_CLEAR_WINNER',
    comparisonReason: 'The sample is too small to favor either mode.'
  };
}
