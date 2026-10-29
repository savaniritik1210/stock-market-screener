import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ScreenerService } from '../../core/services/screener.service';
import { StockSignal } from '../../core/models/market.models';
import { ScoreBadgeComponent } from '../../shared/components/score-badge/score-badge.component';

@Component({
  selector: 'app-market-screener',
  standalone: true,
  imports: [CommonModule, RouterLink, ScoreBadgeComponent],
  templateUrl: './market-screener.component.html',
  styleUrl: './market-screener.component.scss'
})
export class MarketScreenerComponent {
  results: StockSignal[] = [];
  loading = false;
  error: string | null = null;
  hasScanned = false;

  constructor(private screenerService: ScreenerService) {}

  runScan(): void {
    this.loading = true;
    this.error = null;
    this.screenerService.scan({ market: 'NSE', mode: 'SWING', minimumScore: 0, maxResults: 20 }).subscribe({
      next: results => {
        this.results = [...results].sort((a, b) => b.score - a.score);
        this.loading = false;
        this.hasScanned = true;
      },
      error: err => {
        this.error = err?.error?.message || 'Failed to run scan. Is the backend running on port 8085?';
        this.loading = false;
      }
    });
  }
}
