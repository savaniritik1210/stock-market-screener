import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Candle } from '../models/market.models';

@Injectable({ providedIn: 'root' })
export class CandleService {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  getCandles(symbol: string): Observable<Candle[]> {
    return this.http.get<Candle[]>(`${this.baseUrl}/candles/${symbol.toUpperCase()}`);
  }
}
