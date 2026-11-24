import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, of } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ProcedureType {
  id: number;
  code: string;
  name: string;
  description: string;
  dailyQuota: number;
  availableQuota: number;
  requirements: string;
  active: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class CatalogService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiGatewayUrl}/api/catalog/procedures`;

  private mockProcedures: ProcedureType[] = [
    {
      id: 1,
      code: 'PODA_ARBOLES',
      name: 'Poda de Árboles en Vía Pública',
      description: 'Poda de despeje por cables eléctricos, ramas en riesgo de caída o visibilidad vial.',
      dailyQuota: 10,
      availableQuota: 7,
      requirements: 'Cédula de identidad, foto del árbol, dirección exacta',
      active: true
    },
    {
      id: 2,
      code: 'LUMINARIA_PUBLICA',
      name: 'Reparación de Luminaria Pública',
      description: 'Reporte de focos apagados, intermitentes o postes dañados en avenidas o pasajes.',
      dailyQuota: 15,
      availableQuota: 12,
      requirements: 'Número de poste o dirección cercana, fotografía de referencia',
      active: true
    },
    {
      id: 3,
      code: 'BACHEO_CALLES',
      name: 'Bacheo y Reparación de Calzadas',
      description: 'Reparación de hoyos en pavimento o veredas comunales que representen peligro.',
      dailyQuota: 5,
      availableQuota: 2,
      requirements: 'Ubicación precisa, fotos de la calzada o vereda afectada',
      active: true
    },
    {
      id: 4,
      code: 'RETIRO_ESCOMBROS',
      name: 'Retiro de Escombros y Enseres',
      description: 'Servicio municipal para coordinar tolvas o camión de recolección de enseres voluminosos.',
      dailyQuota: 8,
      availableQuota: 5,
      requirements: 'Comprobante de domicilio, listado de artículos a retirar',
      active: true
    },
    {
      id: 5,
      code: 'CONTROL_PLAGAS',
      name: 'Fumigación y Control de Plagas',
      description: 'Intervención sanitaria en espacios públicos, plazas o zanjas comunales.',
      dailyQuota: 6,
      availableQuota: 4,
      requirements: 'Ubicación, descripción del foco detectado',
      active: true
    }
  ];

  getProcedures(): Observable<ProcedureType[]> {
    return this.http.get<ProcedureType[]>(this.apiUrl).pipe(
      catchError(() => of(this.mockProcedures))
    );
  }

  getProcedureById(id: number): Observable<ProcedureType | undefined> {
    return this.http.get<ProcedureType>(`${this.apiUrl}/${id}`).pipe(
      catchError(() => of(this.mockProcedures.find(p => p.id === id)))
    );
  }

  createProcedure(procedure: Partial<ProcedureType>): Observable<ProcedureType> {
    return this.http.post<ProcedureType>(this.apiUrl, procedure).pipe(
      catchError(() => {
        const newProc: ProcedureType = {
          id: Date.now(),
          code: procedure.code || 'PROC_' + Date.now(),
          name: procedure.name || 'Nuevo Trámite',
          description: procedure.description || '',
          dailyQuota: procedure.dailyQuota || 10,
          availableQuota: procedure.dailyQuota || 10,
          requirements: procedure.requirements || '',
          active: true
        };
        this.mockProcedures.push(newProc);
        return of(newProc);
      })
    );
  }

  updateProcedure(id: number, procedure: Partial<ProcedureType>): Observable<ProcedureType> {
    return this.http.put<ProcedureType>(`${this.apiUrl}/${id}`, procedure).pipe(
      catchError(() => {
        const index = this.mockProcedures.findIndex(p => p.id === id);
        if (index !== -1) {
          this.mockProcedures[index] = { ...this.mockProcedures[index], ...procedure };
          return of(this.mockProcedures[index]);
        }
        throw new Error('Not found');
      })
    );
  }
}
