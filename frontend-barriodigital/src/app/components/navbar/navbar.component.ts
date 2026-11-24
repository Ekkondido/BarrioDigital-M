import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
  authService = inject(AuthService);
  router = inject(Router);

  currentUser$ = this.authService.currentUser$;

  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  setDemoRole(role: 'Admin' | 'Funcionario' | 'Vecino' | 'Auditor'): void {
    this.authService.setDemoRole(role);
  }
}
