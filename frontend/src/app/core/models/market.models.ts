export interface Candle {
  instrumentId: number | null;
  symbol: string;
  exchange: string;
  timeframe: string;
  timestamp: string;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
  openInterest: number | null;
  dataSource: string;
  live: boolean;
}

export interface SignalReason {
  category: string;
  code: string;
  description: string;
  positive: boolean;
  weight: number;
}

export interface StockSignal {
  symbol: string;
  instrument: string;
  signalType: 'ENTRY' | 'WATCH' | 'NO_SETUP' | 'INVALIDATED' | 'EXIT';
  direction: 'LONG' | 'SHORT' | 'NONE';
  setup: string;
  entry: number | null;
  stopLoss: number | null;
  target1: number | null;
  target2: number | null;
  target3: number | null;
  riskReward: number | null;
  score: number;
  confidence: number;
  summary: string;
  reasons: SignalReason[];
  timeframeAnalysis: unknown[] | null;
  dataFreshness: string | null;
}

export type TradeMode = 'INTRADAY' | 'SWING';

export interface TradeEvidence {
  status: 'SUFFICIENT' | 'INSUFFICIENT' | 'UNAVAILABLE';
  successRate: number | null;
  confidenceLow: number | null;
  confidenceHigh: number | null;
  sampleSize: number;
  wins: number;
  losses: number;
  timeouts: number;
  netExpectancyR: number | null;
  profitFactor: number | null;
  from: string | null;
  to: string | null;
  method: string;
  warnings: string[];
}

export interface TradeAssessment {
  mode: TradeMode;
  timeframe: string;
  holdingPeriod: string;
  signal: StockSignal;
  dataSource: string;
  isLive: boolean;
  currentPrice: number | null;
  asOf: string | null;
  entryEligible: boolean;
  dataWarning: string;
  evidence: TradeEvidence;
  candles: Candle[];
}

export interface TradeComparison {
  symbol: string;
  generatedAt: string;
  intraday: TradeAssessment;
  swing: TradeAssessment;
  preferredMode: TradeMode | 'NO_CLEAR_WINNER';
  comparisonReason: string;
}

export interface DbHealth {
  connected: boolean;
  status: string;
  database?: string;
  databaseVersion?: string;
  url?: string;
  error?: string;
}

export interface InstrumentSummary {
  id: number;
  symbol: string;
  exchange: string;
  companyName: string;
  instrumentType: string;
  sector: string;
  active: boolean;
  tradable: boolean;
}
