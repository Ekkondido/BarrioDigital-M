# 🔐 Guía de Configuración: Azure Active Directory (Microsoft Entra ID)

Esta guía explica paso a paso cómo registrar y configurar las identidades corporativas en **Microsoft Entra ID (Azure AD)** para el sistema **BarrioDigital**, de acuerdo con las especificaciones del caso y la rúbrica de evaluación de Duoc UC.

---

## 1. Conceptos Clave de la Solución
* **IDaaS (Identity as a Service):** Microsoft Entra ID actúa como proveedor de identidad OAuth 2.0 y OpenID Connect.
* **Tokens emitidos:**
  * `id_token`: Utilizado por el frontend Angular para obtener nombre, correo y roles del usuario.
  * `access_token`: Token JWT con firma criptográfica emitido para la audiencia de la API (`api://<CLIENT_ID>`), validado por **AWS API Gateway** y por el **Spring Boot BFF**.
* **App Roles:** Roles del dominio (`Admin`, `Funcionario`, `Vecino`, `Auditor`) inyectados en el claim `"roles": [...]` del token JWT.

---

## 2. Registro del Backend API en Azure Portal

1. Ingrese a [Azure Portal](https://portal.azure.com/) y navegue a **Microsoft Entra ID** &rarr; **App registrations** &rarr; **New registration**.
2. **Nombre:** `BarrioDigital-Backend-API`.
3. **Supported account types:** *Accounts in this organizational directory only (Single tenant)* o *Multitenant*.
4. Haga clic en **Register**.
5. Copie los valores generados:
   * **Application (client) ID:** (Ejemplo: `11111111-2222-3333-4444-555555555555`)
   * **Directory (tenant) ID:** (Ejemplo: `aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee`)

### A. Exponer la API (Expose an API)
1. En el menú lateral de la aplicación, seleccione **Expose an API**.
2. Al lado de **Application ID URI**, haga clic en **Set**.
3. Guarde el URI propuesto: `api://<Application-client-ID>`.
4. En **Scopes defined by this API**, haga clic en **Add a scope**:
   * **Scope name:** `access_as_user`
   * **Who can consent:** *Admins and users*
   * **Admin consent display name:** Acceso de usuario a BarrioDigital API
   * **Admin consent description:** Permite a la aplicación cliente consumir los microservicios comunales en nombre del usuario autenticado.
   * **State:** *Enabled*
5. Haga clic en **Add scope**.

### B. Crear los Roles de Aplicación (App Roles)
1. En el menú lateral, seleccione **App roles** &rarr; **Create app role**.
2. Cree sucesivamente los 4 roles requeridos por el caso:

| Display Name | Allowed member types | Value | Description |
| :--- | :--- | :--- | :--- |
| **Administrador** | Users/Groups | `Admin` | Define tipos de trámite, cupos y visualiza KPIs comunales. |
| **Funcionario** | Users/Groups | `Funcionario` | Admite solicitudes, asigna cuadrillas y cierra trámites. |
| **Vecino** | Users/Groups | `Vecino` | Ingresa requerimientos y sigue el estado de sus trámites. |
| **Auditor** | Users/Groups | `Auditor` | Consulta el timeline inmutable de eventos. Solo lectura. |

---

## 3. Registro del Frontend Angular (SPA)

1. En **App registrations**, haga clic en **New registration**.
2. **Nombre:** `BarrioDigital-Frontend-SPA`.
3. **Redirect URI:**
   * Plataforma: Seleccione **Single-page application (SPA)**.
   * URI: `http://localhost:4200/` (y agregue la URL pública de su despliegue en AWS S3/CloudFront/EC2 cuando esté activo).
4. Haga clic en **Register**.
5. Copie el **Application (client) ID** del Frontend.

### A. Solicitar Permisos para consumir el Backend
1. En el menú lateral del Frontend, seleccione **API permissions** &rarr; **Add a permission**.
2. Seleccione la pestaña **APIs my organization uses** y busque `BarrioDigital-Backend-API`.
3. Seleccione **Delegated permissions** y marque `access_as_user`.
4. Haga clic en **Add permissions**.
5. **Muy importante:** Haga clic en el botón **Grant admin consent for [Tu Organización]** para habilitar los permisos automáticamente.

---

## 4. Asignación de Roles a Usuarios de Prueba

Para que el token contenga el claim `roles`:
1. Vaya a **Microsoft Entra ID** &rarr; **Enterprise applications**.
2. Busque y seleccione `BarrioDigital-Backend-API`.
3. Vaya a **Users and groups** &rarr; **Add user/group**.
4. Seleccione su usuario de Duoc UC (o cuenta de prueba) y elija el rol que desea asignarle (ej: `Admin` o `Funcionario`).
5. Repita para crear usuarios de prueba con roles `Vecino` y `Auditor`.

---

## 5. Actualización de Credenciales en el Código

Una vez obtenidos los IDs de Azure Portal, actualice los siguientes archivos:

### Frontend (`frontend-barriodigital/src/environments/environment.ts`):
```typescript
export const environment = {
  production: false,
  azureAd: {
    clientId: 'TU_FRONTEND_CLIENT_ID',
    tenantId: 'TU_TENANT_ID',
    redirectUri: 'http://localhost:4200/',
    postLogoutRedirectUri: 'http://localhost:4200/login',
    apiScope: 'api://TU_BACKEND_CLIENT_ID/access_as_user'
  },
  apiGatewayUrl: 'https://<api-id>.execute-api.us-east-1.amazonaws.com' // o http://localhost:8080
};
```

### Backend BFF (`ms-barriodigital-bff/src/main/resources/application.yml`):
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://login.microsoftonline.com/TU_TENANT_ID/v2.0
          jwk-set-uri: https://login.microsoftonline.com/TU_TENANT_ID/discovery/v2.0/keys

azure:
  ad:
    tenant-id: TU_TENANT_ID
    client-id: TU_BACKEND_CLIENT_ID
    audience: api://TU_BACKEND_CLIENT_ID
```
