# Estándares de código — Fitter

Reglas obligatorias para cualquier persona o agente que escriba código en este repositorio. El contexto del producto y del modelo de datos está en [README.md](README.md).

Principio rector: **antes de crear algo, busca lo que ya existe.** Este proyecto tiene sistema de diseño, componentes, utilidades y contratos ya definidos. Añadir un color, un componente o una utilidad paralela es un error de revisión, no una preferencia de estilo.

---

## 1. Reglas que no se negocian

1. **Cero cadenas de texto en el código.** Todo texto visible va en `res/values/strings.xml` y se lee con `stringResource(R.string.x)`. Incluye placeholders tipográficos como el guion largo (`R.string.value_empty`).
2. **Cero colores literales en composables.** El color sale de `MaterialTheme.colorScheme` o de `FitterTheme.colors`. Si falta un color, se añade a `FitterExtendedColors` **y se define en las cuatro combinaciones** (Midnight/Void × claro/oscuro) en `ui/theme/Color.kt`.
3. **Cero `dp` sueltos para espaciado.** Se usa `FitterTheme.spacing`, `FitterTheme.radius` y `FitterTheme.sizes`.
4. **Cero emoji en la interfaz.** Iconografía vectorial vía `FitterIcons`. Para añadir uno: crear `res/drawable/ic_<nombre>.xml` con viewport 24, trazo 1.9, remates redondos, y exponerlo en `FitterIcons`.
5. **Cero duraciones o curvas de animación inventadas.** Se elige una de `FitterMotion`.
6. **La UI nunca toca Room, DataStore ni la red directamente.** Pasa por un repositorio, siempre a través de su interfaz de dominio.
7. **Nada de `if/else` anidados.** Se usa `when`, retorno temprano o extracción de función. Si una función tiene más de dos niveles de indentación lógica, hay que partirla.

---

## 2. Arquitectura y SOLID

`ui → domain ← data`. `domain` no importa nada de Android ni de Room.

- **Responsabilidad única.** Un repositorio por dominio (`TrainingRepository`, `NutritionRepository`, …). Un caso de uso hace una cosa. Una pantalla pinta y delega.
- **Abierto/cerrado.** Añadir una pestaña es añadir una constante a `TopLevelDestination`; añadir un estado a la isla es añadir un `data class` a `IslandState`. Ningún `when` de la UI debería crecer sin que el compilador te obligue.
- **Sustitución de Liskov.** Las implementaciones respetan el contrato: si `observeX` devuelve un `Flow`, emite también el estado vacío, nunca se queda callado.
- **Segregación de interfaces.** Interfaces pequeñas por dominio, nunca un `Repository` general.
- **Inversión de dependencias.** ViewModels y casos de uso reciben **interfaces** por constructor. `AppContainer` es el único sitio que conoce implementaciones concretas.

### Cómo se construye un ViewModel

Dependencias explícitas por constructor y fábrica en `ui/di/ViewModels.kt`:

```kotlin
@Composable
fun miPantalla(): MiViewModel {
    val container = LocalAppContainer.current
    return viewModel(
        factory = viewModelFactory {
            initializer { MiViewModel(container.miRepositorio, container.clock) }
        }
    )
}
```

Nunca `LocalContext.current as Application` dentro de una pantalla, ni un `object` singleton mutable.

### Reglas de estado

- El estado de pantalla es un `data class` inmutable expuesto como `StateFlow`.
- Se recoge con `collectAsStateWithLifecycle()`, nunca con `collectAsState()`.
- `stateIn(..., SharingStarted.WhileSubscribed(5_000), valorInicial)` para que un giro de pantalla no recargue la base.
- Nada de `LiveData`, nada de `mutableStateOf` público en el ViewModel.
- Un composable **sin estado** recibe datos y lambdas; el que tiene estado vive en `FitterApp.kt` o en el `NavHost`.

---

## 3. Fechas, números e identificadores

- **Nunca** `Instant.now()` ni `LocalDate.now()` en el código de producción: se inyecta `AppClock`.
- **Nunca** `UUID.randomUUID()` directo: se inyecta `IdGenerator`.
- Los pesos se guardan **siempre en kilogramos**. La conversión ocurre en el borde (`WeightUnit.toKg` / `fromKg`), no en la base ni en los cálculos.
- El formateo para pantalla se hace con `ui/util/Format.kt`, que respeta el idioma del dispositivo. No se llama a `String.format` suelto en un composable.

---

## 4. Room

- Una entidad por tabla; se agrupan por dominio en `data/local/entity/*Entities.kt`.
- Toda tabla propiedad del usuario incluye `@Embedded val sync: SyncMetadata`.
- Borrado **lógico**: se escribe `deletedAt` y toda consulta filtra `deletedAt IS NULL`.
- Cada columna con clave ajena lleva su `@Index`.
- Los enums se guardan **por nombre**, con `@TypeConverter` en `FitterConverters`. Reordenar un enum no debe corromper datos.
- `Instant` → epoch millis. `LocalDate` → texto ISO, para que una fecha de registro no se mueva al cambiar de zona horaria.
- **Prohibido** `fallbackToDestructiveMigration` fuera de depuración: los datos del usuario no existen en ningún otro sitio. Subir `version` exige una `Migration` explícita y el esquema exportado en `app/schemas` se versiona en git.
- `SyncDao.observeCounts()` es el único punto que conoce la lista completa de tablas sincronizables. Al añadir una tabla sincronizable, se suma ahí.
- Los métodos de escritura son `suspend`; las lecturas observables devuelven `Flow`.

---

## 5. Sistema de diseño

### Tokens

| Qué | Dónde |
|---|---|
| Roles de color estándar | `MaterialTheme.colorScheme` |
| Colores propios de Fitter | `FitterTheme.colors` (`FitterExtendedColors`) |
| Espaciado | `FitterTheme.spacing` |
| Radios | `FitterTheme.radius` |
| Tamaños de icono, anillo, isla | `FitterTheme.sizes` |
| Duraciones, curvas y muelles | `FitterMotion` |
| Escala tipográfica | `MaterialTheme.typography` y `FitterTextStyles` |

Para cambiar la tipografía del proyecto entero basta con sustituir `FitterDisplayFamily` y `FitterBodyFamily` en `ui/theme/Type.kt`: ningún composable referencia una `FontFamily` directamente.

### Componentes existentes — úsalos antes de escribir uno nuevo

| Componente | Para qué |
|---|---|
| `FitterScreen` | Armazón de pantalla: margen lateral, ritmo vertical y desplazamiento |
| `FitterCard` | Toda superficie elevada, con o sin pulsación |
| `SectionHeader` | Rótulo de sección |
| `MetricChip` | Dato compacto con icono; si recibe `onClick` es una acción rápida |
| `ProgressRing` | Anillo de progreso animado |
| `PulseLoader` | **El** indicador de carga. No se usa `CircularProgressIndicator` |
| `FitterPrimaryButton` / `FitterTextButton` | Acciones |
| `FitterTextField` | Campo de texto |
| `SelectableOptionCard` | Opción única en formato tarjeta |
| `SegmentedSelector` | Elección corta y excluyente |
| `FitterBottomBar` | Barra inferior |
| `DynamicIsland` | Estado del sistema y de la sincronización |
| `PlaceholderScreen` | Pantalla aún no construida |
| `Modifier.pressScale` | Respuesta táctil estándar |

### Movimiento

- Muelle para lo que cambia de **tamaño** (`FitterMotion.islandSpring`, `gentleSpring`).
- Curva para lo que cambia de **opacidad o posición** (`standardTween`, `emphasizedTween`).
- Transiciones de navegación en `FitterTransitions`, aplicadas en el `NavHost`, nunca pantalla por pantalla: fundido entre pestañas hermanas, desplazamiento lateral hacia un detalle.
- Nada escala más de un 4 % al pulsarse.

### Accesibilidad

- Todo icono accionable lleva `contentDescription`; los decorativos, `null`.
- Área táctil mínima `FitterTheme.sizes.minTouchTarget` (48 dp).
- Contraste objetivo 4.5:1 en texto y 3:1 en iconos, **en las cuatro combinaciones de tema**.

---

## 6. Añadir una pantalla nueva

1. Añadir el destino a `FitterRoute` (y a `TopLevelDestination` si es pestaña).
2. Crear `ui/feature/<nombre>/` con `<Nombre>Screen.kt` y `<Nombre>ViewModel.kt`.
3. El ViewModel recibe **interfaces** de dominio por constructor; se registra su fábrica en `FitterViewModels`.
4. Si necesita datos nuevos: primero el contrato en `domain/repository`, luego la implementación en `data/repository`, luego el DAO.
5. La pantalla usa `FitterScreen` y los componentes de §5.
6. Registrarla en el `NavHost` de `FitterApp.kt`. No se declaran transiciones salvo que sea un detalle.
7. Textos nuevos a `strings.xml`.

**Sustituir un placeholder** es cambiar la llamada a `PlaceholderScreen` por la pantalla real. No hay que limpiar nada más.

---

## 7. Rendimiento

- Todo `lazy` en `AppContainer`: abrir la base o el DataStore no ocurre hasta que alguien lo pide.
- Nada de trabajo pesado en `Application.onCreate`.
- Listas largas con `LazyColumn` y `key` estable.
- Se evita recomponer de más: parámetros estables, `data class` inmutables, lambdas recordadas cuando se pasan hacia abajo (`remember(viewModel) { ... }`).
- No se añaden dependencias que arrastren cientos de recursos sin usar. `material-icons-extended` está descartada por eso.
- Las consultas agregadas se resuelven en SQL, no recorriendo listas en Kotlin.

---

## 8. Estilo de Kotlin

- Máximo 100 caracteres por línea.
- Constantes con nombre en lugar de números mágicos, declaradas `private const val` arriba del fichero o en un `companion object` privado.
- Funciones cortas y con un solo nivel de abstracción.
- `sealed interface` para estados cerrados; `enum class` para catálogos.
- Nada de `!!`. Nada de `runCatching` que se traga el error sin registrarlo.
- Los comentarios explican **por qué**, no **qué**. Un comentario que repite el nombre de la función sobra.
- Comentarios y documentación en español, sin tildes en identificadores.

---

## 9. Pruebas

- El dominio se prueba sin Android: `AppClock` e `IdGenerator` son inyectables precisamente para eso.
- Se prueba lo que tiene bordes: conversión de unidades, progresos con meta cero, estados de la cola de sincronización.
- Los tests viven en `app/src/test/` y se ejecutan con `./gradlew :app:testDebugUnitTest`.

---

## 10. Antes de dar algo por terminado

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

Ambos deben pasar sin advertencias nuevas del compilador. Si añadiste una entidad o cambiaste el esquema, comprueba que `app/schemas` refleja el cambio y que existe la migración.
