# InventoryPro 📦📊

¡Bienvenido al repositorio oficial de **InventoryPro**! Una aplicación móvil de gestión comercial y productividad diseñada específicamente para pequeños emprendedores que venden sus productos a través de redes sociales y necesitan un control de stock simple, intuitivo pero altamente robusto.

Este proyecto es desarrollado para la materia **Desarrollo de Aplicaciones para Dispositivos Móviles** del segundo año de la **Tecnicatura Superior en Desarrollo de Software** (IFTS N°18, 2026)[cite: 1].

---

## 👥 Integrantes del Equipo y Roles

* **Valentin Martinez** - *Product Owner* (Gestión de requerimientos y backlog)
* **[Nombre Completo]** - *Tech Lead / Android Developer* (Arquitectura y desarrollo core)
* **Valentin Martinez** - *UX/UI Designer* (Diseño de interfaz y experiencia de usuario)
* **[Nombre Completo]** - *Backend Lead* (Desarrollo de API y sincronización nube)
* **[Nombre Completo]** - *QA / DevOps* (Estrategia de testing y automatización)

---

## 🚀 Características Principales del Proyecto

* **Onboarding Inicial:** Flujo guiado de bienvenida para introducir al usuario en las ventajas de la app en su primera instalación.
* **CRUD de Inventario:** Gestión integral de productos (nombre, precio, stock, categoría, descripción e imagen).
* **Catálogo Visual Dinámico:** Listado mediante *Card Views* con alertas visuales de estado críticas (Stock Bajo / Agotado) basadas en las Heurísticas de Nielsen.
* **Escáner Nativo Integrado:** Uso de la cámara como lector de códigos de barra para la localización rápida de productos.
* **Sincronización en Tiempo Real:** Integración con backend multiusuario para reflejar actualizaciones de inventario al instante.

---

## 🛠️ Stack Tecnológico Seleccionado

La aplicación está desarrollada siguiendo los estándares nativos modernos de la industria de Android:

* **Lenguaje:** Kotlin (100% del proyecto)
* **UI Framework:** Jetpack Compose (Diseño Declarativo)
* **Design System:** Material Design 3 con soporte nativo para *Dark Mode* y *Dynamic Color*
* **Local Persistence:** Room Database (Motor SQLite local)
* **Networking:** Retrofit 2 + Gson para el consumo de la API Rest
* **Hardware API:** Google Code Scanner API (Captura nativa por cámara sin fricción de permisos)

---

## 📐 Arquitectura de la Solución

La aplicación implementa una arquitectura limpia (*Clean Architecture*) organizada de manera estricta bajo el patrón **MVVM + Repository**.

```text
[ CAPA DE INTERFAZ DE USUARIO (UI LAYER) ]
      Jetpack Compose (Vistas / Screens)
            ▲ (Observa el estado mediante StateFlow)
            ▼ (Envía eventos de usuario)
      UI ViewModels (Gestión de Estado de Pantallas)
            │
            ▼ (Invoca operaciones de negocio)
[ CAPA DE DATOS (DATA LAYER) ]
      InventoryRepository (Única Fuente de Verdad)
            │
            ├───────────────────────────────┤
            ▼                               ▼
      [ Local Data Source ]           [ Remote Data Source ]
        Room Database (SQLite)          Retrofit 2 API Client