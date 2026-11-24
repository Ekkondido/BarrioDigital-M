import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  authService = inject(AuthService);
  router = inject(Router);

  isAuthenticating$ = this.authService.isAuthenticating$;
  env = environment;

  onMicrosoftLogin(): void {
    this.authService.login().subscribe({
      next: () => {
        void this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        console.error('Error durante autenticacion con Azure AD MSAL:', err);
      }
    });
  }

  onDemoLogin(role: 'Admin' | 'Funcionario' | 'Vecino' | 'Auditor'): void {
    this.authService.setDemoRole(role);
    this.router.navigate(['/dashboard']);
  }
}
