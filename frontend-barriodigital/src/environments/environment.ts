export const environment = {
  production: false,
  // Azure AD (Microsoft Entra ID) - Configuración para evaluación y nube
  azureAd: {
    clientId: 'bbf3d1bd-ee73-4f0f-8efc-90a3688a1199', // Reemplazar con el Client ID de Azure Portal
    tenantId: '9544c211-793b-440b-98a8-cc363e1a04cb', // Reemplazar con el Tenant ID (Directory ID) de Azure Portal
    redirectUri: 'http://localhost:4200/',
    postLogoutRedirectUri: 'http://localhost:4200/login',
    apiScope: 'api://3c53448e-df26-462e-912f-f8acda44d1a4/access_as_user'
  },
  // En entorno desplegado o Nginx se usa ruta relativa '' que proxia hacia el BFF
  apiGatewayUrl: (typeof window !== 'undefined' && window.location.port !== '4200') ? '' : 'http://localhost:8080'
};
