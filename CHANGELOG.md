# Registro de cambios

Todos los cambios relevantes de Fitter, en orden inverso: lo más reciente arriba.

El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y las versiones, el
[versionado semántico](https://semver.org/lang/es/). Mientras la versión mayor sea `0`, la API
interna y el esquema de la base pueden cambiar entre versiones menores.

**Cómo se redacta una entrada:** se describe *qué cambió para quien usa la app*, no qué ficheros,
clases o funciones se tocaron. «Ahora la app avisa cuando el servidor no responde» es una entrada;
«se añadió `RemoteError.ServerUnreachable` a `ApiCall.kt`» es un mensaje de commit. Una entrada que
solo tiene sentido leyendo el código no va aquí. Los cambios internos sin efecto visible se agrupan
bajo **Self** y en una sola línea.

**Self**.
Categorías: **Added**, **Changed**, **Deprecated**, **Removed**, **Fixed**, **Security**,

---

## [Unreleased]

## [0.0.1] - 2026-09-26

Primera versión registrada. Fitter funciona sin conexión de principio a fin: la base de datos del
teléfono es la fuente de verdad y el servidor solo aporta la identidad de la cuenta.

### Added

- **Inicio de sesión.** La app pide correo y contraseña antes de dar acceso. Las cuentas se crean
  fuera de la app; desde aquí solo se entra. La sesión se recuerda entre arranques y se cierra desde
  Ajustes, con confirmación.
- **Sesión protegida con biometría.** La credencial de larga duración se guarda cifrada por el
  hardware del teléfono y se desbloquea con huella. Al renovarse la sesión, la app pide la huella una
  vez y sigue donde estaba, sin sacar al usuario ni perder lo que estuviera haciendo.
- **Conexión al servidor sin configurar nada.** La app encuentra el servidor por su cuenta entre las
  direcciones conocidas y recuerda la que funcionó. Cuando la red cambia y la dirección deja de
  valer, la olvida y vuelve a buscar en el siguiente intento.
- **Avisos de red claros y distintos entre sí.** Se distingue no tener conexión, no encontrar el
  servidor, que el servidor tarde demasiado y que rechace las credenciales. Cada caso dice qué pasó
  y qué revisar, sin errores genéricos.
- **Asistente de primer arranque, cinco pasos.** Cuenta, paleta, unidades, objetivos y resumen. El
  servidor recuerda que ya se completó, así que solo aparece cuando de verdad hace falta.
- **Entrenamiento.** Rutinas por secuencia cíclica en vez de días fijos de la semana, listas de
  ejercicios y catálogo propio, todo editable.
- **Panel de inicio** con los datos reales del teléfono: qué toca hoy, progreso de la semana,
  medidas y objetivos.
- **Dos temas conmutables en caliente:** Midnight y Void, este último pensado para pantallas AMOLED.
- **Indicador de estado en la parte superior**, que informa de la sincronización pendiente y de la
  pérdida de conexión sin interrumpir lo que se está haciendo.

### Security

- El tráfico sin cifrar solo se permite en compilaciones de desarrollo, y únicamente hacia las
  direcciones locales declaradas. La app publicada exige HTTPS.
- Las direcciones de desarrollo y el panel para fijar una a mano no existen en la app publicada: son
  exclusivos de las compilaciones de depuración.
- Cerrar sesión o perderla **nunca borra los datos de entrenamiento del teléfono.** Son del usuario,
  no de la sesión. Al volver a entrar con la misma cuenta siguen ahí y se reconocen como suyos.

[Unrelease]: https://github.com/acdev/fitter/compare/v0.0.1...HEAD
[0.0.1]: https://github.com/acdev/fitter/releases/tag/v0.0.1
