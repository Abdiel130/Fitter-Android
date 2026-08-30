# Fitter

Aplicación Android de entrenamiento, antropometría, nutrición y recuperación. **Offline-first**: la base de datos local es la única fuente de verdad y el servidor es un destino de sincronización, nunca un requisito para usar la app.

---

## 1. Estado actual

| Área | Estado |
|---|---|
| Sistema de diseño (tema, tipografía, movimiento, iconos, componentes) | Completo |
| Dos paletas conmutables en caliente: Midnight y Void (AMOLED) | Completo |
| Isla dinámica (indicador de sistema y de sincronización) | Completo |
| Asistente de primer arranque (5 pasos) | Completo |
| Dashboard | Completo, con datos reales de la base local |
| Entreno / Nutrición / Progreso | Placeholder navegable |
| Perfil | Muestra la pantalla de Ajustes |
| Room: 26 entidades, 8 DAO, conversores, esquema exportado | Completo |
| Backend de servidor / API | **No implementado todavía** |
| Motor de sincronización (WorkManager) | **No implementado todavía**; el contrato de UI ya existe |

---

## 2. Producto

### 2.1 Entrenamiento semanal

Rutinas por **secuencia cíclica**, no por día de la semana. Una rutina (`routine`) agrupa días (`workout`) con un `orderIndex`; el usuario tiene un puntero (`users.activeSequenceIndex`) que indica qué toca hoy. Así dos rutinas pueden compartir los mismos días con distinto orden:

- Semana enfoque empuje → empuje, jale, pierna, empuje
- Semana enfoque jale → jale, empuje, pierna, jale

El usuario puede saltarse el orden recomendado: la secuencia es sugerencia, no obligación.

### 2.2 Sesión de entreno

- Calculadora de 1RM teórico sobre el peso normalizado a kg.
- Precarga del último peso registrado por ejercicio (`WorkoutLogDao.lastSetFor`).
- Cronómetro de descanso automático al marcar una serie, con notificación local.
- Calculadora de discos en ambos sentidos: peso total → discos por lado, y discos puestos → peso levantado.
- Conteo de series efectivas por músculo y semana, para detectar mínimo efectivo o sobreentrenamiento.
- Mapeo de ejercicios sustitutos cuando una máquina está ocupada (`exercise_substitutes`).

### 2.3 Antropometría y progreso

Un registro (`body_measurement`) es un conjunto de cambios de un día y **todos sus campos son opcionales**: puede ser solo peso, solo medidas, solo fotos, o todo junto. Las fotos (`progress_photos`) cuelgan del registro y se organizan por pose (frontal, espalda, perfil izquierdo, perfil derecho). El porcentaje de grasa es manual o calculado.

### 2.4 Nutrición

Hidratación con meta configurable, checklist diario de suplementos, banco de recetas y comidas frecuentes para registrar un bloque entero de un toque, y promedios semanales de macros comparados con la meta.

### 2.5 Recuperación

Escala rápida de calidad de sueño y energía (1-5) al despertar, y bitácora de molestias articulares (1-10) para ajustar la sesión del día.

---

## 3. Dirección de diseño: Pulse

De cuatro direcciones propuestas se eligió **Pulse**.

**Tesis.** Una cápsula negra permanente en la cabecera —la isla dinámica— absorbe todo lo que ocurre en segundo plano: descanso entre series, subida al servidor, récord recién roto. Crece, se transforma y vuelve a su tamaño mínimo. Debajo, superficies redondeadas y flotantes; el contenido nunca compite con la isla.

**Dos paletas conmutables desde Ajustes**, ambas con variante clara y oscura:

| Token | Midnight oscuro | Void oscuro (AMOLED) |
|---|---|---|
| `surface` | `#080A12` | `#000000` |
| `surfaceContainer` | `#141824` | `#000000` |
| `surfaceContainerHigh` | `#1C2233` | `#0B0D12` |
| `primary` | `#4DE1C1` | `#37D6B4` |
| `secondary` | `#7A8CFF` | `#6E80EE` |
| `record` (extendido) | `#FFB454` | `#E8A340` |
| `outlineVariant` | `#262D40` | `#1C222E` |

En Void los acentos bajan croma porque el color muy saturado sangra sobre negro puro, y la isla recibe borde propio (`islandBorder`) porque negro sobre negro desaparecería. Ese es el único punto donde las dos paletas difieren en estructura y no solo en valores.

**Movimiento.** Muelle `dampingRatio 0.72 / stiffness 380` para lo que cambia de tamaño; curva `(0.2, 0, 0, 1)` para lo que cambia de opacidad o posición. Nada aparece de golpe: todo crece desde su origen.

**Iconografía.** Vectores propios de 24 dp, trazo 1.9 dp, remates redondos, en `res/drawable/ic_*.xml`. **Nunca emoji.** No se usa `material-icons-extended`.

---

## 4. Arquitectura

Tres capas, dependencias en una sola dirección: `ui → domain ← data`.

```
com.acdev.fitter
├── FitterApplication.kt        Application; construye el AppContainer
├── MainActivity.kt             Única actividad; publica el contenedor y llama a FitterApp
│
├── core/                       Infraestructura sin reglas de negocio
│   ├── connectivity/           ConnectivityObserver (red)
│   ├── di/AppContainer.kt      Grafo de dependencias (inyección manual)
│   ├── time/AppClock.kt        Reloj inyectable
│   └── util/IdGenerator.kt     UUID generados en cliente
│
├── domain/                     Modelos, contratos y casos de uso. Sin Android.
│   ├── model/                  AppPreferences, Sync, Dashboard, Catalogs
│   ├── repository/             Interfaces de repositorio
│   └── usecase/                ObserveDashboardUseCase, CompleteOnboardingUseCase
│
├── data/                       Implementaciones
│   ├── local/                  Room: entity/, dao/, seed/, FitterDatabase, FitterConverters
│   ├── preferences/            DataStore
│   └── repository/             Implementaciones de los contratos de dominio
│
└── ui/
    ├── theme/                  Color, Type, Dimens, Motion, Theme
    ├── icons/FitterIcons.kt    Catálogo de iconos
    ├── components/             Componentes reutilizables (ver CLAUDE.md §5)
    ├── navigation/             Rutas con tipo seguro y transiciones
    ├── di/ViewModels.kt        LocalAppContainer y fábricas de ViewModel
    ├── feature/                Una carpeta por pantalla
    ├── AppViewModel.kt         Estado global: tema, unidades, sincronización
    └── FitterApp.kt            Raíz: asistente vs app, scaffold, NavHost
```

### Decisiones tomadas y por qué

- **Inyección manual (`AppContainer`) en vez de Hilt.** Un solo módulo y dependencias de larga vida: un contenedor explícito se lee mejor que código generado. Todo se expone por interfaz de dominio, así que migrar a Hilt es sustituir esa clase por módulos sin tocar ViewModels ni pantallas.
- **Rutas de navegación con tipo seguro** (`kotlinx.serialization`): no hay cadenas de ruta y el compilador detecta destinos inexistentes.
- **UUID generados en cliente.** Una fila tiene identidad desde que se escribe en local, de modo que la sincronización posterior no reconcilia identificadores.
- **La UI nunca llama a la API.** Escribe en Room a través de un repositorio; el repositorio marca la fila `PENDING` y encola trabajo.

---

## 5. Modelo de datos

26 entidades derivadas de `Fitter.sql`. Los **nombres de tabla** se mantienen en `snake_case` igual que el esquema del servidor; los **nombres de columna** son los de las propiedades Kotlin (`camelCase`), y la traducción al contrato del servidor ocurrirá en los DTO de la capa de red.

Grupos:

| Fichero | Entidades |
|---|---|
| `UserEntities.kt` | `users` |
| `TrainingEntities.kt` | `routine`, `workout`, `workout_exercises`, `workout_sets` |
| `ExerciseEntities.kt` | `exercises`, `body_parts`, `muscles`, `equipments`, tres tablas puente, `exercise_substitutes` |
| `LogEntities.kt` | `workout_logs`, `workout_log_sets` |
| `BodyEntities.kt` | `body_measurement`, `progress_photos` |
| `NutritionEntities.kt` | `nutrition_targets`, `food_items`, `recipes`, `recipe_items`, `daily_food_logs` |
| `RecoveryEntities.kt` | `daily_habit_logs`, `supplements`, `daily_supplement_logs`, `joint_discomfort_logs` |

### Correcciones aplicadas al esquema original

Tres puntos del SQL de partida no se trasladaron tal cual, a propósito:

1. **`supplements.user_id` apuntaba a `workout_exercises(id)`.** La última línea del script añade una clave ajena que contradice la anterior. Se modela contra `users(id)`, que es lo que el resto del esquema implica.
2. **`daily_food_logs` exigía `food_item_id` **y** `recipe_id` a la vez.** Una línea del diario es un alimento suelto **o** una receta, nunca ambos. Ambas columnas son opcionales y la exclusividad la garantiza el repositorio al insertar.
3. **`body_measurement.weight_kg` era obligatorio.** El producto dice que un registro puede ser solo foto o solo medidas, así que el peso es opcional.

Además, toda tabla propiedad del usuario incorpora `SyncMetadata` (`syncState`, `updatedAt`, `deletedAt`) mediante `@Embedded`. `deletedAt` implementa borrado lógico: nada desaparece de verdad hasta que el servidor confirma la baja.

### Datos de arranque

`StarterCatalog` instala en el primer uso 17 ejercicios con su músculo, zona y material, y una rutina PPL de 4 días editable, más una lista de suplementos habituales. Son plantillas, no datos falsos de progreso: el dashboard muestra ceros reales hasta que el usuario registra algo.

---

## 6. Sincronización

Diseñada pero aún no conectada. El contrato que consume la UI ya es el definitivo.

**Cómo se rastrea, de forma automática:**

1. Cada tabla sincronizable lleva `syncState` (`PENDING` / `SYNCING` / `SYNCED` / `FAILED` / `CONFLICT`), `updatedAt` y `deletedAt`.
2. Escribir a través de un repositorio marca la fila `PENDING` y encola trabajo. Lo hace el repositorio, no la pantalla.
3. **WorkManager** con `NetworkType.CONNECTED`, backoff exponencial y trabajo único por dominio ejecutará la subida. La app funciona igual en modo avión.
4. `SyncDao.observeCounts()` es la **única** consulta que conoce la lista completa de tablas sincronizables y produce un `Flow<SyncStatus>` global.
5. Ese flujo alimenta la isla dinámica y nada más. No hay iconos de nube repartidos por las pantallas.

**Estados en la isla:** punto verde `al día`, ámbar `N sin enviar`, gris `sin conexión`, rojo `error`. Durante una subida la isla se expande sola con el contador y vuelve a colapsar.

**Pendiente para conectar el backend:** capa `data/remote` (DTO + cliente HTTP), `SyncWorker` por dominio, resolución de conflictos por `updatedAt`, y sustituir `isSyncing = false` en `SyncRepositoryImpl` por el estado real del trabajo.

---

## 7. Compilar y ejecutar

```bash
./gradlew :app:assembleDebug      # APK de depuración
./gradlew :app:testDebugUnitTest  # pruebas unitarias
./gradlew :app:installDebug       # instalar en un dispositivo conectado
```

| Herramienta | Versión |
|---|---|
| AGP | 9.2.1 |
| Kotlin | 2.2.10 |
| Compose BOM | 2026.02.01 |
| Room | 2.8.2 |
| KSP | 2.2.10-2.0.2 |
| compileSdk / targetSdk / minSdk | 37 / 37 / 34 |

**Nota de configuración:** `gradle.properties` fija `android.disallowKotlinSourceSets=false`. KSP registra sus fuentes generadas vía `kotlin.sourceSets`, algo que AGP 9 con Kotlin integrado prohíbe por defecto. Se puede quitar cuando KSP soporte `android.sourceSets` de forma nativa.

`compileSdk` es 37 porque `androidx.core:core-ktx:1.19.0` lo exige.

---

## 8. Próximos pasos sugeridos

1. Pantalla de entreno en vivo: series, cronómetro con notificación local, calculadora de discos, 1RM. La isla ya soporta el estado `Rest`; solo falta quien lo emita.
2. Capa `data/remote` y `SyncWorker`.
3. Nutrición: escaneo de código de barras contra Open Food Facts y banco de recetas.
4. Progreso: historial de peso y medidas, fotos por pose, series efectivas por músculo (`WorkoutLogDao.observeVolumeByMuscle` ya existe).
5. Autenticación real; el paso de cuenta del asistente hoy solo guarda nombre y correo en el dispositivo.
