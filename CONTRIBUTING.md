# Guía de contribución

Gracias por colaborar en PulseUp. Esta guía resume cómo trabajamos en el repositorio.

## Flujo de ramas

Usamos un flujo basado en Git Flow:

| Rama | Uso |
|------|-----|
| `main` | Versión estable. Solo recibe los releases. |
| `dev` | Integración del trabajo en curso. |
| `feature/*` | Funcionalidades nuevas. |
| `fix/*` | Corrección de errores. |
| `chore/*` | Mantenimiento, configuración y documentación. |
| `release/*` | Preparación de una versión. |

Reglas:

1. Crea siempre tu rama desde `dev` actualizada.
2. Nunca subas cambios directamente a `main` ni a `dev`. Todo entra por Pull Request.
3. Las ramas `feature/*`, `fix/*` y `chore/*` se integran en `dev`.
4. Los releases se integran en `main` y se etiquetan, por ejemplo `v1.0.0`.

```bash
git switch dev
git pull
git switch -c feature/nombre-de-la-funcionalidad
```

## Convención de commits

Seguimos Conventional Commits: `tipo(alcance): descripción`.

- El tipo y el alcance van en inglés: `feat`, `fix`, `docs`, `test`, `refactor`, `chore`, `ci`.
- La descripción va en español, en una sola línea y comenzando con un verbo en infinitivo.
- Sin espacio antes del paréntesis y con un espacio después de los dos puntos.
- Un commit por cambio coherente.

Ejemplos:

```
feat(session): agregar primerNombre para saludar al usuario
fix(config): leer BASE_URL desde local.properties
docs(readme): agregar estructura y ajustar características
```

## Pull Requests

- La base del PR es `dev` (o `main` solo en un release).
- El título sigue el mismo formato que los commits.
- La descripción incluye: resumen, tipo de cambio, pruebas realizadas, riesgos y una lista de verificación.
- Antes de integrar, revisa la pestaña **Files changed** y confirma que solo aparecen los archivos esperados.

## Configuración del entorno

1. Clona el repositorio y ábrelo con Android Studio.
2. Define la URL del backend en `local.properties` (archivo que no se sube a git):

```properties
   pulseup.baseUrl=http://192.168.X.X:5050/
```

3. Pulsa **Sync Now** y ejecuta la app. Si cambias de red Wi-Fi, actualiza la IP.

## Qué no se sube al repositorio

- `local.properties` y cualquier IP o dirección personal.
- La carpeta `.idea/`, las carpetas `build/` y `.gradle/`.
- Claves, contraseñas, llaves de firma (`.jks`, `.keystore`) y archivos `.apk`.

Antes de cada commit revisa `git status` y evita usar `git add .`: agrega los archivos de uno en uno.

## Primer pull después de ciertos cambios

Si al hacer `git pull` Android Studio avisa que el proyecto cambió y pregunta si lo recarga, responde **Yes**. Esto ocurre, por ejemplo, cuando se dejó de versionar la carpeta `.idea/`, y el IDE regenera esos archivos solo.