import { Injectable, inject } from '@angular/core';
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest
} from '@angular/common/http';
import { Observable, from, of } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private authService = inject(AuthService);

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Solo interceptamos llamadas dirigidas a las APIs del BFF o backend (/api/)
    if (!req.url.includes('/api/')) {
      return next.handle(req);
    }

    const currentUser = this.authService.getCurrentUser();
    if (!currentUser) {
      return next.handle(req);
    }

    // 1. Usuario con perfil Demo (Evaluación Docente): inyectar token demo sin invocar popups de Azure
    if (currentUser.isDemoUser) {
      const demoToken = `demo-token-${currentUser.roles.join('-')}`;
      const clonedReq = req.clone({
        setHeaders: {
          'Authorization': `Bearer ${demoToken}`,
          'X-User-Roles': currentUser.roles.join(','),
          'X-User-Name': currentUser.name,
          'X-User-Email': currentUser.email
        }
      });
      return next.handle(clonedReq);
    }

    // 2. Usuario autenticado con Azure AD: intentar obtener token silencioso sin abrir popups molestos
    return from(this.authService.getAccessToken()).pipe(
      switchMap((token: string | null) => {
        if (token) {
          const authorizedReq = req.clone({
            setHeaders: {
              'Authorization': `Bearer ${token}`
            }
          });
          return next.handle(authorizedReq);
        }
        return next.handle(req);
      }),
      catchError((err: any) => {
        console.warn('No se pudo adquirir token de Azure AD silenciosamente:', err);
        return next.handle(req);
      })
    );
  }
}
