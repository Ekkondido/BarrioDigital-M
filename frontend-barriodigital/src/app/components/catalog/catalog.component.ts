import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { CatalogService, ProcedureType } from '../../services/catalog.service';

@Component({
  selector: 'app-catalog',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './catalog.component.html',
  styleUrls: ['./catalog.component.css']
})
export class CatalogComponent implements OnInit {
  authService = inject(AuthService);
  private catalogService = inject(CatalogService);

  procedures: ProcedureType[] = [];
  showModal = false;
  isEditing = false;

  formData: Partial<ProcedureType> = {
    code: '',
    name: '',
    description: '',
    dailyQuota: 10,
    requirements: ''
  };

  ngOnInit(): void {
    this.loadProcedures();
  }

  loadProcedures(): void {
    this.catalogService.getProcedures().subscribe(procs => {
      this.procedures = procs;
    });
  }

  openCreateModal(): void {
    this.isEditing = false;
    this.formData = {
      code: '',
      name: '',
      description: '',
      dailyQuota: 10,
      requirements: ''
    };
    this.showModal = true;
  }

  openEditModal(proc: ProcedureType): void {
    this.isEditing = true;
    this.formData = { ...proc };
    this.showModal = true;
  }

  saveProcedure(): void {
    if (!this.formData.name || !this.formData.code || !this.formData.dailyQuota) {
      alert('Por favor completa todos los campos requeridos.');
      return;
    }

    if (this.isEditing && this.formData.id) {
      this.catalogService.updateProcedure(this.formData.id, this.formData).subscribe(() => {
        alert('Trámite actualizado con éxito.');
        this.showModal = false;
        this.loadProcedures();
      });
    } else {
      this.catalogService.createProcedure(this.formData).subscribe(() => {
        alert('Nuevo trámite agregado al catálogo comunal.');
        this.showModal = false;
        this.loadProcedures();
      });
    }
  }
}
