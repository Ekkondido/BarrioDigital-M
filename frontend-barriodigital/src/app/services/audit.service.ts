import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, of } from 'rxjs';
import { environment } from '../../environments/environment';

export interface AuditEvent {
  id: number;
  eventId: string;
  requestId: number;
  trackingNumber: string;
  action: string;
  actor: string;
  oldStatus?: string;
  newStatus?: string;
  timestamp: string;
  detailsJson?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuditService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiGatewayUrl}/api/audit`;

  private mockAuditEvents: AuditEvent[] = [
    {
      id: 1,
      eventId: 'evt-001',
      requestId: 1,
      trackingNumber: 'REQ-2026-0001',
      action: 'INGRESADO',
      actor: 'roberto.vecino@gmail.com (Vecino)',
      oldStatus: undefined,
      newStatus: 'INGRESADO',
      timestamp: new Date(Date.now() - 3600000 * 24).toISOString(),
      detailsJson: '{"channel":"web-portal","ip":"190.161.45.12","procedure":"Poda de Árboles"}'
    },
    {
      id: 2,
      eventId: 'evt-002',
      requestId: 1,
      trackingNumber: 'REQ-2026-0001',
      action: 'ADMITIDO',
      actor: 'mariana.funcionario@barriodigital.cl (Funcionario)',
      oldStatus: 'INGRESADO',
      newStatus: 'ADMITIDO',
      timestamp: new Date(Date.now() - 3600000 * 18).toISOString(),
      detailsJson: '{"notes":"Cupo validado en catálogo de Podas","dailyQuotaReduced":true}'
    },
    {
      id: 3,
      eventId: 'evt-003',
      requestId: 1,
      trackingNumber: 'REQ-2026-0001',
      action: 'EN_TERRENO',
      actor: 'mariana.funcionario@barriodigital.cl (Funcionario)',
      oldStatus: 'ADMITIDO',
      newStatus: 'EN_TERRENO',
      timestamp: new Date(Date.now() - 3600000 * 2).toISOString(),
      detailsJson: '{"assignedCrew":"Cuadrilla #2 - Despeje Vial","ticketGenerated":true}'
    },
    {
      id: 4,
      eventId: 'evt-004',
      requestId: 2,
      trackingNumber: 'REQ-2026-0002',
      action: 'INGRESADO',
      actor: 'mariana.vecina@outlook.com (Vecino)',
      oldStatus: undefined,
      newStatus: 'INGRESADO',
      timestamp: new Date(Date.now() - 3600000 * 12).toISOString(),
      detailsJson: '{"channel":"mobile-app","ip":"186.105.88.2"}'
    },
    {
      id: 5,
      eventId: 'evt-005',
      requestId: 2,
      trackingNumber: 'REQ-2026-0002',
      action: 'ADMITIDO',
      actor: 'mariana.funcionario@barriodigital.cl (Funcionario)',
      oldStatus: 'INGRESADO',
      newStatus: 'ADMITIDO',
      timestamp: new Date(Date.now() - 3600000 * 4).toISOString(),
      detailsJson: '{"notes":"Visita priorizada para turno noche"}'
    }
  ];

  getAuditEvents(requestId?: number, actor?: string): Observable<AuditEvent[]> {
    let url = this.apiUrl;
    const params: string[] = [];
    if (requestId) params.push(`requestId=${requestId}`);
    if (actor) params.push(`actor=${actor}`);
    if (params.length > 0) url += `?${params.join('&')}`;

    return this.http.get<AuditEvent[]>(url).pipe(
      catchError(() => {
        let events = [...this.mockAuditEvents];
        if (requestId) {
          events = events.filter(e => e.requestId === Number(requestId));
        }
        if (actor) {
          events = events.filter(e => e.actor.toLowerCase().includes(actor.toLowerCase()));
        }
        return of(events);
      })
    );
  }

  getAuditByRequestId(requestId: number): Observable<AuditEvent[]> {
    return this.http.get<AuditEvent[]>(`${this.apiUrl}/request/${requestId}`).pipe(
      catchError(() => of(this.mockAuditEvents.filter(e => e.requestId === requestId)))
    );
  }
}
