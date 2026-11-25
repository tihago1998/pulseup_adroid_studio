# Documentación de PulseUp — aplicación Android

**PulseUp: aplicación móvil de entrenamiento con plan semanal**  
Santiago Varela Peña y Sebastián Navarro López · Tecnología en Análisis y Desarrollo de Software, SENA · Ficha 3223873  
Versión de la documentación: **2.0 — 24 de noviembre de 2026**

Este repositorio contiene la **aplicación Android**. La documentación completa del proyecto (acta, interesados, requisitos, 43 historias de usuario y todos los diagramas) está en el repositorio del backend, para tenerla en un solo lugar:

**➡ [Documentación completa de PulseUp](https://github.com/tihago1998/pulse_up_backend/tree/dev/docs)**

Esta página resume lo que es específico de la aplicación.

## Contenido

1. [Qué hace la aplicación](#1-qué-hace-la-aplicación)
2. [Arquitectura](#2-arquitectura)
3. [Pantallas y rutas](#3-pantallas-y-rutas)
4. [Configuración y ejecución](#4-configuración-y-ejecución)
5. [Pruebas](#5-pruebas)
6. [Estado de las historias de usuario en la app](#6-estado-de-las-historias-de-usuario-en-la-app)
7. [Limitaciones conocidas de la app](#7-limitaciones-conocidas-de-la-app)
8. [Referencias](#8-referencias)

## 1. Qué hace la aplicación

El usuario se registra, completa su perfil físico, explora un catálogo de 150 ejercicios con fotos, recibe una recomendación personalizada de series, repeticiones, descanso y peso, crea rutinas, arma su plan semanal con avisos de descanso muscular de 48 horas (solo orientan, nunca bloquean) y marca cada día cumplido en un calendario con racha.

## 2. Arquitectura

Paquete `com.example.pulseup`, organizado en capas:

| Capa | Contenido |
|---|---|
| `domain` | Modelos y reglas de la app (por ejemplo, la semana y el progreso semanal) |
| `data` | Acceso a la API con Retrofit y OkHttp, repositorios, mapeadores y la sesión en memoria |
| `di` | Fábricas para crear repositorios y ViewModels |
| `presentation` | Pantallas en Jetpack Compose con `ViewModel` y `StateFlow` |

Las fotos se cargan con Coil (con caché). Los tiempos de espera de red son de 30 segundos para conectar y 30 para leer.

## 3. Pantallas y rutas

| Ruta | Pantalla | Historias |
|---|---|---|
| `login`, `register` | Inicio de sesión y registro | HU001, HU002 |
| `editProfile`, `changePassword` | Editar datos y cambiar contraseña | HU003 |
| `users` | Administrar usuarios | HU004–HU006 |
| `perfilFisico` | Perfil físico | HU029 |
| `ejercicios`, `ejercicio/{id}` | Catálogo y detalle con recomendación | HU009, HU010, HU030–HU032 |
| `creditosEjercicios` | Créditos de las fotos | HU036 |
| `rutinas`, `rutinaNueva` | Rutinas y creación de rutina | HU011–HU013, HU035 |
| `plan`, `planDia/{dia}`, `elegirEjercicio/{dia}` | Plan semanal, día del plan y selector de ejercicios | HU016, HU017, HU033, HU034 |
| `calendario` | Calendario y racha | HU018–HU020 |
| `home` | Inicio con la tira de la semana y «Hoy toca» | HU037 |

## 4. Configuración y ejecución

1. Iniciar el backend (`python app.py`, puerto 5050) y cargar las fotos con `python -m tools.importar_catalogo`.
2. En `local.properties` del proyecto Android definir la dirección del servidor, sin espacios al final:

   ```
   pulseup.baseUrl=http://IP_DEL_COMPUTADOR:5050
   ```

   La IP cambia según la red; hay que actualizarla cuando cambie.
3. Abrir el proyecto en Android Studio, sincronizar Gradle y ejecutar en un teléfono o emulador.

## 5. Pruebas

La aplicación tiene **174 pruebas unitarias** aprobadas. Se ejecutan con `./gradlew testDebugUnitTest`.

## 6. Estado de las historias de usuario en la app

Resultado de una auditoría de solo lectura del código (rama `dev`, 24 de noviembre de 2026). Lo que depende del servidor se marca como no aplicable en este repositorio.

| Estado | Historias |
|---|---|
| Implementadas | HU001, HU003, HU005, HU009, HU010, HU013, HU020 (vista), HU028, HU029–HU037, TK001 |
| Parciales | HU002, HU004, HU006, HU011, HU012, HU016, HU017, HU018, HU019, HU021, HU026 |
| No existen (planeadas) | HU007, HU008, HU014, HU015, HU023, HU024, HU025, HU027 |
| Retirada | HU022 |

El detalle de cada criterio de aceptación está en el documento de requisitos (DOC-03), en el repositorio del backend.

## 7. Limitaciones conocidas de la app

- La sesión vive solo en memoria y se pierde al cerrar la aplicación; con un 401 las pantallas nuevas avisan, pero no vuelven solas al inicio de sesión.
- Las pantallas antiguas (inicio de sesión, registro, editar perfil y cambiar contraseña) muestran códigos HTTP y texto técnico de las excepciones.
- Eliminar un usuario no pide confirmación.
- No se impide repetir un ejercicio dentro de una rutina.
- El selector de ejercicios de «Nueva rutina» muestra todo el catálogo sin paginar y sin fotos.
- «Hoy» se calcula con la fecha del teléfono, mientras el servidor usa su zona horaria.
- No hay notificaciones ni sesión guiada con temporizador (planeadas).

## 8. Referencias

- **free-exercise-db** (licencia Unlicense): https://github.com/yuhonas/free-exercise-db. Créditos visibles en la pantalla «Créditos de las fotos».
- **Open Gym**: referencia de diseño; sus medios son de su autor y no se reutilizan.
- Documentación completa: https://github.com/tihago1998/pulse_up_backend/tree/dev/docs
