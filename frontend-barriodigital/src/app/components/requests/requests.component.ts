import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { RequestsService, CitizenRequest, CreateRequestPayload, UpdateStatusPayload } from '../../services/requests.service';
import { CatalogService, ProcedureType } from '../../services/catalog.service';

@Component({
  selector: 'app-requests',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './requests.component.html',
  styleUrls: ['./requests.component.css']
})
export class RequestsComponent implements OnInit {
  authService = inject(AuthService);
  private requestsService = inject(RequestsService);
  private catalogService = inject(CatalogService);

  requests: CitizenRequest[] = [];
  filteredRequests: CitizenRequest[] = [];
  procedures: ProcedureType[] = [];

  selectedStatusFilter: string = 'ALL';
  searchTerm: string = '';

  // Modal Crear
  showCreateModal = false;
  newRequest: CreateRequestPayload = {
    procedureId: 1,
    procedureName: '',
    citizenName: '',
    citizenEmail: '',
    citizenPhone: '',
    address: '',
    description: ''
  };

  // Modal Cambiar Estado
  showStatusModal = false;
  selectedRequest: CitizenRequest | null = null;
  statusPayload: UpdateStatusPayload = {
    status: 'ADMITIDO',
    assignedCrew: '',
    resolutionNotes: ''
  };
  errorMessage: string | null = null;

  ngOnInit(): void {
    this.loadProcedures();
    this.loadRequests();
  }

  loadProcedures(): void {
    this.catalogService.getProcedures().subscribe(procs => {
      this.procedures = procs;
      if (procs.length > 0) {
        this.newRequest.procedureId = procs[0].id;
        this.newRequest.procedureName = procs[0].name;
      }
    });
  }

  loadRequests(): void {
    this.requestsService.getRequests().subscribe(reqs => {
      this.requests = reqs;
      this.applyFilter();
    });
  }

  applyFilter(): void {
    let list = [...this.requests];
    if (this.selectedStatusFilter !== 'ALL') {
      list = list.filter(r => r.status === this.selectedStatusFilter);
    }
    if (this.searchTerm.trim() !== '') {
      const term = this.searchTerm.toLowerCase();
      list = list.filter(r =>
        r.trackingNumber.toLowerCase().includes(term) ||
        r.procedureName.toLowerCase().includes(term) ||
        r.citizenName.toLowerCase().includes(term) ||
        r.address.toLowerCase().includes(term)
      );
    }
    this.filteredRequests = list;
  }

  openCreateModal(): void {
    const user = this.authService.getCurrentUser();
    this.newRequest = {
      procedureId: this.procedures.length > 0 ? this.procedures[0].id : 1,
      procedureName: this.procedures.length > 0 ? this.procedures[0].name : '',
      citizenName: user ? user.name : '',
      citizenEmail: user ? user.email : '',
      citizenPhone: '',
      address: '',
      description: ''
    };
    this.showCreateModal = true;
  }

  onProcedureSelect(procId: any): void {
    const id = Number(procId);
    const found = this.procedures.find(p => p.id === id);
    if (found) {
      this.newRequest.procedureId = found.id;
      this.newRequest.procedureName = found.name;
    }
  }

  submitCreate(): void {
    if (!this.newRequest.address || !this.newRequest.description) {
      alert('Por favor completa la dirección y descripción del requerimiento.');
      return;
    }

    this.requestsService.createRequest(this.newRequest).subscribe({
      next: (created) => {
        alert(`¡Trámite ${created.trackingNumber} ingresado con éxito! Se envió notificación a la cola de mensajería.`);
        this.showCreateModal = false;
        this.loadRequests();
      },
      error: (err) => {
        alert('Error al crear trámite: ' + (err.message || err));
      }
    });
  }

  openStatusModal(req: CitizenRequest): void {
    this.selectedRequest = req;
    this.errorMessage = null;

    // Sugerir el siguiente estado natural en el flujo comunal
    let nextStatus = 'ADMITIDO';
    if (req.status === 'INGRESADO') nextStatus = 'ADMITIDO';
    else if (req.status === 'ADMITIDO') nextStatus = 'EN_GESTION';
    else if (req.status === 'EN_GESTION') nextStatus = 'EN_TERRENO';
    else if (req.status === 'EN_TERRENO') nextStatus = 'RESUELTO';

    this.statusPayload = {
      status: nextStatus,
      assignedCrew: req.assignedCrew || '',
      resolutionNotes: req.resolutionNotes || ''
    };
    this.showStatusModal = true;
  }

  submitStatusChange(): void {
    if (!this.selectedRequest) return;
    this.errorMessage = null;

    // Regla clave: No se puede pasar a EN_TERRENO sin ADMITIR
    if (this.statusPayload.status === 'EN_TERRENO' &&
        this.selectedRequest.status !== 'ADMITIDO' &&
        this.selectedRequest.status !== 'EN_GESTION') {
      this.errorMessage = '⚠️ Regla de Negocio del Caso: No se puede pasar a EN_TERRENO sin haber sido previamente ADMITIDO o puesto EN_GESTIÓN.';
      return;
    }

    if (this.statusPayload.status === 'EN_TERRENO' && !this.statusPayload.assignedCrew?.trim()) {
      this.statusPayload.assignedCrew = 'Cuadrilla Comunal de Guardia';
    }

    this.requestsService.updateStatus(this.selectedRequest.id, this.statusPayload).subscribe({
      next: (updated) => {
        alert(`Estado actualizado a ${updated.status}. Evento publicado a Kafka y RabbitMQ.`);
        this.showStatusModal = false;
        this.loadRequests();
      },
      error: (err) => {
        this.errorMessage = err.message || 'Error al actualizar el estado.';
      }
    });
  }
}
