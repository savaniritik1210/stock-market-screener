import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-score-badge',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span class="badge" [ngClass]="tierClass()" title="Rule-based confluence score; not a probability of profit">
      {{ score }} · {{ tierLabel() }}
    </span>
  `,
  styles: [`
    .badge {
      display: inline-block;
      padding: 4px 12px;
      border-radius: 999px;
      font-weight: 600;
      font-size: 0.85rem;
      letter-spacing: 0.02em;
      color: #0b0f14;
    }
    .strong { background: #33d17a; }
    .valid { background: #7ce38b; }
    .watch { background: #f5c451; }
    .none { background: #e0645a; color: #fff; }
  `]
})
export class ScoreBadgeComponent {
  @Input() score = 0;

  tierLabel(): string {
    if (this.score >= 80) return 'STRONG_SETUP';
    if (this.score >= 65) return 'VALID_SETUP';
    if (this.score >= 50) return 'WATCH';
    return 'NO_SETUP';
  }

  tierClass(): string {
    if (this.score >= 80) return 'strong';
    if (this.score >= 65) return 'valid';
    if (this.score >= 50) return 'watch';
    return 'none';
  }
}
