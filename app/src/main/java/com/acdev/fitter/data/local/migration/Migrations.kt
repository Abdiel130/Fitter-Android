package com.acdev.fitter.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migraciones de la base local.
 *
 * Nunca se recurre a `fallbackToDestructiveMigration`: los datos del usuario no existen en ningun
 * otro sitio mientras no haya servidor.
 */
object FitterMigrations {

    /**
     * v2: la lista de ejercicios deja de pertenecer a una rutina.
     *
     * Antes `workout` colgaba de `routine`, asi que una lista solo podia usarse en una rutina y
     * no se podia crear sin tener rutina. Ahora la lista es del usuario y la pertenencia a una
     * rutina, con su orden, vive en `routine_workouts`. La fila puente hereda el `orderIndex` que
     * tenia la lista, de modo que ninguna rutina existente cambia de secuencia.
     *
     * Ademas `exercises` gana `iconKey`, para que un ejercicio propio pueda elegir icono.
     */
    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `routine_workouts` (
                    `id` TEXT NOT NULL,
                    `routineId` TEXT NOT NULL,
                    `workoutId` TEXT NOT NULL,
                    `orderIndex` INTEGER NOT NULL,
                    `syncState` TEXT NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    `deletedAt` INTEGER,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`routineId`) REFERENCES `routine`(`id`)
                        ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(`workoutId`) REFERENCES `workout`(`id`)
                        ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO `routine_workouts`
                    (`id`, `routineId`, `workoutId`, `orderIndex`, `syncState`, `updatedAt`, `deletedAt`)
                SELECT `id`, `routineId`, `id`, `orderIndex`, `syncState`, `updatedAt`, `deletedAt`
                FROM `workout`
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_routine_workouts_routineId` " +
                    "ON `routine_workouts` (`routineId`)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_routine_workouts_workoutId` " +
                    "ON `routine_workouts` (`workoutId`)"
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `workout_v2` (
                    `id` TEXT NOT NULL,
                    `userId` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `notes` TEXT,
                    `createdAt` INTEGER NOT NULL,
                    `syncState` TEXT NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    `deletedAt` INTEGER,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`userId`) REFERENCES `users`(`id`)
                        ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO `workout_v2`
                    (`id`, `userId`, `name`, `notes`, `createdAt`, `syncState`, `updatedAt`, `deletedAt`)
                SELECT w.`id`, r.`userId`, w.`name`, NULL, w.`createdAt`,
                       w.`syncState`, w.`updatedAt`, w.`deletedAt`
                FROM `workout` w
                INNER JOIN `routine` r ON r.`id` = w.`routineId`
                """.trimIndent()
            )
            db.execSQL("DROP TABLE `workout`")
            db.execSQL("ALTER TABLE `workout_v2` RENAME TO `workout`")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_userId` ON `workout` (`userId`)")

            db.execSQL("ALTER TABLE `exercises` ADD COLUMN `iconKey` TEXT")
        }
    }

    val all: Array<Migration> = arrayOf(MIGRATION_1_2)
}
