# Changelog

Todos los cambios importantes de este proyecto se documentan en este archivo.

El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y el proyecto usa [Versionado Semántico](https://semver.org/lang/es/).
## [Unreleased]

### Added

- Catálogo de ejercicios con búsqueda y filtros por músculo y equipo, y detalle con instrucciones.
- Rutinas: plantillas del sistema y rutinas propias (crear, ver y eliminar).
- Accesos a Ejercicios y Rutinas en el Home.
- Plan semanal: asignar una rutina a cada día; el mensaje de la regla de descanso de 48 h del backend se muestra al usuario.
- Calendario del mes con estados (cumplido 🔥, pendiente, incumplido, descanso), racha y botón para marcar la rutina de hoy.
- Tarjeta "Hoy toca" en el Home con la rutina del día, la racha y el botón de marcar.
- Pantalla "Mi perfil físico": fecha de nacimiento, sexo (opcional), peso, estatura y nivel, con validación local (edad de 14 a 100 años, peso de 30 a 300 kg, estatura de 100 a 250 cm) y opción de borrar los datos.
- Acceso a "Mi perfil físico" en el Home, junto a Ejercicios y Rutinas.

### Changed

- Nueva estructura por capas (`data`, `domain`, `presentation`) para las funciones nuevas.
- Home rehecho en `presentation/home` y alimentado solo por el plan semanal y el calendario: saludo, frase motivadora, tira de la semana (L a D) con el estado de cada día, "Hoy toca", progreso de la semana ("X de Y rutinas") y accesos a Ejercicios, Rutinas y Mi perfil físico. Se actualiza al volver al Home y al marcar la rutina de hoy.
- Barra inferior del Home con Inicio, Calendario, Ejercicios y Perfil (sin botón central).

### Removed

- Hábitos en la app: lista, creación y detalle de hábitos, tarjetas de Ejercicio, Alimentación y Sueño, calendario de hábitos y "Progreso de hoy" basado en hábitos.
- Botones sin función del Home: Configuración, Progreso y Avisos.
- Endpoints y modelos de hábitos y de resumen en `ApiService` y `Models`.

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