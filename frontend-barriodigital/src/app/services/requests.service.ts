import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, of, throwError } from 'rxjs';
import { environment } from '../../environments/environment';

export interface CitizenRequest {
  id: number;
  trackingNumber: string;
  procedureId: number;
  procedureName: string;
  citizenEmail: string;
  citizenName: string;
  citizenPhone?: string;
  address: string;
  description: string;
  status: 'INGRESADO' | 'ADMITIDO' | 'EN_GESTION' | 'EN_TERRENO' | 'RESUELTO' | 'RECHAZADO';
  assignedCrew?: string;
  resolutionNotes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateRequestPayload {
  procedureId: number;
  procedureName: string;
  citizenEmail?: string;
  citizenName?: string;
  citizenPhone?: string;
  address: string;
  description: string;
}

export interface UpdateStatusPayload {
  status: string;
  assignedCrew?: string;
  resolutionNotes?: string;
}

@Injectable({
  providedIn: 'root'
})
export class RequestsService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiGatewayUrl}/api/requests`;

  private mockRequests: CitizenRequest[] = [
    {
      id: 1,
      trackingNumber: 'REQ-2026-0001',
      procedureId: 1,
      procedureName: 'Poda de Árboles en Vía Pública',
      citizenEmail: 'roberto.vecino@gmail.com',
      citizenName: 'Roberto Salgado',
      citizenPhone: '+56912345678',
      address: 'Av. Las Condes 4500, Pasaje Los Olmos 12',
      description: 'Ramas de eucalipto rozando cables de media tensión en frontis de la casa.',
      status: 'EN_TERRENO',
      assignedCrew: 'Cuadrilla #2 - Despeje Vial',
      createdAt: new Date(Date.now() - 3600000 * 24).toISOString(),
      updatedAt: new Date(Date.now() - 3600000 * 2).toISOString()
    },
    {
      id: 2,
      trackingNumber: 'REQ-2026-0002',
      procedureId: 2,
      procedureName: 'Reparación de Luminaria Pública',
      citizenEmail: 'mariana.vecina@outlook.com',
      citizenName: 'Mariana Silva',
      citizenPhone: '+56987654321',
      address: 'Calle San Martín con Prat',
      description: 'Luminaria intermitente hace 3 días, genera inseguridad de noche.',
      status: 'ADMITIDO',
      createdAt: new Date(Date.now() - 3600000 * 12).toISOString(),
      updatedAt: new Date(Date.now() - 3600000 * 4).toISOString()
    },
    {
      id: 3,
      trackingNumber: 'REQ-2026-0003',
      procedureId: 3,
      procedureName: 'Bacheo y Reparación de Calzadas',
      citizenEmail: 'carlos.conductor@gmail.com',
      citizenName: 'Carlos Fuentes',
      citizenPhone: '+56933445566',
      address: 'Av. Providencia 1204',
      description: 'Hoyo de gran profundidad en pista derecha frente a paradero.',
      status: 'INGRESADO',
      createdAt: new Date(Date.now() - 3600000 * 3).toISOString(),
      updatedAt: new Date(Date.now() - 3600000 * 3).toISOString()
    },
    {
      id: 4,
      trackingNumber: 'REQ-2026-0004',
      procedureId: 4,
      procedureName: 'Retiro de Escombros y Enseres',
      citizenEmail: 'ana.vecina@yahoo.cl',
      citizenName: 'Ana Morales',
      citizenPhone: '+56944556677',
      address: 'Pasaje El Roble 78',
      description: 'Colchón antiguo y maderas tras remodelación.',
      status: 'RESUELTO',
      assignedCrew: 'Tolva Municipal #1',
      resolutionNotes: 'Retiro completado exitosamente a las 11:30 hrs.',
      createdAt: new Date(Date.now() - 3600000 * 72).toISOString(),
      updatedAt: new Date(Date.now() - 3600000 * 18).toISOString()
    }
  ];

  getRequests(status?: string, citizenEmail?: string): Observable<CitizenRequest[]> {
    let url = this.apiUrl;
    const params: string[] = [];
    if (status) params.push(`status=${status}`);
    if (citizenEmail) params.push(`citizenEmail=${citizenEmail}`);
    if (params.length > 0) url += `?${params.join('&')}`;

    return this.http.get<CitizenRequest[]>(url).pipe(
      catchError(() => {
        let filtered = [...this.mockRequests];
        if (status) {
          filtered = filtered.filter(r => r.status === status);
        }
        if (citizenEmail) {
          filtered = filtered.filter(r => r.citizenEmail.toLowerCase() === citizenEmail.toLowerCase());
        }
        return of(filtered);
      })
    );
  }

  getRequestById(id: number): Observable<CitizenRequest | undefined> {
    return this.http.get<CitizenRequest>(`${this.apiUrl}/${id}`).pipe(
      catchError(() => of(this.mockRequests.find(r => r.id === id)))
    );
  }

  createRequest(payload: CreateRequestPayload): Observable<CitizenRequest> {
    return this.http.post<CitizenRequest>(this.apiUrl, payload).pipe(
      catchError(() => {
        const nextId = this.mockRequests.length + 1;
        const newReq: CitizenRequest = {
          id: nextId,
          trackingNumber: `REQ-2026-${String(nextId).padStart(4, '0')}`,
          procedureId: payload.procedureId,
          procedureName: payload.procedureName,
          citizenEmail: payload.citizenEmail || 'vecino@barriodigital.cl',
          citizenName: payload.citizenName || 'Vecino Anónimo',
          citizenPhone: payload.citizenPhone,
          address: payload.address,
          description: payload.description,
          status: 'INGRESADO',
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        };
        this.mockRequests.unshift(newReq);
        return of(newReq);
      })
    );
  }

  updateStatus(id: number, payload: UpdateStatusPayload): Observable<CitizenRequest> {
    return this.http.put<CitizenRequest>(`${this.apiUrl}/${id}/status`, payload).pipe(
      catchError((error) => {
        // Validación de regla de negocio local si el backend está desconectado
        const req = this.mockRequests.find(r => r.id === id);
        if (!req) return throwError(() => new Error('Trámite no encontrado'));

        if (payload.status === 'EN_TERRENO' && req.status !== 'ADMITIDO' && req.status !== 'EN_GESTION') {
          return throwError(() => new Error('Regla de negocio: No se puede pasar a EN_TERRENO sin haber sido ADMITIDO primero.'));
        }

        req.status = payload.status as any;
        if (payload.assignedCrew) req.assignedCrew = payload.assignedCrew;
        if (payload.resolutionNotes) req.resolutionNotes = payload.resolutionNotes;
        req.updatedAt = new Date().toISOString();

        return of(req);
      })
    );
  }
}
