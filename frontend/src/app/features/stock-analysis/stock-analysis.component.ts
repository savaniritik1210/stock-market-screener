import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { BehaviorSubject, catchError, combineLatest, of, switchMap, tap } from 'rxjs';
import { AnalysisService } from '../../core/services/analysis.service';
import { TradeAssessment, TradeComparison, TradeMode } from '../../core/models/market.models';
import { ScoreBadgeComponent } from '../../shared/components/score-badge/score-badge.component';
import { PriceChartComponent } from '../../shared/components/price-chart/price-chart.component';
import { TradeEvidenceComponent } from '../../shared/components/trade-evidence/trade-evidence.component';

@Component({
  selector: 'app-stock-analysis',
  standalone: true,
  imports: [CommonModule, RouterLink, ScoreBadgeComponent, PriceChartComponent, TradeEvidenceComponent],
  templateUrl: './stock-analysis.component.html',
  styleUrl: './stock-analysis.component.scss'
})
export class StockAnalysisComponent implements OnInit {
  symbol = '';
  comparison: TradeComparison | null = null;
  selectedMode: TradeMode = 'SWING';
  loading = true;
  error: string | null = null;
  private readonly refresh = new BehaviorSubject(0);
  private readonly destroyRef = inject(DestroyRef);

  constructor(private route: ActivatedRoute, private analysisService: AnalysisService) {}

  ngOnInit(): void {
    combineLatest([this.route.paramMap, this.refresh]).pipe(
      tap(([params]) => {
        this.symbol = (params.get('symbol') ?? '').toUpperCase();
        this.loading = true;
        this.error = null;
        this.comparison = null;
      }),
      switchMap(() => this.analysisService.compare(this.symbol).pipe(
        catchError(() => {
          this.error = 'Analysis unavailable. Check the backend and data provider, then retry. No simulated result has been substituted.';
          return of(null);
        })
      )),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(comparison => {
      this.comparison = comparison;
      this.loading = false;
    });
  }

  get selected(): TradeAssessment | null {
    if (!this.comparison) return null;
    return this.selectedMode === 'INTRADAY' ? this.comparison.intraday : this.comparison.swing;
  }

  reload(): void {
    this.refresh.next(this.refresh.value + 1);
  }
}
