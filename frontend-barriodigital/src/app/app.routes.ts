import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { RequestsComponent } from './components/requests/requests.component';
import { CatalogComponent } from './components/catalog/catalog.component';
import { ReportsComponent } from './components/reports/reports.component';
import { AuditComponent } from './components/audit/audit.component';
import { AdminMqComponent } from './components/admin-mq/admin-mq.component';
import { roleGuard } from './guards/role.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [roleGuard],
    data: { roles: ['Admin', 'Funcionario', 'Vecino', 'Auditor'] }
  },
  {
    path: 'requests',
    component: RequestsComponent,
    canActivate: [roleGuard],
    data: { roles: ['Admin', 'Funcionario', 'Vecino', 'Auditor'] }
  },
  {
    path: 'catalog',
    component: CatalogComponent,
    canActivate: [roleGuard],
    data: { roles: ['Admin', 'Funcionario'] }
  },
  {
    path: 'reports',
    component: ReportsComponent,
    canActivate: [roleGuard],
    data: { roles: ['Admin'] }
  },
  {
    path: 'audit',
    component: AuditComponent,
    canActivate: [roleGuard],
    data: { roles: ['Admin', 'Auditor'] }
  },
  {
    path: 'admin/mq',
    component: AdminMqComponent,
    canActivate: [roleGuard],
    data: { roles: ['Admin'] }
  },
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: '**', redirectTo: 'dashboard' }
];
