import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { TradeAssessment } from '../../../core/models/market.models';

@Component({
  selector: 'app-trade-evidence',
  standalone: true,
  imports: [CommonModule],
  template: `
    <article *ngIf="assessment as a" [attr.aria-label]="a.mode + ' historical evidence'">
      <header>
        <h2>{{ a.mode === 'INTRADAY' ? 'Intraday' : '5-day swing' }}</h2>
        <span>{{ a.evidence.status }}</span>
      </header>
      <p>{{ a.holdingPeriod }}</p>
      <ng-container *ngIf="a.evidence.status === 'SUFFICIENT' && a.evidence.successRate !== null; else insufficient">
        <div class="rate">{{ a.evidence.successRate | number:'1.1-1' }}%</div>
        <p>Historical target-before-stop success rate</p>
        <p class="interval">95% interval:
          {{ a.evidence.confidenceLow | number:'1.1-1' }}% -
          {{ a.evidence.confidenceHigh | number:'1.1-1' }}%
        </p>
      </ng-container>
      <ng-template #insufficient>
        <div class="unavailable">{{ a.evidence.status === 'UNAVAILABLE' ? 'Data unavailable' : 'Insufficient evidence' }}</div>
        <p>No defensible probability estimate to display.</p>
      </ng-template>
      <dl>
        <div><dt>Evaluated trades</dt><dd>{{ a.evidence.sampleSize }}</dd></div>
        <div><dt>Success / failure / timed out</dt>
          <dd>{{ a.evidence.wins }} / {{ a.evidence.losses }} / {{ a.evidence.timeouts }}</dd></div>
        <div><dt>Net expectancy per trade</dt>
          <dd>{{ a.evidence.netExpectancyR === null ? 'N/A' : (a.evidence.netExpectancyR | number:'1.2-2') + ' R' }}</dd></div>
        <div><dt>Profit factor</dt>
          <dd>{{ a.evidence.profitFactor === null ? 'N/A' : (a.evidence.profitFactor | number:'1.2-2') }}</dd></div>
      </dl>
      <p *ngIf="a.evidence.from && a.evidence.to">
        Evaluation: {{ a.evidence.from | date:'dd MMM yyyy':'+0530' }} -
        {{ a.evidence.to | date:'dd MMM yyyy':'+0530' }} (IST)
      </p>
      <details>
        <summary>Method and limitations</summary>
        <p>{{ a.evidence.method }}</p>
        <ul><li *ngFor="let warning of a.evidence.warnings">{{ warning }}</li></ul>
      </details>
      <p class="caution">Historical frequency, not a calibrated probability for your next trade.
        R is the initial planned price risk. Costs are modeled, not guaranteed.</p>
    </article>
  `,
  styles: [`
    :host { display: block; height: 100%; }
    article { height: 100%; padding: 20px; background: #111820; border: 1px solid #2a3a48; border-radius: 12px; }
    header { display: flex; align-items: center; justify-content: space-between; gap: 12px; flex-wrap: wrap; }
    h2 { font-size: 1.15rem; margin: 0; }
    header span { color: #f5c451; font-size: .75rem; }
    p, details { color: #a9bac8; font-size: .85rem; line-height: 1.5; }
    .rate { font-size: 2.5rem; font-weight: 700; color: #d4e8ff; }
    .interval { color: #d4e8ff; }
    .unavailable { color: #f5c451; font-size: 1.3rem; margin-top: 20px; }
    dl div { display: flex; justify-content: space-between; gap: 12px; margin: 12px 0; font-size: .85rem; }
    dt { color: #a9bac8; } dd { margin: 0; text-align: right; }
    summary { cursor: pointer; color: #d4e8ff; }
    li { margin: 8px 0; }
    .caution { border-top: 1px solid #2a3a48; padding-top: 12px; }
  `]
})
export class TradeEvidenceComponent {
  @Input({ required: true }) assessment!: TradeAssessment;
}
