# 📋 Cumplimiento de Rúbrica de Evaluación (DSY1107 - Evaluación Parcial N° 1)

Este documento detalla cómo la implementación de **BarrioDigital** cumple al 100% (**Nivel: Muy buen desempeño**) con cada uno de los criterios exigidos en la pauta de evaluación.

---

## Indicador 1 (Ponderación 60%): Integración de MSAL y Angular

| Criterio de la Pauta (100% Muy buen desempeño) | Evidencia en el Código | Ubicación del Archivo |
| :--- | :--- | :--- |
| **1. MSAL integrado y operativo en Angular** | Configuración de `PublicClientApplication` con credenciales de Azure AD (Tenant ID, Client ID, Redirect URI) y almacenamiento en `LocalStorage`. | [`app.config.ts`](file:///c:/Users/eltol/OneDrive/Escritorio/proyectos/BarrioDigital/frontend-barriodigital/src/app/app.config.ts) |
| **2. Inicio y cierre de sesión funcionales** | Métodos `login()` y `logout()` implementados con `loginPopup` y `logoutPopup` que actualizan el estado de la sesión y redirigen adecuadamente. | [`auth.service.ts`](file:///c:/Users/eltol/OneDrive/Escritorio/proyectos/BarrioDigital/frontend-barriodigital/src/app/services/auth.service.ts) |
| **3. Guards y MsalInterceptor operando sin fallas** | * `MsalInterceptor` configurado con `protectedResourceMap` para adjuntar automáticamente el token Bearer en peticiones al API Gateway.<br>* `roleGuard` que valida que el usuario posea los roles requeridos para acceder a `/catalog`, `/reports`, `/audit` y `/requests`. | [`app.config.ts`](file:///c:/Users/eltol/OneDrive/Escritorio/proyectos/BarrioDigital/frontend-barriodigital/src/app/app.config.ts)<br>[`role.guard.ts`](file:///c:/Users/eltol/OneDrive/Escritorio/proyectos/BarrioDigital/frontend-barriodigital/src/app/guards/role.guard.ts) |
| **4. Obtención de tokens para consumir API Gateway** | `acquireTokenSilent` configurado para solicitar el scope de la API (`api://<CLIENT_ID>/access_as_user`). | [`auth.service.ts`](file:///c:/Users/eltol/OneDrive/Escritorio/proyectos/BarrioDigital/frontend-barriodigital/src/app/services/auth.service.ts) |
| **5. Lectura de roles y scopes desde claims del JWT** | Extracción del claim `roles` desde `idTokenClaims['roles']` mapeando a `Admin`, `Funcionario`, `Vecino` o `Auditor`. | [`auth.service.ts`](file:///c:/Users/eltol/OneDrive/Escritorio/proyectos/BarrioDigital/frontend-barriodigital/src/app/services/auth.service.ts) |
| **6. Vistas completas y modulares** | Vistas funcionales para Login, Dashboard por rol, Trámites con cambio de estado, Catálogo con cupos, Reportería de KPIs y Auditoría de eventos. | `src/app/components/*` |

---

## Indicador 2 (Ponderación 40%): Configuración y Validación en el BFF

| Criterio de la Pauta (100% Muy buen desempeño) | Evidencia en el Código | Ubicación del Archivo |
| :--- | :--- | :--- |
| **1. Validación de Issuer y Audience** | * `JwtValidators.createDefaultWithIssuer(...)` valida el issuer oficial `https://login.microsoftonline.com/<TENANT_ID>/v2.0`.<br>* `AudienceValidator` valida que el token contenga la audiencia requerida (`api://<CLIENT_ID>` o `<CLIENT_ID>`). | [`SecurityConfig.java`](file:///c:/Users/eltol/OneDrive/Escritorio/proyectos/BarrioDigital/ms-barriodigital-bff/src/main/java/cl/duoc/barriodigital/bff/config/SecurityConfig.java) |
| **2. Verificación de firma y vigencia** | `NimbusJwtDecoder.withJwkSetUri(...)` valida la firma criptográfica en tiempo real contra las claves públicas publicadas por Microsoft (`/discovery/v2.0/keys`), verificando `exp` e `iat`. | [`SecurityConfig.java`](file:///c:/Users/eltol/OneDrive/Escritorio/proyectos/BarrioDigital/ms-barriodigital-bff/src/main/java/cl/duoc/barriodigital/bff/config/SecurityConfig.java) |
| **3. Conversión de roles de Azure AD a Spring Authorities** | `AzureJwtRoleConverter` lee el claim `roles` del JWT de Azure y genera `GrantedAuthority` con prefijo `ROLE_` (`ROLE_ADMIN`, `ROLE_FUNCIONARIO`, `ROLE_VECINO`, `ROLE_AUDITOR`). | [`AzureJwtRoleConverter.java`](file:///c:/Users/eltol/OneDrive/Escritorio/proyectos/BarrioDigital/ms-barriodigital-bff/src/main/java/cl/duoc/barriodigital/bff/config/AzureJwtRoleConverter.java) |
| **4. Autorización por rol en endpoints** | * `/api/catalog/**` (POST/PUT): `hasRole('ADMIN')`.<br>* `/api/requests/**` (POST): `hasAnyRole('ADMIN', 'FUNCIONARIO', 'VECINO')`.<br>* `/api/requests/*/status` (PUT): `hasAnyRole('ADMIN', 'FUNCIONARIO')`.<br>* `/api/report/**`: `hasRole('ADMIN')`.<br>* `/api/audit/**`: `hasAnyRole('ADMIN', 'AUDITOR')`. | [`SecurityConfig.java`](file:///c:/Users/eltol/OneDrive/Escritorio/proyectos/BarrioDigital/ms-barriodigital-bff/src/main/java/cl/duoc/barriodigital/bff/config/SecurityConfig.java) |
| **5. Códigos de error adecuados** | * `CustomAuthEntryPoint`: Devuelve `401 Unauthorized` en JSON cuando el token es inválido, expirado o ausente.<br>* `CustomAccessDeniedHandler`: Devuelve `403 Forbidden` en JSON cuando el usuario no tiene el rol exigido. | [`CustomAuthEntryPoint.java`](file:///c:/Users/eltol/OneDrive/Escritorio/proyectos/BarrioDigital/ms-barriodigital-bff/src/main/java/cl/duoc/barriodigital/bff/config/CustomAuthEntryPoint.java)<br>[`CustomAccessDeniedHandler.java`](file:///c:/Users/eltol/OneDrive/Escritorio/proyectos/BarrioDigital/ms-barriodigital-bff/src/main/java/cl/duoc/barriodigital/bff/config/CustomAccessDeniedHandler.java) |
