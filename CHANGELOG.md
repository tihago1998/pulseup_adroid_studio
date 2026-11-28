# Changelog

Todos los cambios importantes de este proyecto se documentan en este archivo.

El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y el proyecto usa [Versionado Semántico](https://semver.org/lang/es/).
## [Unreleased]

### Added

- Catálogo de ejercicios con búsqueda y filtros por músculo y equipo, y detalle con instrucciones.
- Rutinas: plantillas del sistema y rutinas propias (crear, ver y eliminar).
- Accesos a Ejercicios y Rutinas en el Home.
- Plan semanal: asignar una rutina a cada día.
- Calendario del mes con estados (cumplido 🔥, pendiente, incumplido, descanso), racha y botón para marcar la rutina de hoy.
- Tarjeta "Hoy toca" en el Home con la rutina del día, la racha y el botón de marcar.
- Pantalla "Mi perfil físico": fecha de nacimiento, sexo (opcional), peso, estatura y nivel, con validación local (edad de 14 a 100 años, peso de 30 a 300 kg, estatura de 100 a 250 cm) y opción de borrar los datos.
- Acceso a "Mi perfil físico" en el Home, junto a Ejercicios y Rutinas.
- Catálogo ampliado de ejercicios (Fase 1): fotos con Coil, filtros por lugar (Gimnasio / Casa / Calistenia), músculo, equipo y nivel, y paginación de 20 en 20 al llegar al final.
- Detalle del ejercicio con fotos de inicio y final, pasos numerados, respiración, errores comunes, variantes fácil y difícil, y la recomendación personalizada (series, repeticiones o tiempo, descanso, reserva y peso aproximado).
- Pantalla de créditos de las fotos del catálogo (fuentes y licencias).
- Ejercicios en el plan semanal: cada día muestra sus ejercicios (miniatura, nombre y "3 × 10 · 40 kg", "3 × 40 s" o "35 min") y abre su detalle, donde se editan series, repeticiones o duración, peso y descanso (con la recomendación como guía), se quitan con confirmación y se agregan desde el catálogo.
- "Agregar a mi plan" en el detalle del ejercicio: hoja "¿Qué día?" con los 7 días, si ya tienen rutina y los avisos de 48 h; un día con aviso pide "Agregar igual". Confirmación con "Ver día".
- Al crear una rutina, hoja "¿Quieres ponerla en tu plan?" para elegir uno o varios días, con avisos de 48 h y un resumen al terminar.
- Documentación de la aplicación en docs/README.md.
- Plan del día: tocar toda la tarjeta de un ejercicio abre su edición (se quita el lápiz; la papelera sigue aparte) y una línea indica "Toca un ejercicio para editarlo".
- Plan del día: tocar la foto de un ejercicio abre una ventana con sus fotos ("Posición inicial" / "Posición final", deslizables con puntos), "Este ejercicio todavía no tiene fotos" si no tiene, "Reintentar" si falla y "Ver cómo se hace", que abre el detalle completo.

### Changed

- Nueva estructura por capas (`data`, `domain`, `presentation`) para las funciones nuevas.
- Home rehecho en `presentation/home` y alimentado solo por el plan semanal y el calendario: saludo, frase motivadora, tira de la semana (L a D) con el estado de cada día, "Hoy toca", progreso de la semana ("X de Y rutinas") y accesos a Ejercicios, Rutinas y Mi perfil físico. Se actualiza al volver al Home y al marcar la rutina de hoy.
- Barra inferior del Home con Inicio, Calendario, Ejercicios y Perfil (sin botón central).
- Los colores `Pu*` se definen todos en `ui/theme/Color.kt` (mismos valores); se borra `PerfilScreen.kt` con la tarjeta `TarjetaPerfil`, que no se usaba.
- La regla de descanso de 48 h solo avisa: al asignar una rutina se muestran antes los avisos (previsualizar) y se confirma con "Asignar igual"; el backend ya no responde 409.
- El catálogo ya no marca el nivel por defecto: solo se filtra por nivel si el usuario toca el chip (el nivel sigue visible en cada ejercicio y en la recomendación).
- `PuRojo` pasa a `ui/theme/Color.kt` y se agrega `PuAdvertencia` para los avisos que no bloquean.
- Home: el saludo se elige por la hora del teléfono ("Buenos días" de 05:00 a 11:59 y "Buenas tardes" de 12:00 a 18:59, con sol; "Buenas noches" de 19:00 a 04:59, con luna) y el icono va al lado del nombre.
- Home: la tarjeta "Hoy toca" deja un solo botón, "Plan de entrenamiento" (se quita "Calendario"), y con el día cumplido la segunda línea muestra el nombre de la rutina en vez de repetir el título.
- Home: se quita el acceso a Ejercicios (sigue en la barra inferior); Rutinas y Mi perfil físico quedan en una sola fila con dos tarjetas de igual tamaño.

### Removed

- Hábitos en la app: lista, creación y detalle de hábitos, tarjetas de Ejercicio, Alimentación y Sueño, calendario de hábitos y "Progreso de hoy" basado en hábitos.
- Botones sin función del Home: Configuración, Progreso y Avisos.
- Endpoints y modelos de hábitos y de resumen en `ApiService` y `Models`.

### Fixed

- Administración de usuarios: si falla la carga se muestra el error con un botón Reintentar, y los errores al cambiar un rol o eliminar un usuario muestran el mensaje del servidor o de conexión (antes se ocultaban).

## [1.0.0] - 2026-10-26

### Added

- Inicio de sesión y registro de usuarios.
- Roles: Usuario, Administrador y SuperAdministrador.
- Registro, listado y detalle de hábitos.
- Edición de perfil y cambio de contraseña.
- Administración de usuarios para los roles con permisos.
- Propiedad `primerNombre` en `Session` para mostrar saludos con el primer nombre del usuario.
- README, LICENSE (MIT) y CHANGELOG del repositorio.

### Changed

- La URL del backend se lee desde `local.properties` (`pulseup.baseUrl`) y se expone como `BuildConfig.BASE_URL`, en lugar de estar escrita en el código.
- Los tiempos de espera de red pasan de 90 a 30 segundos.
- La carpeta `.idea/` deja de versionarse y se completan las reglas de `.gitignore`.