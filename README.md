# Azure DevOps Dashboard & Checklist Application

Esta aplicación permite a los usuarios conectarse a Azure DevOps para visualizar información de proyectos, work items, y gestionar checklists asociados.

## Módulos Principales
- **Autenticación:** Manejo de usuarios y login al sistema.
- **Azure DevOps:** Integración para visualizar proyectos, work items, tiempos, y un dashboard con métricas.
- **Checklist:** Creación y gestión de checklists personalizadas vinculadas a work items de Azure DevOps.

## Tecnologías Usadas
- **Backend:** Java, Spring Boot, Spring Security (JWT), Spring Data JPA, MySQL.
- **Frontend:** Angular, TypeScript, ng2-charts.
- **Base de Datos:** MySQL.

## Prerrequisitos
- Java JDK (ej: 17 o superior)
- Maven (ej: 3.6 o superior)
- Node.js y npm (ej: Node 18+, npm 9+ o versiones LTS recientes)
- MySQL Server (ej: 8.0+)
- Un PAT (Personal Access Token) de Azure DevOps con permisos de lectura para work items y proyectos (y escritura si se usan las funcionalidades de actualización).

## Configuración del Backend
1.  Clonar el repositorio: `git clone <url-del-repositorio>`
2.  Navegar al directorio del backend: `cd azure-devops-app/backend`
3.  Configurar la base de datos MySQL:
    *   Crear una base de datos (ej: `azure_devops_dashboard_db`).
    *   Actualizar `src/main/resources/application.properties` con la URL de tu base de datos, usuario y contraseña.
        ```properties
        spring.datasource.url=jdbc:mysql://localhost:3306/azure_devops_dashboard_db
        spring.datasource.username=tu_usuario_mysql
        spring.datasource.password=tu_contraseña_mysql
        ```
    *   El esquema inicial incluye tablas para usuarios, aplicaciones, y checklists.
        Estas se crearán/actualizarán al iniciar el backend si `spring.jpa.hibernate.ddl-auto`
        está configurado como `update` o `create`, o si `spring.sql.init.mode=always`
        está usando el archivo `schema.sql`.
4.  Configurar la integración con Azure DevOps:
    *   En `application.properties`, define las siguientes propiedades (reemplaza los placeholders):
        ```properties
        azure.devops.organization.url=https://dev.azure.com/TU_ORGANIZACION
        azure.devops.pat=TU_PERSONAL_ACCESS_TOKEN_AQUI
        azure.devops.api.version=7.1
        # (Opcional) Si la URL de tu organización ya incluye el proyecto principal y quieres usarlo por defecto:
        # azure.devops.organization.url=https://dev.azure.com/TU_ORGANIZACION/TU_PROYECTO_POR_DEFECTO
        ```
        El PAT necesita permisos como `Work Items (Read & write)`, `Project and Team (Read)`, `Token Administration (Read & manage)` si se planea usar todas las funcionalidades. Para solo lectura, permisos de `Read` son suficientes.
5.  Compilar y ejecutar el backend:
    ```bash
    mvn spring-boot:run
    ```
6.  La API estará disponible en `http://localhost:8080`.
7.  La documentación de la API (SpringDoc OpenAPI/Swagger UI) estará en `http://localhost:8080/swagger-ui.html`.

## Configuración del Frontend
1.  Navegar al directorio del frontend:
    ```bash
    cd azure-devops-app/frontend
    ```
    (Asegúrate de estar en el directorio `frontend` que está dentro de `azure-devops-app`)
2.  Instalar dependencias:
    ```bash
    npm install
    ```
3.  Configuración del Proxy (Opcional pero recomendado para desarrollo):
    *   El frontend está configurado para usar un proxy que redirige las llamadas a `/api` hacia `http://localhost:8080` (el backend). Esto se define en `frontend/proxy.conf.json`.
    *   Para que Angular CLI use este proxy, el comando `ng serve` se ejecuta con la opción `--proxy-config proxy.conf.json`. El `package.json` ya tiene el script `start` configurado para esto.
4.  Ejecutar el frontend:
    ```bash
    npm start
    ```
    (Esto usa el script `start` de `package.json` que típicamente ejecuta `ng serve --proxy-config proxy.conf.json`)
5.  La aplicación estará disponible en `http://localhost:4200`.

## Ejecución de Pruebas
-   **Backend (Maven):**
    ```bash
    cd azure-devops-app/backend
    mvn test
    ```
-   **Frontend (Angular CLI):**
    ```bash
    cd azure-devops-app/frontend
    ng test
    ```
    (Para pruebas End-to-End, se necesitaría configurar un framework como Cypress o Protractor y añadir scripts correspondientes).

## Próximos Pasos y Mejoras (Roadmap General)
-   **Checklist Module:** Refinar y expandir la funcionalidad de checklists (ej: plantillas, más opciones de personalización).
-   **Seguridad Avanzada:** Roles y permisos más granulares (ej: quién puede crear/editar/eliminar checklists).
-   **Más Métricas en Dashboard:** Expandir los tipos de gráficos y datos visualizados.
-   **Gestión de Configuración de Proyectos:** UI para que los usuarios configuren sus proyectos y PATs.
-   **Notificaciones:** Alertas en la UI o por email.
-   **Internacionalización (i18n).**
-   **Pruebas E2E.**

## Contribuciones
Por favor, sigue las guías de estilo del proyecto y asegúrate de que todas las pruebas pasen antes de enviar un Pull Request.
(Más detalles pueden añadirse aquí: Code of Conduct, Proceso de PR, etc.)
