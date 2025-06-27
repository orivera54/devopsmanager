# Diseño Funcional: Azure DevOps Dashboard & Checklist App

## 1. Introducción y Propósito

Este documento describe el diseño funcional de la aplicación "Azure DevOps Dashboard & Checklist App". El propósito de la aplicación es proporcionar a los usuarios una herramienta para visualizar y gestionar información de sus proyectos en Azure DevOps, incluyendo un dashboard con métricas clave, y la capacidad de crear y administrar checklists personalizadas asociadas a los work items de Azure DevOps.

## 2. Arquitectura General de la Aplicación

La aplicación sigue una arquitectura cliente-servidor moderna:

-   **Backend (Servidor):**
    -   Tecnología: Java 17+ con Spring Boot Framework.
    -   Responsabilidades:
        -   Gestionar la lógica de negocio.
        -   Interactuar con la base de datos (MySQL).
        -   Proveer una API RESTful segura para el frontend.
        -   Manejar la autenticación y autorización de usuarios (Spring Security con JWT).
        -   Comunicarse con la API REST de Azure DevOps para obtener y actualizar datos.
    -   Componentes Clave: Controladores, Servicios, Repositorios (Spring Data JPA), Entidades JPA.

-   **Frontend (Cliente):**
    -   Tecnología: Angular (TypeScript).
    -   Responsabilidades:
        -   Proporcionar la interfaz de usuario (UI) e interacción con el usuario (UX).
        -   Consumir la API RESTful del backend.
        -   Gestionar el estado del lado del cliente.
        -   Renderizar vistas dinámicas y componentes reutilizables.
    -   Componentes Clave: Módulos (con carga diferida), Componentes, Servicios, Modelos/Interfaces, Enrutamiento.

-   **Base de Datos:**
    -   Tecnología: MySQL Server.
    -   Responsabilidades:
        -   Persistir datos de usuarios de la aplicación.
        -   Almacenar información de aplicaciones cliente (para futuras integraciones API).
        -   Persistir datos del módulo de checklists (checklists e ítems).

## 3. Diseño de la Base de Datos

La base de datos MySQL almacena la siguiente información principal:

-   **Tabla `users`:**
    -   Propósito: Almacenar credenciales y datos de los usuarios que pueden acceder a esta aplicación.
    -   Campos clave: `id`, `username`, `password` (hasheada), `email`, `is_active`.

-   **Tabla `applications`:**
    -   Propósito: (Para uso futuro) Registrar aplicaciones cliente que puedan interactuar con la API de esta aplicación directamente.
    -   Campos clave: `id`, `app_name`, `app_token` (hasheado o encriptado).

-   **Tabla `checklists`:**
    -   Propósito: Almacenar las checklists creadas por los usuarios.
    -   Campos clave: `id`, `name`, `description`, `azure_devops_organization`, `azure_devops_project_name`, `azure_devops_work_item_id`, `created_by_user_id` (FK a `users.id`).
    -   Relación: Vinculada a un work item específico de Azure DevOps y a un usuario creador.

-   **Tabla `checklist_items`:**
    -   Propósito: Almacenar los ítems individuales de cada checklist.
    -   Campos clave: `id`, `checklist_id` (FK a `checklists.id`), `item_text`, `is_completed`, `item_order`.
    -   Relación: Pertenece a una checklist. Si una checklist se elimina, sus ítems se eliminan en cascada.

*(Para un diagrama E-R detallado o definiciones DDL completas, referirse al archivo `schema.sql` y a la documentación de la base de datos del proyecto).*

## 4. Módulos Funcionales Detallados

### 4.1. Módulo de Autenticación y Gestión de Usuarios

-   **Propósito:** Permitir el acceso seguro a la aplicación y gestionar la identidad de los usuarios.
-   **Funcionalidades Clave (Backend):**
    -   Registro de nuevos usuarios (endpoint `POST /api/auth/signup`).
    -   Login de usuarios existentes (endpoint `POST /api/auth/signin`) usando nombre de usuario/email y contraseña.
    -   Generación de JSON Web Tokens (JWT) tras un login exitoso.
    -   Validación de JWT en solicitudes subsecuentes para proteger endpoints.
    -   Uso de Spring Security para la configuración de seguridad, hasheo de contraseñas (BCrypt), y gestión de `UserDetailsService`.
-   **Funcionalidades Clave (Frontend):**
    -   Formularios de Login y Registro (`LoginComponent`, `RegisterComponent`).
    *   Servicio `AuthService` para manejar la comunicación con el backend y el almacenamiento/eliminación del JWT en el cliente (ej: `localStorage`).
    *   `AuthGuard` para proteger rutas que requieren autenticación.

### 4.2. Módulo de Integración con Azure DevOps

-   **Propósito:** Conectar con la API de Azure DevOps para obtener, mostrar, y (parcialmente) modificar datos de proyectos y work items.
-   **Funcionalidades Clave (Backend):**
    -   Configuración del Personal Access Token (PAT) y URL de la organización Azure DevOps.
    -   `AzureDevOpsClientService`: Cliente HTTP (`RestTemplate`) para interactuar con la API REST de Azure DevOps.
    -   `AzDevOpsService`: Capa de servicio para la lógica de negocio.
    -   Endpoints REST (`/api/azdevops/...`):
        -   Listar proyectos (`GET /projects`).
        -   Obtener work items por ID, por batch de IDs, o mediante consultas WIQL (`GET /workitems/{id}`, `GET /workitems/batch`, `POST /projects/{projectId}/workitems/query`).
        -   Actualizar work items (`PATCH /workitems/{id}`) usando operaciones JSON Patch.
        -   Obtener comentarios de un work item (`GET /workitems/{workItemId}/comments`).
        -   Identificar work items con alertas por tiempo sin reportar (`GET /projects/{projectId}/workitems/time-alerts`).
    -   DTOs para mapear las respuestas de la API de Azure DevOps.
-   **Funcionalidades Clave (Frontend):**
    -   `AzureDevopsModule` (Angular, carga diferida).
    -   `AzureDevopsService` (Angular) para consumir los endpoints del backend.
    -   `ProjectListComponent`: Muestra la lista de proyectos.
    -   `WorkItemListComponent`: Muestra work items de un proyecto, con filtros (estado, tipo, asignado) que construyen consultas WIQL.
    -   `WorkItemDetailComponent`: Muestra detalles completos de un work item, permite la edición de campos clave, y visualiza comentarios.
    -   `DashboardComponent`: Presenta un dashboard con métricas (horas estimadas/completadas/restantes, % completado, conteo de estados abiertos/cerrados) y gráficos (distribución de estados, resumen de horas) para un proyecto. Muestra también las alertas por tiempo sin reportar.

### 4.3. Módulo de Checklists

-   **Propósito:** Permitir a los usuarios crear y gestionar checklists personalizadas, vinculadas a work items específicos de Azure DevOps.
-   **Funcionalidades Clave (Backend):**
    -   `ChecklistService`: Lógica para CRUD de checklists e ítems.
    -   Endpoints REST (`/api/checklists/...`):
        -   Crear una checklist asociada a un work item (`POST /`).
        -   Obtener checklists para un work item (`GET /for-workitem`).
        -   Obtener, actualizar (info) y eliminar una checklist (`GET /{id}`, `PUT /{id}/info`, `DELETE /{id}`).
        -   Añadir, actualizar (texto, estado, orden) y eliminar ítems de una checklist (`POST /{checklistId}/items`, `PATCH /items/{itemId}`, `DELETE /items/{itemId}`).
    -   DTOs para checklists e ítems.
-   **Funcionalidades Clave (Frontend):**
    -   `ChecklistService` (Angular) para consumir los endpoints del backend.
    -   `ChecklistManagerComponent`: Componente integrado en `WorkItemDetailComponent` que permite:
        -   Visualizar todas las checklists de un work item.
        -   Crear una nueva checklist para el work item.
        -   Eliminar una checklist.
        -   Añadir ítems a una checklist.
        -   Marcar/desmarcar ítems como completados.
        -   Eliminar ítems.

## 5. Flujos de Datos Principales (Ejemplos)

-   **Flujo de Login:**
    1.  Usuario ingresa credenciales en `LoginComponent` (Frontend).
    2.  `AuthService` (Frontend) envía POST a `/api/auth/signin` (Backend).
    3.  `AuthController` (Backend) usa `AuthenticationManager` y `CustomUserDetailsService` para validar.
    4.  Si es válido, `JwtTokenProvider` (Backend) genera un JWT.
    5.  JWT se devuelve al `AuthService` (Frontend), que lo almacena.
    6.  Frontend redirige al usuario a la página principal/dashboard.

-   **Flujo para Ver Work Items de un Proyecto:**
    1.  Usuario selecciona un proyecto en `ProjectListComponent` (Frontend).
    2.  Frontend navega a `WorkItemListComponent` con el ID del proyecto.
    3.  `WorkItemListComponent` (Frontend) llama a `AzureDevopsService.queryWorkItems()` (Frontend) con una consulta WIQL.
    4.  `AzureDevopsService` (Frontend) envía POST a `/api/azdevops/projects/{projectId}/workitems/query` (Backend).
    5.  `AzDevOpsController` (Backend) llama a `AzDevOpsService.getWorkItemsByWiql()` (Backend).
    6.  `AzDevOpsService` (Backend) llama a `AzureDevOpsClientService` para ejecutar la WIQL en Azure DevOps, obtiene IDs, y luego obtiene detalles de work items en batch.
    7.  Los datos de work items se devuelven al frontend y se muestran.

-   **Flujo para Crear un Ítem de Checklist:**
    1.  Usuario está en `WorkItemDetailComponent`, viendo el `ChecklistManagerComponent` (Frontend).
    2.  Usuario ingresa texto para un nuevo ítem en una checklist existente y hace clic en "Añadir Ítem".
    3.  `ChecklistManagerComponent` llama a `ChecklistService.addChecklistItem()` (Frontend).
    4.  `ChecklistService` (Frontend) envía POST a `/api/checklists/{checklistId}/items` (Backend).
    5.  `ChecklistController` (Backend) llama a `ChecklistService.addChecklistItem()` (Backend).
    6.  `ChecklistService` (Backend) crea el nuevo `ChecklistItem`, lo asocia a la `Checklist` y lo guarda en la BD.
    7.  El ítem creado se devuelve al frontend, que actualiza la UI.

## 6. Consideraciones de Seguridad (Alto Nivel)

-   **Autenticación:** Implementada con Spring Security y JWT. Todas las llamadas a `/api/**` (excepto `/api/auth/**`) requieren un JWT válido.
-   **Autorización:** Actualmente, un usuario autenticado tiene acceso a la mayoría de las funcionalidades. Se podrían implementar roles y permisos más granulares (ej: solo el creador de una checklist puede eliminarla, roles de administrador vs. usuario).
-   **Manejo de PAT de Azure DevOps:** El PAT se almacena en `application.properties` del backend. En un entorno de producción, debería gestionarse de forma más segura (ej: variables de entorno, secrets manager, o permitir a cada usuario ingresar su propio PAT que se almacena encriptado en la BD).
-   **Protección contra Vulnerabilidades Comunes:**
    -   Backend: Spring Security proporciona protección contra CSRF (deshabilitado para API sin estado), XSS (a través de la configuración de cabeceras y el escape de datos si se renderiza HTML en el servidor, aunque Angular ayuda en el frontend).
    -   Frontend: Angular tiene protecciones incorporadas contra XSS. Se usa `DomSanitizer` (`SafeHtmlPipe`) para renderizar HTML proveniente de Azure DevOps de forma segura.
    -   Validación de entradas en DTOs del backend y en formularios del frontend.
    -   Prevención de Inyección SQL: Usando Spring Data JPA y consultas parametrizadas (o constructores de queries seguras como Criteria API si se usan).

## 7. Posibles Futuras Extensiones (Roadmap General)

-   Refinamiento de la gestión de PAT de Azure DevOps (por usuario).
-   Roles y permisos avanzados.
-   Creación de work items desde la aplicación.
-   Plantillas de Checklist.
-   Notificaciones en tiempo real.
-   Internacionalización (i18n).
-   Mejoras en la UI/UX y más opciones de personalización.

---
Fin del Diseño Funcional.
