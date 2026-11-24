# 🏛️ BarrioDigital: Plataforma Comunal de Atención Vecinal y Gestión de Trámites

> **Evaluación Parcial N° 1 - DSY1107: Desarrollo Cloud Native I (Duoc UC)**  
> **Integración:** AWS (EC2 + API Gateway HTTP API con JWT Authorizer) + Azure AD (IDaaS Microsoft Entra ID).

---

## 📌 1. Descripción del Proyecto

**BarrioDigital** es una solución Cloud Native para resolver la gestión de solicitudes, cupos de atención municipal, coordinación de cuadrillas en terreno, auditoría inmutable y reportería en tiempo real para una red de 20 oficinas comunales.

### Componentes Clave:
* **Frontend:** Angular con MSAL (`@azure/msal-angular`, `@azure/msal-browser`), con login corporativo Microsoft, guards por rol e interceptor automático del token Bearer.
* **IDaaS (Identidad):** Microsoft Entra ID (Azure AD), que emite los tokens JWT y los App Roles (`Admin`, `Funcionario`, `Vecino`, `Auditor`).
* **AWS API Gateway:** HTTP API protegida con **JWT Authorizer**, validando el issuer de Azure AD (`https://login.microsoftonline.com/<TENANT_ID>/v2.0`) y la audiencia (`api://<CLIENT_ID>`).
* **Backend Spring Boot:** Microservicios en Java 17 tras un **BFF Gateway** con Spring Security OAuth2 Resource Server.
* **Mensajería Asíncrona:** Clúster **RabbitMQ** (2 nodos con DLQ) para notificaciones y despacho de cuadrillas.
* **Streaming de Eventos:** Clúster **Apache Kafka** (3 brokers + Zookeeper) para auditoría inmutable (`audit.timeline`) y analítica comunal (`requests.events`).

---

## 📂 2. Estructura de Repositorios y Código Fuente

Siguiendo la sugerencia de repositorios de la pauta de evaluación:

```text
BarrioDigital/
├── frontend-barriodigital/      # Frontend Angular + MSAL v3 (Single Page Application)
├── ms-barriodigital-bff/        # BFF Gateway (Spring Boot + Spring Security OAuth2 + Azure JWT)
├── ms-barriodigital-requests/   # Microservicio de Trámites y máquina de estados (Producer AMQP/Kafka)
├── ms-barriodigital-catalog/    # Microservicio de Catálogo y Cupos Diarios
├── ms-barriodigital-notify/     # Microservicio Consumidor RabbitMQ (email, crew, certificate)
├── ms-barriodigital-report/     # Microservicio Consumidor Kafka y KPIs en tiempo real
├── ms-barriodigital-audit/      # Microservicio Consumidor Kafka y Timeline inmutable
├── ms-barriodigital-admin-mq/   # Microservicio Administrador de RabbitMQ (Evaluación 3)
├── infra/
│   ├── apps/                    # compose.yml para ec2-apps (BFF, Microservicios, Frontend, Admin-MQ)
│   ├── mq/                      # compose.yml y rabbitmq_definitions.json para ec2-mq (RabbitMQ)
│   ├── kafka/                   # compose.yml para ec2-kafka (Zookeeper, Kafka, Kafka UI)
│   ├── aws/                     # Plantillas CloudFormation, Terraform y scripts para AWS
│   └── docker-compose-all-in-one.yml # Compose unificado para pruebas locales o EC2 único
├── docs/
│   ├── AZURE_AD_CONFIG.md       # Paso a paso para registrar Apps y Roles en Azure Portal
│   ├── AWS_DEPLOYMENT_GUIDE.md  # Paso a paso para desplegar en AWS (EC2 + API Gateway)
│   ├── ARCHITECTURE.md          # Arquitectura técnica, diagramas y topologías
│   ├── EVALUATION_RUBRIC_COMPLIANCE.md # Mapeo de rúbrica Evaluación 1 (MSAL 60% y BFF 40%)
│   └── EVALUACION_3_RABBITMQ.md # Mapeo de rúbrica Evaluación 3 (RabbitMQ, DLQs y Admin 100%)
└── pom.xml                      # POM agregador raíz para compilación unificada Maven
```

---

## 🚀 3. Guía Rápida de Ejecución

### Opción A: Ejecución Local Completa con Docker
Para levantar toda la infraestructura (BFF, microservicios, frontend, RabbitMQ, Kafka y Kafka UI):
```bash
cd infra
docker compose -f docker-compose-all-in-one.yml up -d --build
```
* **Frontend Web:** `http://localhost:80`
* **Spring BFF Gateway:** `http://localhost:8080`
* **RabbitMQ Management UI:** `http://localhost:15672` (usuario: `guest`, clave: `guest`)
* **Kafka UI:** `http://localhost:8090`

### Opción B: Ejecución en AWS (Nube)
1. **Configurar Azure AD:** Siga [`docs/AZURE_AD_CONFIG.md`](file:///c:/Users/eltol/OneDrive/Escritorio/proyectos/BarrioDigital/docs/AZURE_AD_CONFIG.md) para registrar las aplicaciones y roles.
2. **Desplegar en AWS:** Siga [`docs/AWS_DEPLOYMENT_GUIDE.md`](file:///c:/Users/eltol/OneDrive/Escritorio/proyectos/BarrioDigital/docs/AWS_DEPLOYMENT_GUIDE.md) utilizando las plantillas de CloudFormation (`infra/aws/cloudformation-ec2.yaml` y `infra/aws/cloudformation-apigw.yaml`).

---

## 👥 4. Roles y Usuarios del Sistema

| Rol | Responsabilidad | Endpoints Clave |
| :--- | :--- | :--- |
| **Admin** | Define tipos de trámite, cupos y visualiza KPIs. | `POST /api/catalog/procedures`, `GET /api/report/kpis` |
| **Funcionario** | Admite solicitudes, asigna cuadrillas y cierra trámites. | `PUT /api/requests/{id}/status` |
| **Vecino** | Ingresa y sigue el avance de sus requerimientos. | `POST /api/requests`, `GET /api/requests` |
| **Auditor** | Consulta la trazabilidad inmutable. Solo lectura. | `GET /api/audit`, `GET /api/audit/request/{id}` |

---

## 📖 5. Documentación Adicional
* [Guía de Configuración de Azure Entra ID](file:///c:/Users/eltol/OneDrive/Escritorio/proyectos/BarrioDigital/docs/AZURE_AD_CONFIG.md)
* [Guía de Despliegue en AWS (EC2 + API Gateway)](file:///c:/Users/eltol/OneDrive/Escritorio/proyectos/BarrioDigital/docs/AWS_DEPLOYMENT_GUIDE.md)
* [Arquitectura y Topologías (RabbitMQ & Kafka)](file:///c:/Users/eltol/OneDrive/Escritorio/proyectos/BarrioDigital/docs/ARCHITECTURE.md)
* [Cumplimiento de Rúbrica Duoc UC](file:///c:/Users/eltol/OneDrive/Escritorio/proyectos/BarrioDigital/docs/EVALUATION_RUBRIC_COMPLIANCE.md)
