import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { StockService } from '../../core/services/stock.service';
import { DbHealth } from '../../core/models/market.models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  symbol = '';
  popularSymbols = ['RELIANCE', 'TCS', 'INFY', 'HDFCBANK', 'ICICIBANK', 'SBIN', 'ITC', 'LT'];
  dbHealth: DbHealth | null = null;
  healthLoading = true;
  healthError = false;

  constructor(private stockService: StockService, private router: Router) {}

  ngOnInit(): void {
    this.stockService.dbHealth().subscribe({
      next: health => {
        this.dbHealth = health;
        this.healthLoading = false;
      },
      error: () => {
        this.healthError = true;
        this.healthLoading = false;
      }
    });
  }

  goToStock(symbol: string): void {
    const trimmed = symbol.trim().toUpperCase();
    if (!trimmed) return;
    this.router.navigate(['/stock', trimmed]);
  }

  onSearchSubmit(): void {
    this.goToStock(this.symbol);
  }
}
