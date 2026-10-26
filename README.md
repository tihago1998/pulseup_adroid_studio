# PulseUp (Android)

Aplicación móvil nativa para Android que ayuda a las personas a registrar y dar seguimiento a sus hábitos saludables. Se conecta a la API de [PulseUp Backend](https://github.com/tihago1998/pulse_up_backend).

## Características

- Inicio de sesión y registro de usuarios.
- Roles: Usuario, Administrador y SuperAdministrador.
- Registro, listado y detalle de hábitos.
- Edición de perfil y cambio de contraseña.
- Administración de usuarios para los roles con permisos.

## Tecnologías

- Kotlin y Jetpack Compose (Material 3)
- Navigation Compose
- Retrofit, Gson y OkHttp (cliente HTTP)
- Coroutines y DataStore
- Gradle (Kotlin DSL)

## Requisitos

- Android Studio actualizado.
- Android 7.0 o superior (API 24+).
- El backend de PulseUp en ejecución (ver su README).
- Celular y computador en la misma red Wi-Fi, o el emulador de Android Studio.

## Instalación y ejecución

1. Clona el repositorio:

```bash
   git clone https://github.com/tihago1998/pulseup_adroid_studio.git
```

2. Ábrelo con Android Studio y espera a que termine la sincronización de Gradle.
3. Configura la URL del backend (ver la sección siguiente).
4. Arranca el backend y pulsa **Run** en Android Studio.

## Configuración de la URL del backend

La dirección del backend no está escrita en el código. Se lee desde el archivo `local.properties` (en la raíz del proyecto), que no se sube a git:

```properties
pulseup.baseUrl=http://192.168.X.X:5050/
```

- Reemplaza `192.168.X.X` por la IP de tu computador en la red local. El backend la imprime al arrancar con `python app.py`.
- Conserva el `http://` y la `/` del final.
- Si la propiedad no existe, la app usa `http://10.0.2.2:5050/`, que solo funciona en el emulador de Android Studio.
- Si cambias de red Wi-Fi, actualiza la IP, pulsa **Sync Now** y vuelve a ejecutar.

## Estructura del proyecto

```
app/src/main/java/com/example/pulseup/
├── MainActivity.kt           # Punto de entrada de la aplicación
├── AppNavegacion.kt          # Rutas y navegación entre pantallas
├── LoginScreen.kt            # Inicio de sesión
├── RegisterScreen.kt         # Registro de usuarios
├── HabitosScreen.kt          # Listado y registro de hábitos
├── DetalleHabitoScreen.kt    # Detalle de un hábito
├── PerfilScreen.kt           # Perfil del usuario
├── EditProfileScreen.kt      # Edición de perfil
├── ChangePasswordScreen.kt   # Cambio de contraseña
├── UserScreen.kt             # Administración de usuarios
├── data/
│   ├── ApiService.kt         # Endpoints de la API (Retrofit)
│   ├── Models.kt             # Modelos de datos
│   ├── RetrofitClient.kt     # Cliente HTTP y URL del backend
│   └── Session.kt            # Token y usuario de la sesión actual
└── ui/theme/                 # Colores, tema y tipografía
```

## Autores

- Santiago Varela Peña
- Sebastián Navarro López

Proyecto del programa Tecnología en Análisis y Desarrollo de Software (ADSO) del SENA, Ficha 3223873.

## Licencia

Distribuido bajo la licencia MIT. Consulta el archivo [LICENSE](LICENSE).