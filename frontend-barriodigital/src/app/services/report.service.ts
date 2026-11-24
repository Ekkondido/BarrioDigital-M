import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, of } from 'rxjs';
import { environment } from '../../environments/environment';

export interface KpiData {
  range: string;
  averageResolutionHours: number;
  activeRequestsTotal: number;
  resolvedTotal: number;
  rejectedTotal: number;
  statuses: Record<string, number>;
  requestsPerHour: Record<string, number>;
}

export interface TopProcedure {
  procedureName: string;
  count: number;
  percentage: number;
}

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiGatewayUrl}/api/report`;

  private mockKpis: KpiData = {
    range: 'last24h',
    averageResolutionHours: 28.5,
    activeRequestsTotal: 36,
    resolvedTotal: 42,
    rejectedTotal: 3,
    statuses: {
      INGRESADO: 14,
      ADMITIDO: 8,
      EN_GESTION: 5,
      EN_TERRENO: 9,
      RESUELTO: 42,
      RECHAZADO: 3
    },
    requestsPerHour: {
      '08:00': 4,
      '09:00': 9,
      '10:00': 15,
      '11:00': 18,
      '12:00': 12,
      '13:00': 7,
      '14:00': 8,
      '15:00': 14,
      '16:00': 11
    }
  };

  private mockTopProcedures: TopProcedure[] = [
    { procedureName: 'Reparación de Luminaria Pública', count: 32, percentage: 33 },
    { procedureName: 'Poda de Árboles en Vía Pública', count: 24, percentage: 25 },
    { procedureName: 'Bacheo y Reparación de Calzadas', count: 18, percentage: 19 },
    { procedureName: 'Retiro de Escombros y Enseres', count: 15, percentage: 15 },
    { procedureName: 'Fumigación y Control de Plagas', count: 8, percentage: 8 }
  ];

  getKpis(range: string = 'last24h'): Observable<KpiData> {
    return this.http.get<KpiData>(`${this.apiUrl}/kpis?range=${range}`).pipe(
      catchError(() => of(this.mockKpis))
    );
  }

  getTopProcedures(range: string = 'last7d'): Observable<TopProcedure[]> {
    return this.http.get<TopProcedure[]>(`${this.apiUrl}/top-procedures?range=${range}`).pipe(
      catchError(() => of(this.mockTopProcedures))
    );
  }
}
