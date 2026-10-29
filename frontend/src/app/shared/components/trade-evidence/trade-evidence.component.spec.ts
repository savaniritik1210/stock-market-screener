import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TradeEvidenceComponent } from './trade-evidence.component';
import { assessment } from '../../../testing/trade-fixtures';

describe('TradeEvidenceComponent', () => {
  let fixture: ComponentFixture<TradeEvidenceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [TradeEvidenceComponent] }).compileComponents();
    fixture = TestBed.createComponent(TradeEvidenceComponent);
    fixture.componentRef.setInput('assessment', assessment());
  });

  function text(): string {
    fixture.detectChanges();
    return (fixture.nativeElement as HTMLElement).textContent ?? '';
  }

  it('does not convert insufficient evidence into a displayed probability', () => {
    fixture.componentInstance.assessment.evidence.successRate = 80;
    expect(text()).toContain('Insufficient evidence');
    expect(text()).not.toContain('80.0%');
    expect((fixture.nativeElement as HTMLElement).querySelector('.rate')).toBeNull();
  });

  it('shows zero percent as a valid historical estimate, with its interval', () => {
    Object.assign(fixture.componentInstance.assessment.evidence, {
      status: 'SUFFICIENT', successRate: 0, confidenceLow: 0, confidenceHigh: 11.4,
      sampleSize: 30, wins: 0, losses: 30, timeouts: 0
    });
    expect(text()).toContain('0.0%');
    expect(text()).toContain('11.4%');
    expect(text()).toContain('not a calibrated probability');
  });

  it('distinguishes missing data from weak historical evidence', () => {
    fixture.componentInstance.assessment.evidence.status = 'UNAVAILABLE';
    fixture.componentInstance.assessment.evidence.profitFactor = null;
    expect(text()).toContain('Data unavailable');
    expect(text()).toContain('N/A');
  });
});
