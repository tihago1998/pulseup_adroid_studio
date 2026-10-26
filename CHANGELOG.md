# Changelog

Todos los cambios importantes de este proyecto se documentan en este archivo.

El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y el proyecto usa [Versionado Semántico](https://semver.org/lang/es/).
## [Unreleased]

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