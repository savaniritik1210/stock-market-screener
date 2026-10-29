import {
  AfterViewInit,
  Component,
  ElementRef,
  Input,
  OnChanges,
  SimpleChanges,
  ViewChild
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Candle } from '../../../core/models/market.models';

@Component({
  selector: 'app-price-chart',
  standalone: true,
  imports: [CommonModule],
  template: `
    <canvas #canvas width="900" height="260" class="price-canvas"></canvas>
  `,
  styles: [`
    .price-canvas {
      width: 100%;
      height: 260px;
      background: #0f151c;
      border-radius: 8px;
    }
  `]
})
export class PriceChartComponent implements AfterViewInit, OnChanges {
  @Input() candles: Candle[] = [];
  @Input() entry: number | null = null;
  @Input() stopLoss: number | null = null;
  @Input() target1: number | null = null;

  @ViewChild('canvas', { static: true }) canvasRef!: ElementRef<HTMLCanvasElement>;

  ngAfterViewInit(): void {
    this.draw();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.canvasRef) {
      this.draw();
    }
  }

  private draw(): void {
    const canvas = this.canvasRef.nativeElement;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const width = canvas.width;
    const height = canvas.height;
    ctx.clearRect(0, 0, width, height);

    if (!this.candles || this.candles.length === 0) {
      ctx.fillStyle = '#5b6b7a';
      ctx.font = '14px sans-serif';
      ctx.fillText('No candle data available', 16, height / 2);
      return;
    }

    const closes = this.candles.map(c => c.close);
    const volumes = this.candles.map(c => c.volume);
    const overlayValues = [this.entry, this.stopLoss, this.target1].filter(
      (v): v is number => v !== null && v !== undefined
    );

    const min = Math.min(...closes, ...overlayValues);
    const max = Math.max(...closes, ...overlayValues);
    const range = max - min || 1;

    const priceAreaHeight = height * 0.72;
    const volumeAreaTop = priceAreaHeight + 10;
    const volumeAreaHeight = height - volumeAreaTop - 10;
    const maxVolume = Math.max(...volumes, 1);

    const stepX = width / Math.max(this.candles.length - 1, 1);

    // Volume bars
    ctx.fillStyle = 'rgba(90, 140, 255, 0.35)';
    this.candles.forEach((c, i) => {
      const barHeight = (c.volume / maxVolume) * volumeAreaHeight;
      const x = i * stepX;
      ctx.fillRect(x - 1, height - 10 - barHeight, Math.max(stepX - 1, 1), barHeight);
    });

    // Price line
    ctx.beginPath();
    ctx.strokeStyle = '#39d98a';
    ctx.lineWidth = 2;
    closes.forEach((price, i) => {
      const x = i * stepX;
      const y = priceAreaHeight - ((price - min) / range) * priceAreaHeight;
      if (i === 0) {
        ctx.moveTo(x, y);
      } else {
        ctx.lineTo(x, y);
      }
    });
    ctx.stroke();

    // Overlay lines: entry / stop / target
    this.drawLevelLine(ctx, this.entry, min, range, priceAreaHeight, width, '#5aa9ff', 'Entry');
    this.drawLevelLine(ctx, this.stopLoss, min, range, priceAreaHeight, width, '#e0645a', 'Stop');
    this.drawLevelLine(ctx, this.target1, min, range, priceAreaHeight, width, '#f5c451', 'Target 1');
  }

  private drawLevelLine(
    ctx: CanvasRenderingContext2D,
    value: number | null,
    min: number,
    range: number,
    priceAreaHeight: number,
    width: number,
    color: string,
    label: string
  ): void {
    if (value === null || value === undefined) return;
    const y = priceAreaHeight - ((value - min) / range) * priceAreaHeight;
    ctx.save();
    ctx.strokeStyle = color;
    ctx.setLineDash([4, 4]);
    ctx.beginPath();
    ctx.moveTo(0, y);
    ctx.lineTo(width, y);
    ctx.stroke();
    ctx.setLineDash([]);
    ctx.fillStyle = color;
    ctx.font = '11px sans-serif';
    ctx.fillText(`${label} ${value.toFixed(2)}`, width - 110, y - 4);
    ctx.restore();
  }
}
