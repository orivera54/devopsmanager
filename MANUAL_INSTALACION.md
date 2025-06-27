# Manual de Instalación: Azure DevOps Dashboard & Checklist App

## 1. Introducción

Este manual proporciona instrucciones detalladas para configurar, instalar y ejecutar la aplicación "Azure DevOps Dashboard & Checklist App" en un entorno de desarrollo local. La aplicación consta de un backend desarrollado en Java con Spring Boot y un frontend en Angular.

## 2. Prerrequisitos

Asegúrese de tener instalado el siguiente software antes de comenzar:

- **Java Development Kit (JDK):** Versión 17 o superior (ej: OpenJDK, Oracle JDK).
- **Apache Maven:** Versión 3.6 o superior (para compilar el backend).
- **Node.js y npm:** Node.js versión 18.x o superior, npm versión 9.x o superior (para el frontend).
  - Puede verificar sus versiones con `node -v` y `npm -v`.
- **MySQL Server:** Versión 8.0 o superior.
  - Necesitará acceso para crear una base de datos y un usuario.
- **Git:** Para clonar el repositorio.
- **IDE (Opcional pero Recomendado):** IntelliJ IDEA, Eclipse (para Java/Spring Boot) y VS Code (para Angular).
- **Personal Access Token (PAT) de Azure DevOps:**
  - Con permisos de lectura para Proyectos y Work Items (`Work Items > Read`, `Project and Team > Read`).
  - Si se quieren probar las funcionalidades de edición de work items, se necesitarán permisos de escritura (`Work Items > Read & Write`).

## 3. Configuración del Entorno de Desarrollo (Opcional)

- **JAVA_HOME:** Asegúrese de que la variable de entorno `JAVA_HOME` apunte a su directorio de instalación del JDK.
- **MAVEN_HOME (Opcional):** Si tiene Maven instalado manualmente, configure `MAVEN_HOME` y añada su directorio `bin` al `PATH`.
- **Node/npm en el PATH:** Asegúrese de que los directorios de Node.js y npm estén en su variable de entorno `PATH`.

## 4. Configuración de la Base de Datos MySQL

1.  **Acceda a su servidor MySQL.**
2.  **Cree una nueva base de datos:**
    ```sql
    CREATE DATABASE azure_devops_dashboard_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    ```
3.  **Cree un usuario para la aplicación (o use uno existente):**
    ```sql
    CREATE USER 'appuser'@'localhost' IDENTIFIED BY 'tu_contraseña_segura';
    GRANT ALL PRIVILEGES ON azure_devops_dashboard_db.* TO 'appuser'@'localhost';
    FLUSH PRIVILEGES;
    ```
    *Reemplace `'appuser'` y `'tu_contraseña_segura'` con los valores deseados.*

## 5. Configuración del Backend (Spring Boot)

1.  **Clonar el Repositorio:**
    ```bash
    git clone <URL_DEL_REPOSITORIO>
    cd <NOMBRE_DEL_REPOSITORIO>/backend
    ```
    *(Reemplace `<URL_DEL_REPOSITORIO>` y `<NOMBRE_DEL_REPOSITORIO}`)*

2.  **Configurar `application.properties`:**
    Abra el archivo `backend/src/main/resources/application.properties` y actualice las siguientes propiedades:

    - **Conexión a la Base de Datos:**
      ```properties
      spring.datasource.url=jdbc:mysql://localhost:3306/azure_devops_dashboard_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
      spring.datasource.username=appuser
      spring.datasource.password=tu_contraseña_segura
      ```
      *(Ajuste el puerto `3306` si su MySQL corre en uno diferente. Actualice `appuser` y `tu_contraseña_segura`)*

    - **Configuración de Azure DevOps:**
      ```properties
      azure.devops.organization.url=https://dev.azure.com/TU_ORGANIZACION_AZDO
      azure.devops.pat=TU_PERSONAL_ACCESS_TOKEN_DE_AZDO
      azure.devops.api.version=7.1
      ```
      *(Reemplace `TU_ORGANIZACION_AZDO` con el nombre de su organización en Azure DevOps y `TU_PERSONAL_ACCESS_TOKEN_DE_AZDO` con su PAT).*
      *El placeholder `YOUR_AZDO_ORG_PLACEHOLDER` en `WorkItemDetailComponent.ts` del frontend también debe ser actualizado o reemplazado por una fuente dinámica para la correcta funcionalidad de las checklists.*


    - **Configuración de JWT (Opcional - se genera una clave por defecto si se deja vacía):**
      ```properties
      # app.jwtSecret=YourSuperSecretKeyWhichShouldBeLongAndSecureRandomlyGenerated
      app.jwtExpirationInMs=86400000
      ```

    - **Inicialización del Esquema de la Base de Datos:**
      La propiedad `spring.jpa.hibernate.ddl-auto` (ej: `update`, `validate`) y `spring.sql.init.mode` (ej: `always`) controlan cómo se crea/actualiza el esquema. El archivo `schema.sql` contiene las definiciones de las tablas (`users`, `applications`, `checklists`, `checklist_items`).
      Con `spring.sql.init.mode=always`, el `schema.sql` se ejecutará. Con `ddl-auto=update`, Hibernate intentará actualizar el esquema.

## 6. Compilación y Ejecución del Backend

1.  **Desde la raíz del directorio `backend`:**
    ```bash
    mvn clean install
    mvn spring-boot:run
    ```
    O, si prefiere ejecutar el JAR directamente después de compilar:
    ```bash
    # mvn clean install
    # java -jar target/azuredevopsdashboard-0.0.1-SNAPSHOT.jar (el nombre del JAR puede variar)
    ```

2.  El backend debería iniciarse en `http://localhost:8080` (o el puerto configurado en `application.properties`).

## 7. Configuración del Frontend (Angular)

1.  **Navegar al directorio del frontend:**
    Desde la raíz del proyecto:
    ```bash
    cd ../frontend
    ```
    *(Si estaba en el directorio `backend`)*

2.  **Instalar Dependencias de Node.js:**
    ```bash
    npm install
    ```
    Esto instalará Angular CLI localmente y todas las dependencias listadas en `package.json` (incluyendo `ng2-charts` y `chart.js`).

3.  **Configuración del Proxy (Opcional pero Recomendado para Desarrollo):**
    El frontend está configurado para hacer llamadas API a `/api/...`. Para que esto funcione en desarrollo cuando el servidor de Angular (`ng serve`) corre en un puerto diferente al backend (ej: Angular en 4200, Spring Boot en 8080), se usa un archivo de configuración de proxy.
    - El archivo `frontend/proxy.conf.json` está configurado para redirigir `/api` a `http://localhost:8080`.
    - El archivo `frontend/angular.json` en la configuración `serve` ya debería tener la opción `proxyConfig`:
      ```json
      // angular.json (extracto)
      "serve": {
        "builder": "@angular-devkit/build-angular:dev-server",
        "configurations": {
          "production": { /* ... */ },
          "development": {
            "browserTarget": "azure-devops-dashboard-ui:build:development",
            "proxyConfig": "proxy.conf.json" // Asegurar que esta línea esté presente
          }
        },
        "defaultConfiguration": "development"
      },
      ```
    Si necesita cambiar el puerto del backend, actualice `proxy.conf.json`.

## 8. Compilación y Ejecución del Frontend

1.  **Desde la raíz del directorio `frontend`:**
    ```bash
    ng serve
    ```
    *(Esto usa la configuración de desarrollo que incluye el proxy).*

2.  La aplicación frontend estará disponible en `http://localhost:4200`.

## 9. Acceso a la Aplicación y Documentación API

- **Aplicación Frontend:** `http://localhost:4200`
- **API Backend:** `http://localhost:8080`
- **Documentación API (Swagger UI):** `http://localhost:8080/swagger-ui.html` (después de que el backend se haya iniciado).
- **Especificación OpenAPI:** `http://localhost:8080/v3/api-docs`

## 10. Solución de Problemas Comunes

- **Errores `Could not resolve dependencies` (Maven):**
  - Verifique su conexión a internet.
  - Asegúrese de que `settings.xml` de Maven (si usa uno personalizado) esté configurado correctamente para acceder a Maven Central.
  - Pruebe `mvn clean install -U` para forzar la actualización de snapshots.
- **Errores `npm ERR! ...` (npm install):**
  - Verifique su conexión a internet.
  - Pruebe eliminando `node_modules` y `package-lock.json` y ejecutando `npm install` de nuevo.
  - Asegúrese de tener permisos de escritura en el directorio.
  - Considere usar un gestor de versiones de Node como NVM para evitar problemas de permisos globales.
- **El backend no conecta a la base de datos:**
  - Verifique que MySQL Server esté corriendo.
  - Confirme que los datos en `spring.datasource.url`, `username`, y `password` en `application.properties` sean correctos.
  - Asegúrese de que el usuario de la base de datos tenga permisos para la base de datos especificada desde `localhost` (o el host desde donde se conecte).
- **El frontend no puede llamar al backend (errores CORS o 404 en `/api`):**
  - Asegúrese de que el backend esté corriendo.
  - Verifique que la configuración del proxy (`proxy.conf.json` y `angular.json`) en el frontend sea correcta y que esté usando `ng serve` (que usa esta configuración).
  - Si despliega en producción, necesitará una configuración de CORS adecuada en el backend o un reverse proxy (ej: Nginx, Apache) para manejar las rutas.
- **Error relacionado con `YOUR_AZDO_ORG_PLACEHOLDER`:**
  - Si ve errores al intentar cargar checklists, asegúrese de haber reemplazado el placeholder `YOUR_AZDO_ORG_PLACEHOLDER` en `frontend/src/app/modules/azure-devops/components/work-item-detail/work-item-detail.component.ts` con el nombre real de su organización de Azure DevOps, o implemente una forma dinámica de obtener este valor.

---
Fin del Manual de Instalación.
