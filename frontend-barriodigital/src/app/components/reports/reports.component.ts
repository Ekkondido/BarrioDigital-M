import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReportService, KpiData, TopProcedure } from '../../services/report.service';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.css']
})
export class ReportsComponent implements OnInit {
  private reportService = inject(ReportService);

  kpis: KpiData | null = null;
  topProcedures: TopProcedure[] = [];
  selectedRange: string = 'last24h';
  loading = true;

  ngOnInit(): void {
    this.loadReports();
  }

  loadReports(): void {
    this.loading = true;
    this.reportService.getKpis(this.selectedRange).subscribe(data => {
      this.kpis = data;
      this.loading = false;
    });

    this.reportService.getTopProcedures('last7d').subscribe(top => {
      this.topProcedures = top;
    });
  }

  onRangeChange(range: string): void {
    this.selectedRange = range;
    this.loadReports();
  }

  getHourlyKeys(): string[] {
    return this.kpis && this.kpis.requestsPerHour ? Object.keys(this.kpis.requestsPerHour) : [];
  }

  getMaxHourlyCount(): number {
    if (!this.kpis || !this.kpis.requestsPerHour) return 1;
    const values = Object.values(this.kpis.requestsPerHour);
    return Math.max(...values, 1);
  }
}
