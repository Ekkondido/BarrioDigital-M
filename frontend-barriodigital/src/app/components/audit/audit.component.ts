import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuditService, AuditEvent } from '../../services/audit.service';

@Component({
  selector: 'app-audit',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './audit.component.html',
  styleUrls: ['./audit.component.css']
})
export class AuditComponent implements OnInit {
  private auditService = inject(AuditService);

  events: AuditEvent[] = [];
  filteredEvents: AuditEvent[] = [];
  selectedEvent: AuditEvent | null = null;
  showDetailModal = false;

  filterActor: string = '';
  filterRequestId: string = '';

  ngOnInit(): void {
    this.loadEvents();
  }

  loadEvents(): void {
    this.auditService.getAuditEvents().subscribe(evts => {
      this.events = evts;
      this.applyFilter();
    });
  }

  applyFilter(): void {
    let list = [...this.events];
    if (this.filterActor.trim()) {
      list = list.filter(e => e.actor.toLowerCase().includes(this.filterActor.toLowerCase()));
    }
    if (this.filterRequestId.trim()) {
      list = list.filter(e =>
        e.trackingNumber.toLowerCase().includes(this.filterRequestId.toLowerCase()) ||
        String(e.requestId).includes(this.filterRequestId)
      );
    }
    this.filteredEvents = list;
  }

  openDetail(event: AuditEvent): void {
    this.selectedEvent = event;
    this.showDetailModal = true;
  }
}
