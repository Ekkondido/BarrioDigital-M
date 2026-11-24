import { Injectable, inject } from '@angular/core';
import { MsalService, MsalBroadcastService } from '@azure/msal-angular';
import { EventMessage, EventType, InteractionStatus, AuthenticationResult, AccountInfo } from '@azure/msal-browser';
import { BehaviorSubject, Observable, finalize, filter, tap } from 'rxjs';
import { environment } from '../../environments/environment';

export interface UserProfile {
  name: string;
  email: string;
  username: string;
  roles: string[];
  isDemoUser?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly STORAGE_KEY = 'barriodigital_user';
  private msalService = inject(MsalService);
  private msalBroadcastService = inject(MsalBroadcastService);

  private currentUserSubject = new BehaviorSubject<UserProfile | null>(this.loadStoredUser());
  public currentUser$ = this.currentUserSubject.asObservable();

  private isAuthenticatingSubject = new BehaviorSubject<boolean>(false);
  public isAuthenticating$ = this.isAuthenticatingSubject.asObservable();

  private cachedToken: string | null = null;
  private tokenExpiresAt: number = 0;
  private tokenPromise: Promise<string | null> | null = null;

  constructor() {
    this.initMsalListener();
    this.checkCurrentAccount();
  }

  private loadStoredUser(): UserProfile | null {
    try {
      if (typeof window !== 'undefined' && window.localStorage) {
        const stored = localStorage.getItem(this.STORAGE_KEY);
        if (stored) {
          return JSON.parse(stored);
        }
      }
    } catch (e) {
      console.warn('Error al leer usuario de localStorage:', e);
    }
    return null;
  }

  private setCurrentUser(profile: UserProfile | null): void {
    this.currentUserSubject.next(profile);
    try {
      if (typeof window !== 'undefined' && window.localStorage) {
        if (profile) {
          localStorage.setItem(this.STORAGE_KEY, JSON.stringify(profile));
        } else {
          localStorage.removeItem(this.STORAGE_KEY);
        }
      }
    } catch (e) {
      console.warn('Error al persistir usuario en localStorage:', e);
    }
  }

  private initMsalListener(): void {
    this.msalBroadcastService.msalSubject$
      .pipe(
        filter((msg: EventMessage) =>
          msg.eventType === EventType.LOGIN_SUCCESS ||
          msg.eventType === EventType.ACQUIRE_TOKEN_SUCCESS
        )
      )
      .subscribe((result: EventMessage) => {
        const payload = result.payload as AuthenticationResult;
        if (payload && payload.account) {
          this.msalService.instance.setActiveAccount(payload.account);
          this.extractAndSetUser(payload.account);
        }
      });

    this.msalBroadcastService.inProgress$
      .pipe(filter((status: InteractionStatus) => status === InteractionStatus.None))
      .subscribe(() => {
        this.checkCurrentAccount();
      });
  }

  public checkCurrentAccount(): void {
    // Si ya hay un usuario demo activo, no sobreescribirlo
    if (this.currentUserSubject.value?.isDemoUser) {
      return;
    }

    try {
      const activeAccount = this.msalService.instance.getActiveAccount();
      if (activeAccount) {
        this.extractAndSetUser(activeAccount);
      } else {
        const accounts = this.msalService.instance.getAllAccounts();
        if (accounts.length > 0) {
          this.msalService.instance.setActiveAccount(accounts[0]);
          this.extractAndSetUser(accounts[0]);
        }
      }
    } catch (err) {
      console.warn('MSAL checkCurrentAccount handled:', err);
    }
  }

  private extractAndSetUser(account: AccountInfo): void {
    const idTokenClaims = account.idTokenClaims as Record<string, any> || {};
    // Los roles en Azure AD se devuelven en el claim 'roles'
    let roles: string[] = [];
    if (Array.isArray(idTokenClaims['roles'])) {
      roles = idTokenClaims['roles'];
    } else if (typeof idTokenClaims['roles'] === 'string') {
      roles = [idTokenClaims['roles']];
    } else {
      // Si no tiene roles asignados aun en Azure, por defecto Vecino
      roles = ['Vecino'];
    }

    const profile: UserProfile = {
      name: account.name || account.username || 'Usuario',
      email: account.username,
      username: account.username,
      roles: roles,
      isDemoUser: false
    };

    this.setCurrentUser(profile);
  }

  public login(): Observable<AuthenticationResult> {
    this.isAuthenticatingSubject.next(true);

    const isLocal = typeof window !== 'undefined' && (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1');
    const redirectUri = isLocal ? 'http://localhost:4200/' : 'https://50.16.132.25/';

    return this.msalService.loginPopup({
      scopes: ['user.read', environment.azureAd.apiScope],
      redirectUri: redirectUri
    }).pipe(
      tap((result: AuthenticationResult) => {
        this.msalService.instance.setActiveAccount(result.account);
        this.extractAndSetUser(result.account);
      }),
      finalize(() => this.isAuthenticatingSubject.next(false))
    );
  }

  public logout(): void {
    this.cachedToken = null;
    this.tokenExpiresAt = 0;
    this.setCurrentUser(null);
    try {
      const activeAccount = this.msalService.instance.getActiveAccount();
      if (activeAccount) {
        this.msalService.logoutPopup({
          postLogoutRedirectUri: environment.azureAd.postLogoutRedirectUri
        }).subscribe();
      }
    } catch (e) {
      console.warn('MSAL logoutPopup handled:', e);
    }
  }

  public async getAccessToken(): Promise<string | null> {
    const current = this.currentUserSubject.value;
    if (current?.isDemoUser) {
      return 'demo-token-' + current.roles.join('-');
    }

    if (this.cachedToken && Date.now() < this.tokenExpiresAt - 60000) {
      return this.cachedToken;
    }

    if (this.tokenPromise) {
      return this.tokenPromise;
    }

    this.tokenPromise = this.fetchToken();
    try {
      const token = await this.tokenPromise;
      return token;
    } finally {
      this.tokenPromise = null;
    }
  }

  private async fetchToken(): Promise<string | null> {
    const account = this.msalService.instance.getActiveAccount();
    if (!account) {
      console.warn('[AuthService] getAccessToken: no hay cuenta activa en MSAL');
      return null;
    }

    // 1er intento: access token para el scope de la API
    try {
      const response = await this.msalService.instance.acquireTokenSilent({
        account: account,
        scopes: [environment.azureAd.apiScope]
      });
      if (response.accessToken) {
        this.cachedToken = response.accessToken;
        this.tokenExpiresAt = response.expiresOn ? response.expiresOn.getTime() : Date.now() + 3600000;
        console.log('[AuthService] Access token para API obtenido correctamente');
        return response.accessToken;
      }
    } catch (err) {
      console.warn('[AuthService] acquireTokenSilent (API scope) falló:', err);
    }

    // 2do intento: token para openid/profile/email (idToken fallback)
    try {
      const response = await this.msalService.instance.acquireTokenSilent({
        account: account,
        scopes: ['openid', 'profile', 'email']
      });
      if (response.idToken) {
        this.cachedToken = response.idToken;
        this.tokenExpiresAt = response.expiresOn ? response.expiresOn.getTime() : Date.now() + 3600000;
        console.log('[AuthService] Usando idToken como fallback para autorización');
        return response.idToken;
      }
    } catch (err2) {
      console.warn('[AuthService] acquireTokenSilent (openid) falló:', err2);
    }

    // 3er intento: idToken guardado en account
    const rawIdToken = (account as any).idToken;
    if (rawIdToken) {
      this.cachedToken = rawIdToken;
      this.tokenExpiresAt = Date.now() + 3600000;
      console.log('[AuthService] Usando idToken del account cache');
      return rawIdToken;
    }

    console.error('[AuthService] No se pudo obtener ningún token para el usuario de Azure AD');
    return null;
  }

  public hasRole(role: string): boolean {
    const user = this.currentUserSubject.value;
    if (!user) return false;
    return user.roles.some(r => r.toLowerCase() === role.toLowerCase());
  }

  public hasAnyRole(roles: string[]): boolean {
    return roles.some(role => this.hasRole(role));
  }

  public getCurrentUser(): UserProfile | null {
    return this.currentUserSubject.value;
  }

  /**
   * Permite alternar roles para revision docente y pruebas funcionales completas
   */
  public setDemoRole(role: 'Admin' | 'Funcionario' | 'Vecino' | 'Auditor'): void {
    const demoProfiles: Record<string, UserProfile> = {
      Admin: {
        name: 'Carlos Mendoza (Administrador)',
        email: 'carlos.admin@barriodigital.cl',
        username: 'carlos.admin',
        roles: ['Admin'],
        isDemoUser: true
      },
      Funcionario: {
        name: 'Mariana González (Funcionario)',
        email: 'mariana.operaciones@barriodigital.cl',
        username: 'mariana.funcionario',
        roles: ['Funcionario'],
        isDemoUser: true
      },
      Vecino: {
        name: 'Roberto Salgado (Vecino)',
        email: 'roberto.salgado@gmail.com',
        username: 'roberto.vecino',
        roles: ['Vecino'],
        isDemoUser: true
      },
      Auditor: {
        name: 'Verónica Valenzuela (Auditora)',
        email: 'veronica.auditoria@barriodigital.cl',
        username: 'veronica.auditor',
        roles: ['Auditor'],
        isDemoUser: true
      }
    };

    const profile = demoProfiles[role];
    if (profile) {
      this.setCurrentUser(profile);
    }
  }
}
