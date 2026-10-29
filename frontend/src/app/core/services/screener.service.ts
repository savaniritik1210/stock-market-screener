import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { StockSignal } from '../models/market.models';

export interface ScreenerRequest {
  market?: string;
  mode?: string;
  minimumScore?: number;
  timeframe?: string;
  directions?: string[];
  maxResults?: number;
  symbols?: string[];
}

@Injectable({ providedIn: 'root' })
export class ScreenerService {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  scan(request: ScreenerRequest): Observable<StockSignal[]> {
    return this.http.post<StockSignal[]>(`${this.baseUrl}/screener/scan`, request);
  }
}
