import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminMqService, QueueInfo, QueueCreatePayload } from '../../services/admin-mq.service';

@Component({
  selector: 'app-admin-mq',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-mq.component.html',
  styleUrls: ['./admin-mq.component.css']
})
export class AdminMqComponent implements OnInit {
  private mqService = inject(AdminMqService);

  queues: QueueInfo[] = [];
  loading = false;

  // Modal Crear Cola
  showCreateModal = false;
  newQueue: QueueCreatePayload = {
    name: '',
    durable: true,
    autoDelete: false,
    deadLetterExchange: 'cmd.dead.dlx',
    deadLetterRoutingKey: ''
  };
  createError: string | null = null;

  // Pruebas de Mensajes y DLQ
  testExchange = 'cmd.direct';
  testRoutingKey = 'email.send';
  testPayloadText = '{"asunto": "Aviso Comunal", "mensaje": "Su solicitud ha sido recibida con éxito"}';
  simulateError = false;
  testResult: string | null = null;

  ngOnInit(): void {
    this.loadQueues();
  }

  loadQueues(): void {
    this.loading = true;
    this.mqService.getQueues().subscribe({
      next: (data) => {
        this.queues = data;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  openCreateModal(): void {
    this.createError = null;
    this.newQueue = {
      name: '',
      durable: true,
      autoDelete: false,
      deadLetterExchange: 'cmd.dead.dlx',
      deadLetterRoutingKey: ''
    };
    this.showCreateModal = true;
  }

  submitCreateQueue(): void {
    this.createError = null;
    if (!this.newQueue.name || this.newQueue.name.trim().length < 3) {
      this.createError = 'El nombre de la cola es obligatorio y debe tener al menos 3 caracteres.';
      return;
    }

    const pattern = /^[a-zA-Z0-9._-]+$/;
    if (!pattern.test(this.newQueue.name)) {
      this.createError = 'El nombre solo puede contener letras, números, puntos, guiones y guiones bajos.';
      return;
    }

    this.mqService.createQueue(this.newQueue).subscribe({
      next: (created) => {
        alert(`Cola '${created.name}' creada exitosamente en RabbitMQ.`);
        this.showCreateModal = false;
        this.loadQueues();
      },
      error: (err) => {
        this.createError = err.message || 'Error creando cola en RabbitMQ.';
      }
    });
  }

  deleteQueue(queue: QueueInfo): void {
    if (!confirm(`¿Está seguro de eliminar la cola '${queue.name}' de RabbitMQ?`)) {
      return;
    }

    this.mqService.deleteQueue(queue.name).subscribe({
      next: () => {
        alert(`Cola '${queue.name}' eliminada correctamente.`);
        this.loadQueues();
      },
      error: (err) => alert('Error al eliminar cola: ' + (err.message || err))
    });
  }

  purgeQueue(queue: QueueInfo): void {
    this.mqService.purgeQueue(queue.name).subscribe({
      next: () => {
        alert(`Cola '${queue.name}' purgada exitosamente.`);
        this.loadQueues();
      }
    });
  }

  sendTest(): void {
    this.testResult = null;
    try {
      let parsed = JSON.parse(this.testPayloadText);
      if (this.simulateError) {
        parsed.simulate_failure = true;
      }

      this.mqService.sendTestMessage(this.testExchange, this.testRoutingKey, parsed).subscribe({
        next: () => {
          if (this.simulateError) {
            this.testResult = '✓ Mensaje con error simulado publicado. El consumidor ejecutará basicNack y RabbitMQ lo derivará a la DLQ correspondiente.';
          } else {
            this.testResult = '✓ Mensaje publicado con éxito en exchange ' + this.testExchange + ' [' + this.testRoutingKey + ']. El consumidor ejecutará basicAck.';
          }
          setTimeout(() => this.loadQueues(), 1000);
        },
        error: (err) => {
          this.testResult = '✗ Error publicando mensaje: ' + (err.message || err);
        }
      });
    } catch {
      alert('El payload debe ser un JSON válido.');
    }
  }
}
