# Manual de Usuario: Azure DevOps Dashboard & Checklist App

## 1. Introducción

¡Bienvenido a la aplicación Azure DevOps Dashboard & Checklist App! Esta herramienta está diseñada para ayudarte a obtener una mejor visión de tus proyectos en Azure DevOps y para gestionar tareas detalladas mediante checklists personalizadas asociadas a tus work items.

Con esta aplicación podrás:
- Ver un resumen de tus proyectos de Azure DevOps.
- Analizar métricas clave de tus proyectos a través de un dashboard interactivo.
- Listar y filtrar tus work items (tareas, historias de usuario, bugs, etc.).
- Ver y editar los detalles de tus work items.
- Consultar los comentarios y discusiones de los work items.
- Recibir alertas sobre work items que podrían necesitar atención en el reporte de tiempo.
- Crear y gestionar checklists personalizadas para cualquier work item, ayudándote a desglosar tareas complejas.

## 2. Acceso y Primeros Pasos

### 2.1. Acceder a la Aplicación
Puedes acceder a la aplicación a través de tu navegador web en la siguiente dirección (proporcionada por tu administrador):
`http://localhost:4200` (Ejemplo para entorno local)

### 2.2. Iniciar Sesión (Login)
Al ingresar a la aplicación, serás recibido por la pantalla de inicio de sesión.
1.  Ingresa tu **Nombre de usuario** (o email) y **Contraseña**.
2.  Haz clic en el botón "Login".
3.  Si tus credenciales son correctas, serás redirigido a la página principal de la aplicación.

### 2.3. Registrarse (Crear una Nueva Cuenta)
Si no tienes una cuenta, puedes crear una:
1.  En la pantalla de login, busca y haz clic en el enlace "Register" o "Crear cuenta".
2.  Completa el formulario de registro con:
    -   Nombre de usuario deseado.
    -   Tu dirección de correo electrónico.
    -   Una contraseña segura.
3.  Haz clic en el botón "Register" o "Crear cuenta".
4.  Si el registro es exitoso, generalmente serás redirigido a la pantalla de login para que puedas ingresar con tus nuevas credenciales.

## 3. Navegación Principal

Una vez que hayas iniciado sesión, la interfaz principal te permitirá navegar entre las diferentes funcionalidades. La navegación principal usualmente se encontrará en una barra lateral o superior. Las secciones principales son:

-   **Azure DevOps:** Contiene todas las funcionalidades relacionadas con la integración de Azure DevOps.
    -   **Projects:** Para ver la lista de tus proyectos.
    -   (Dentro de un proyecto) **Dashboard:** Métricas y gráficos del proyecto.
    -   (Dentro de un proyecto) **Work Items:** Listado y gestión de work items.
-   **Logout:** Para cerrar tu sesión de forma segura.

*(Captura de pantalla de la navegación principal sería útil aquí)*

## 4. Módulo Azure DevOps

Esta sección te permite interactuar con los datos de tus proyectos en Azure DevOps.

### 4.1. Visualizar Proyectos
1.  Navega a la sección "Projects" (o la vista inicial después del login que muestre proyectos).
2.  Verás una lista de los proyectos de Azure DevOps a los que tienes acceso (según el PAT configurado en el backend).
3.  Cada proyecto mostrará su nombre, descripción, estado y fecha de última actualización.
4.  Desde aquí, podrás seleccionar un proyecto para ver más detalles.

*(Captura de pantalla de la lista de proyectos)*

### 4.2. Dashboard de un Proyecto
Al seleccionar un proyecto, podrás acceder a su dashboard.
1.  En la lista de proyectos, haz clic en el nombre del proyecto o en un enlace/botón "View Dashboard".
2.  El dashboard te mostrará:
    -   **Métricas Resumidas:**
        -   Horas Estimadas Totales.
        -   Trabajo Completado Total.
        -   Trabajo Restante Total.
        -   Porcentaje Completado (basado en horas o ítems).
        -   Número de Work Items Abiertos.
        -   Número de Work Items Cerrados.
    -   **Gráficos:**
        -   **Distribución de Estado de Work Items:** Un gráfico de tarta (o similar) mostrando cuántos work items hay en cada estado (Nuevo, Activo, Resuelto, Cerrado, etc.).
        -   **Resumen de Horas:** Un gráfico de barras mostrando las Horas Estimadas, Completadas y Restantes.
    -   **Alertas por Tiempo sin Reportar:** (Ver sección 4.7)

*(Capturas de pantalla del dashboard, mostrando las métricas y los gráficos)*

### 4.3. Listar Work Items de un Proyecto
1.  Desde la lista de proyectos, haz clic en un enlace/botón "View Work Items" para un proyecto específico. O navega a la sección de work items después de seleccionar un proyecto.
2.  Verás una tabla o lista con los work items del proyecto. La información mostrada típicamente incluye: ID, Tipo (Tarea, Bug, etc.), Título, Estado, Asignado A, y Fecha de Último Cambio.
3.  **Filtros:** Encima de la lista, encontrarás campos para filtrar los work items:
    -   **Estado:** Escribe un estado (ej: Active, Closed) y presiona Enter o "Apply Filters".
    -   **Tipo:** Escribe un tipo (ej: User Story, Task) y presiona Enter o "Apply Filters".
    -   **Asignado A:** Escribe el nombre o email del usuario asignado y presiona Enter o "Apply Filters".
    -   Puedes usar el botón "Clear Filters" para limpiar todos los filtros aplicados.

*(Captura de pantalla de la lista de work items con los filtros visibles)*

### 4.4. Ver Detalles de un Work Item
1.  En la lista de work items, haz clic en el título del work item o en un botón/enlace "View Details".
2.  Serás llevado a la página de detalle del work item, donde verás toda la información relevante:
    -   ID, Tipo, Título, Estado, Razón.
    -   Asignado A, Creado Por, Modificado Por (con fechas).
    -   Descripción completa (puede incluir formato HTML).
    -   Prioridad.
    -   Estimaciones de trabajo: Original, Completado, Restante.
    -   Comentarios (ver sección 4.6).
    -   Checklists asociadas (ver sección 5).

*(Captura de pantalla de la vista de detalle de un work item)*

### 4.5. Editar un Work Item
1.  En la página de detalle de un work item, busca un botón "Edit".
2.  Al hacer clic, los campos editables se convertirán en un formulario. Podrás modificar:
    -   Título.
    -   Estado (seleccionando de una lista).
    -   Razón (las opciones pueden cambiar según el estado seleccionado).
    -   Asignado A (ingresando el nombre o email del usuario).
    -   Descripción.
    -   Prioridad.
    -   Horas Estimadas, Completadas y Restantes.
3.  Una vez hechos los cambios, haz clic en "Save".
4.  Si no deseas guardar, haz clic en "Cancel".
5.  La vista se actualizará con los nuevos datos si el guardado fue exitoso.

*(Captura de pantalla del formulario de edición de un work item)*

### 4.6. Ver Comentarios de un Work Item
En la página de detalle de un work item, usualmente habrá una sección dedicada a los comentarios o la discusión.
-   Aquí podrás leer todos los comentarios asociados al work item, mostrando quién lo escribió y cuándo.

### 4.7. Alertas por Tiempo sin Reportar
En el Dashboard de un proyecto, encontrarás una sección de "Alertas por Tiempo sin Reportar".
-   Esta sección lista los work items que están en un estado activo pero no tienen horas de trabajo completado registradas (o son cero).
-   Para cada alerta, verás:
    -   ID, Título y Tipo del work item.
    -   A quién está asignado.
    -   El mensaje de alerta específico.
-   Puedes hacer clic en el work item para navegar a sus detalles y actualizarlo si es necesario.

*(Captura de pantalla de la sección de alertas en el dashboard)*

## 5. Módulo Checklist

Este módulo te permite crear listas de tareas detalladas (checklists) y vincularlas a tus work items de Azure DevOps.

### 5.1. Acceder a las Checklists de un Work Item
1.  Navega a la página de detalle de un work item (ver sección 4.4).
2.  Busca una sección titulada "Checklists". Aquí se mostrarán todas las checklists asociadas a ese work item.

### 5.2. Crear una Nueva Checklist
1.  En la sección "Checklists" de la vista de detalle de un work item, encontrarás un pequeño formulario para crear una nueva checklist.
2.  Ingresa un **Nombre** para tu checklist (ej: "Pruebas de Aceptación", "Pasos para Despliegue").
3.  Opcionalmente, añade una **Descripción**.
4.  Haz clic en el botón "Add Checklist" o "Crear".
5.  La nueva checklist aparecerá en la lista.

*(Captura de pantalla del formulario de creación de checklist)*

### 5.3. Añadir Ítems a una Checklist
1.  Una vez creada una checklist, o al ver una existente, encontrarás un campo de texto o un pequeño formulario debajo de los ítems de esa checklist para añadir nuevos ítems.
2.  Escribe el **texto del ítem** (ej: "Verificar configuración X", "Ejecutar prueba Y").
3.  (Opcional) Puedes especificar un orden si la interfaz lo permite.
4.  Haz clic en "Add Item". El nuevo ítem aparecerá en la lista de esa checklist.

### 5.4. Marcar/Desmarcar Ítems como Completados
-   Cada ítem de la checklist tendrá una casilla de verificación (checkbox) a su lado.
-   Haz clic en la casilla para marcar un ítem como completado. El texto del ítem podría tacharse o cambiar visualmente.
-   Vuelve a hacer clic para desmarcarlo.

### 5.5. Editar Texto de Ítems
*(Esta funcionalidad podría no estar implementada en la primera versión. Si lo está, se describiría aquí cómo hacer doble clic o encontrar un botón de editar para cambiar el texto de un ítem existente).*
Actualmente, para cambiar el texto de un ítem, necesitarías eliminar el ítem antiguo y crear uno nuevo con el texto corregido.

### 5.6. Eliminar Ítems y Checklists
-   **Eliminar un Ítem:** Cada ítem de la checklist tendrá un pequeño botón o icono de eliminar (ej: una 'x' o un bote de basura). Haz clic en él y confirma para eliminar el ítem.
-   **Eliminar una Checklist Completa:** Cada checklist tendrá un botón o icono similar para eliminarla por completo. Al hacer clic, se te pedirá confirmación, ya que esto eliminará la checklist y todos sus ítems.

*(Captura de pantalla de una checklist con sus ítems, mostrando checkboxes y botones de eliminar)*

## 6. Gestión de Cuenta (Opcional)

*(Esta sección se completaría si se implementan funcionalidades como cambio de contraseña, actualización de perfil de usuario, etc. Actualmente, estas no están implementadas).*

## 7. Preguntas Frecuentes (FAQ)

-   **P: ¿Cómo obtengo un Personal Access Token (PAT) de Azure DevOps?**
    R: Debes generarlo desde tu perfil en Azure DevOps. Asegúrate de darle los permisos correctos (lectura de proyectos y work items como mínimo). Consulta la documentación oficial de Microsoft Azure DevOps para los pasos exactos.

-   **P: ¿Por qué no veo todos mis proyectos de Azure DevOps?**
    R: La aplicación solo mostrará los proyectos a los que el PAT configurado en el backend tiene acceso. Verifica los permisos de tu PAT.

-   **P: Los cálculos de horas en el dashboard no parecen correctos.**
    R: Asegúrate de que los campos de estimación (`Original Estimate`, `Completed Work`, `Remaining Work`) se estén usando consistentemente en tus work items en Azure DevOps. La aplicación solo refleja los datos que encuentra.

---
Fin del Manual de Usuario.
