import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DbHealth, InstrumentSummary } from '../models/market.models';

@Injectable({ providedIn: 'root' })
export class StockService {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  search(query: string): Observable<InstrumentSummary[]> {
    return this.http.get<InstrumentSummary[]>(`${this.baseUrl}/stocks/search`, {
      params: { q: query }
    });
  }

  getBySymbol(symbol: string): Observable<InstrumentSummary> {
    return this.http.get<InstrumentSummary>(`${this.baseUrl}/stocks/${symbol.toUpperCase()}`);
  }

  dbHealth(): Observable<DbHealth> {
    return this.http.get<DbHealth>(`${this.baseUrl}/system/db/health`);
  }
}
