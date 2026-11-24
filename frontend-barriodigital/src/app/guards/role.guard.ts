import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Guard que verifica si el usuario autenticado posee al menos uno de los roles
 * especificados en la data de la ruta (e.g. data: { roles: ['Admin', 'Funcionario'] }).
 */
export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const currentUser = authService.getCurrentUser();
  if (!currentUser) {
    router.navigate(['/login']);
    return false;
  }

  const expectedRoles = route.data['roles'] as Array<string>;
  if (!expectedRoles || expectedRoles.length === 0) {
    return true;
  }

  const hasPermission = authService.hasAnyRole(expectedRoles);
  if (!hasPermission) {
    alert(`Acceso denegado: Se requiere uno de los siguientes roles: ${expectedRoles.join(', ')}. Tu rol actual es: ${currentUser.roles.join(', ')}`);
    router.navigate(['/dashboard']);
    return false;
  }

  return true;
};
