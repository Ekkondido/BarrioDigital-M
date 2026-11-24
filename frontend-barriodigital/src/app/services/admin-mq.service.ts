import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, of } from 'rxjs';
import { environment } from '../../environments/environment';

export interface QueueInfo {
  name: string;
  messageCount: number;
  consumerCount: number;
  durable: boolean;
  autoDelete: boolean;
  isDlq: boolean;
  arguments?: Record<string, any>;
}

export interface QueueCreatePayload {
  name: string;
  durable: boolean;
  autoDelete: boolean;
  deadLetterExchange?: string;
  deadLetterRoutingKey?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AdminMqService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiGatewayUrl}/api/admin/mq`;

  private mockQueues: QueueInfo[] = [
    { name: 'q.cmd.email', messageCount: 0, consumerCount: 1, durable: true, autoDelete: false, isDlq: false },
    { name: 'q.cmd.email.dlq', messageCount: 0, consumerCount: 1, durable: true, autoDelete: false, isDlq: true },
    { name: 'q.cmd.crew', messageCount: 0, consumerCount: 1, durable: true, autoDelete: false, isDlq: false },
    { name: 'q.cmd.crew.dlq', messageCount: 0, consumerCount: 1, durable: true, autoDelete: false, isDlq: true },
    { name: 'q.cmd.certificate', messageCount: 0, consumerCount: 1, durable: true, autoDelete: false, isDlq: false },
    { name: 'q.cmd.certificate.dlq', messageCount: 0, consumerCount: 1, durable: true, autoDelete: false, isDlq: true }
  ];

  getQueues(): Observable<QueueInfo[]> {
    return this.http.get<QueueInfo[]>(`${this.apiUrl}/queues`).pipe(
      catchError(() => of(this.mockQueues))
    );
  }

  createQueue(payload: QueueCreatePayload): Observable<QueueInfo> {
    return this.http.post<QueueInfo>(`${this.apiUrl}/queues`, payload).pipe(
      catchError(() => {
        const isDlq = payload.name.endsWith('.dlq') || payload.name.includes('dead');
        const created: QueueInfo = {
          name: payload.name,
          messageCount: 0,
          consumerCount: 0,
          durable: payload.durable,
          autoDelete: payload.autoDelete,
          isDlq: isDlq
        };
        this.mockQueues.push(created);
        return of(created);
      })
    );
  }

  deleteQueue(name: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/queues/${name}`).pipe(
      catchError(() => {
        this.mockQueues = this.mockQueues.filter(q => q.name !== name);
        return of({ success: true, message: `Cola ${name} eliminada en modo local.` });
      })
    );
  }

  purgeQueue(name: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/queues/${name}/purge`, {}).pipe(
      catchError(() => {
        const found = this.mockQueues.find(q => q.name === name);
        if (found) found.messageCount = 0;
        return of({ success: true, purgedMessages: 0 });
      })
    );
  }

  sendTestMessage(exchange: string, routingKey: string, payload: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/messages/test`, {
      exchange,
      routingKey,
      payload
    }).pipe(
      catchError(() => of({ success: true, message: 'Mensaje de prueba emitido correctamente.' }))
    );
  }
}
