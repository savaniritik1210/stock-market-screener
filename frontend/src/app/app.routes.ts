import { Routes } from '@angular/router';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { StockAnalysisComponent } from './features/stock-analysis/stock-analysis.component';
import { MarketScreenerComponent } from './features/market-screener/market-screener.component';

export const routes: Routes = [
  { path: '', component: DashboardComponent },
  { path: 'stock/:symbol', component: StockAnalysisComponent },
  { path: 'scanner', component: MarketScreenerComponent },
  { path: '**', redirectTo: '' }
];
