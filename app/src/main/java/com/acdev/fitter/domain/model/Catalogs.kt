package com.acdev.fitter.domain.model

/** Tipo de serie, tanto planificada como ejecutada. */
enum class SetType { NORMAL, WARMUP, DROPSET, FAILURE }

/** Pose de una foto de progreso. */
enum class PoseType { FRONT, BACK, LEFT, RIGHT }

/** Momento del dia al que pertenece una linea del diario de comidas. */
enum class MealType { BREAKFAST, LUNCH, DINNER, SNACK }
