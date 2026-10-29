import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { environment } from '../../../environments/environment';
import { comparison } from '../../testing/trade-fixtures';
import { StockAnalysisComponent } from './stock-analysis.component';

describe('StockAnalysisComponent', () => {
  let fixture: ComponentFixture<StockAnalysisComponent>;
  let http: HttpTestingController;
  let params: BehaviorSubject<ReturnType<typeof convertToParamMap>>;
  const endpoint = `${environment.apiBaseUrl}/analysis/RELIANCE/comparison`;

  beforeEach(async () => {
    params = new BehaviorSubject(convertToParamMap({ symbol: 'RELIANCE' }));
    await TestBed.configureTestingModule({
      imports: [StockAnalysisComponent],
      providers: [
        provideRouter([]), provideHttpClient(), provideHttpClientTesting(),
        { provide: ActivatedRoute, useValue: { paramMap: params } }
      ]
    }).compileComponents();
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(StockAnalysisComponent);
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  function text(): string {
    fixture.detectChanges();
    return (fixture.nativeElement as HTMLElement).textContent ?? '';
  }

  it('loads both modes once and makes entry restrictions visible', () => {
    http.expectOne(endpoint).flush(comparison());
    expect(text()).toContain('No clear historical winner');
    expect(text()).toContain('Entry blocked');
    expect(text()).toContain('Insufficient evidence');
    expect(text()).not.toContain('Conditional trade levels');
    expect(text()).not.toContain('Confidence');
    fixture.componentInstance.selectedMode = 'INTRADAY';
    expect(text()).toContain('INTRADAY - NO_SETUP');
    http.expectNone(`${environment.apiBaseUrl}/candles/RELIANCE`);
  });

  it('retains usable swing evidence when intraday data is unavailable', () => {
    const result = comparison();
    result.intraday.evidence.status = 'UNAVAILABLE';
    Object.assign(result.swing.evidence, {
      status: 'SUFFICIENT', successRate: 60, confidenceLow: 42.3, confidenceHigh: 75.4,
      sampleSize: 30, wins: 18, losses: 10, timeouts: 2
    });
    http.expectOne(endpoint).flush(result);
    expect(text()).toContain('Data unavailable');
    expect(text()).toContain('60.0%');
  });

  it('cancels outdated symbol requests rather than showing another stocks results', () => {
    const previous = http.expectOne(endpoint);
    params.next(convertToParamMap({ symbol: 'TCS' }));
    expect(previous.cancelled).toBeTrue();
    const result = comparison();
    result.symbol = 'TCS';
    http.expectOne(`${environment.apiBaseUrl}/analysis/TCS/comparison`).flush(result);
    expect(fixture.componentInstance.symbol).toBe('TCS');
    expect(fixture.componentInstance.comparison?.symbol).toBe('TCS');
  });

  it('clears old results during refresh and reports provider errors without a fallback', () => {
    http.expectOne(endpoint).flush(comparison());
    fixture.componentInstance.reload();
    expect(fixture.componentInstance.comparison).toBeNull();
    http.expectOne(endpoint).flush({ message: 'Unavailable' }, { status: 503, statusText: 'Unavailable' });
    expect(text()).toContain('No simulated result has been substituted');
    expect(text()).not.toContain('Historical target-before-stop success rate');
    fixture.componentInstance.reload();
    http.expectOne(endpoint).flush(comparison());
    expect(fixture.componentInstance.error).toBeNull();
  });
});
