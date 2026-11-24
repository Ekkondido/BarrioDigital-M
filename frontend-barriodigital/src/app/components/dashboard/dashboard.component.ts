import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { RequestsService, CitizenRequest } from '../../services/requests.service';
import { CatalogService, ProcedureType } from '../../services/catalog.service';
import { ReportService, KpiData } from '../../services/report.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  authService = inject(AuthService);
  private requestsService = inject(RequestsService);
  private catalogService = inject(CatalogService);
  private reportService = inject(ReportService);

  currentUser$ = this.authService.currentUser$;
  requests: CitizenRequest[] = [];
  procedures: ProcedureType[] = [];
  kpis: KpiData | null = null;
  loading = true;

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.requestsService.getRequests().subscribe({
      next: (reqs) => this.requests = reqs
    });

    this.catalogService.getProcedures().subscribe({
      next: (procs) => this.procedures = procs
    });

    this.reportService.getKpis().subscribe({
      next: (k) => {
        this.kpis = k;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  getPendingAdmissionCount(): number {
    return this.requests.filter(r => r.status === 'INGRESADO').length;
  }

  getOnFieldCount(): number {
    return this.requests.filter(r => r.status === 'EN_TERRENO').length;
  }

  getResolvedCount(): number {
    return this.requests.filter(r => r.status === 'RESUELTO').length;
  }
}
