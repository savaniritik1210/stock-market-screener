import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { StockSignal, TradeComparison } from '../models/market.models';

@Injectable({ providedIn: 'root' })
export class AnalysisService {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  analyze(symbol: string): Observable<StockSignal> {
    return this.http.get<StockSignal>(`${this.baseUrl}/analysis/${encodeURIComponent(symbol.trim().toUpperCase())}`);
  }

  compare(symbol: string): Observable<TradeComparison> {
    return this.http.get<TradeComparison>(
      `${this.baseUrl}/analysis/${encodeURIComponent(symbol.trim().toUpperCase())}/comparison`
    );
  }

  latestSignal(symbol: string): Observable<StockSignal> {
    return this.http.get<StockSignal>(`${this.baseUrl}/signals/${encodeURIComponent(symbol.trim().toUpperCase())}/latest`);
  }
}
