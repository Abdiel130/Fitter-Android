package com.acdev.fitter.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.acdev.fitter.core.connectivity.AndroidConnectivityObserver
import com.acdev.fitter.core.connectivity.ConnectivityObserver
import com.acdev.fitter.core.time.AppClock
import com.acdev.fitter.core.time.SystemAppClock
import com.acdev.fitter.core.util.IdGenerator
import com.acdev.fitter.core.util.UuidGenerator
import com.acdev.fitter.data.local.FitterDatabase
import com.acdev.fitter.data.preferences.PreferencesRepositoryImpl
import com.acdev.fitter.data.repository.BodyRepositoryImpl
import com.acdev.fitter.data.repository.NutritionRepositoryImpl
import com.acdev.fitter.data.repository.RecoveryRepositoryImpl
import com.acdev.fitter.data.repository.SyncRepositoryImpl
import com.acdev.fitter.data.repository.TrainingRepositoryImpl
import com.acdev.fitter.data.repository.UserRepositoryImpl
import com.acdev.fitter.domain.repository.BodyRepository
import com.acdev.fitter.domain.repository.NutritionRepository
import com.acdev.fitter.domain.repository.PreferencesRepository
import com.acdev.fitter.domain.repository.RecoveryRepository
import com.acdev.fitter.domain.repository.SyncRepository
import com.acdev.fitter.domain.repository.TrainingRepository
import com.acdev.fitter.domain.repository.UserRepository
import com.acdev.fitter.domain.usecase.CompleteOnboardingUseCase
import com.acdev.fitter.domain.usecase.ObserveDashboardUseCase

/**
 * Grafo de dependencias de la app.
 *
 * Inyeccion manual a proposito: el proyecto tiene un unico modulo y todas las dependencias son
 * de larga vida, asi que un contenedor explicito se lee mejor que un generador de codigo. Todo se
 * expone por su interfaz de dominio, de modo que migrar a Hilt mas adelante es sustituir esta
 * clase por modulos sin tocar ni ViewModels ni pantallas.
 *
 * Todo es `lazy`: abrir la base de datos o el DataStore no ocurre hasta que alguien los pide.
 */
class AppContainer(context: Context) {

    private val appContext: Context = context.applicationContext

    val clock: AppClock = SystemAppClock()

    private val idGenerator: IdGenerator = UuidGenerator

    private val database: FitterDatabase by lazy { FitterDatabase.build(appContext) }

    private val dataStore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.create {
            appContext.preferencesDataStoreFile(PREFERENCES_NAME)
        }
    }

    private val connectivityObserver: ConnectivityObserver by lazy {
        AndroidConnectivityObserver(appContext)
    }

    val preferencesRepository: PreferencesRepository by lazy {
        PreferencesRepositoryImpl(dataStore)
    }

    val userRepository: UserRepository by lazy {
        UserRepositoryImpl(database.userDao(), clock, idGenerator)
    }

    val trainingRepository: TrainingRepository by lazy {
        TrainingRepositoryImpl(
            trainingDao = database.trainingDao(),
            exerciseDao = database.exerciseDao(),
            workoutLogDao = database.workoutLogDao(),
            userDao = database.userDao(),
            clock = clock,
            idGenerator = idGenerator
        )
    }

    val nutritionRepository: NutritionRepository by lazy {
        NutritionRepositoryImpl(database.nutritionDao(), idGenerator)
    }

    val recoveryRepository: RecoveryRepository by lazy {
        RecoveryRepositoryImpl(database.recoveryDao(), clock, idGenerator)
    }

    val bodyRepository: BodyRepository by lazy {
        BodyRepositoryImpl(database.bodyMeasurementDao(), clock, idGenerator)
    }

    val syncRepository: SyncRepository by lazy {
        SyncRepositoryImpl(database.syncDao(), connectivityObserver)
    }

    val observeDashboard: ObserveDashboardUseCase by lazy {
        ObserveDashboardUseCase(
            userRepository = userRepository,
            trainingRepository = trainingRepository,
            nutritionRepository = nutritionRepository,
            recoveryRepository = recoveryRepository,
            syncRepository = syncRepository,
            clock = clock
        )
    }

    val completeOnboarding: CompleteOnboardingUseCase by lazy {
        CompleteOnboardingUseCase(
            userRepository = userRepository,
            trainingRepository = trainingRepository,
            nutritionRepository = nutritionRepository,
            recoveryRepository = recoveryRepository,
            bodyRepository = bodyRepository,
            preferencesRepository = preferencesRepository,
            clock = clock
        )
    }

    private companion object {
        const val PREFERENCES_NAME = "fitter_preferences"
    }
}
