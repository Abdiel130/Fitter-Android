package com.acdev.fitter.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.acdev.fitter.BuildConfig
import com.acdev.fitter.R
import com.acdev.fitter.core.connectivity.AndroidConnectivityObserver
import com.acdev.fitter.core.connectivity.ConnectivityObserver
import com.acdev.fitter.core.network.AndroidDeviceNameProvider
import com.acdev.fitter.core.network.AuthHeaderInterceptor
import com.acdev.fitter.core.network.BaseUrlCandidates
import com.acdev.fitter.core.network.BaseUrlInterceptor
import com.acdev.fitter.core.network.BaseUrlProbe
import com.acdev.fitter.core.network.BaseUrlResolver
import com.acdev.fitter.core.network.BaseUrlStore
import com.acdev.fitter.core.network.DefaultBaseUrlResolver
import com.acdev.fitter.core.network.DeviceNameProvider
import com.acdev.fitter.core.network.NetworkClients
import com.acdev.fitter.core.network.OkHttpBaseUrlProbe
import com.acdev.fitter.core.network.TokenAuthenticator
import com.acdev.fitter.core.network.TokenRefresher
import com.acdev.fitter.core.security.AndroidBiometricGate
import com.acdev.fitter.core.security.BiometricGate
import com.acdev.fitter.core.security.CryptoBox
import com.acdev.fitter.core.security.CurrentActivityProvider
import com.acdev.fitter.core.security.KeystoreCryptoBox
import com.acdev.fitter.core.time.AppClock
import com.acdev.fitter.core.time.SystemAppClock
import com.acdev.fitter.core.util.IdGenerator
import com.acdev.fitter.core.util.UuidGenerator
import com.acdev.fitter.data.auth.AuthRepositoryImpl
import com.acdev.fitter.data.local.FitterDatabase
import com.acdev.fitter.data.preferences.PreferencesRepositoryImpl
import com.acdev.fitter.data.remote.RemoteTokenRefresher
import com.acdev.fitter.data.remote.api.AuthApi
import com.acdev.fitter.data.remote.api.ProfileApi
import com.acdev.fitter.data.repository.BodyRepositoryImpl
import com.acdev.fitter.data.repository.ExerciseRepositoryImpl
import com.acdev.fitter.data.repository.NutritionRepositoryImpl
import com.acdev.fitter.data.repository.RecoveryRepositoryImpl
import com.acdev.fitter.data.repository.RoutineRepositoryImpl
import com.acdev.fitter.data.repository.SyncRepositoryImpl
import com.acdev.fitter.data.repository.TrainingRepositoryImpl
import com.acdev.fitter.data.repository.UserRepositoryImpl
import com.acdev.fitter.data.repository.WorkoutRepositoryImpl
import com.acdev.fitter.data.session.BaseUrlStoreImpl
import com.acdev.fitter.data.session.DataStoreTokenStorage
import com.acdev.fitter.data.session.SessionRepositoryImpl
import com.acdev.fitter.data.session.TokenStorage
import com.acdev.fitter.domain.repository.AuthRepository
import com.acdev.fitter.domain.repository.BodyRepository
import com.acdev.fitter.domain.repository.ExerciseRepository
import com.acdev.fitter.domain.repository.NutritionRepository
import com.acdev.fitter.domain.repository.PreferencesRepository
import com.acdev.fitter.domain.repository.RecoveryRepository
import com.acdev.fitter.domain.repository.RoutineRepository
import com.acdev.fitter.domain.repository.SessionRepository
import com.acdev.fitter.domain.repository.SyncRepository
import com.acdev.fitter.domain.repository.TrainingRepository
import com.acdev.fitter.domain.repository.UserRepository
import com.acdev.fitter.domain.repository.WorkoutRepository
import com.acdev.fitter.domain.usecase.CompleteOnboardingUseCase
import com.acdev.fitter.domain.usecase.ObserveDashboardUseCase
import com.acdev.fitter.domain.usecase.SignInUseCase
import com.acdev.fitter.domain.usecase.SignOutUseCase
import com.acdev.fitter.domain.usecase.SyncSetupFlagUseCase
import okhttp3.OkHttpClient
import retrofit2.Retrofit

/**
 * Grafo de dependencias de la app.
 *
 * Inyeccion manual a proposito: el proyecto tiene un unico modulo y todas las dependencias son
 * de larga vida, asi que un contenedor explicito se lee mejor que un generador de codigo. Todo se
 * expone por su interfaz de dominio, de modo que migrar a Hilt mas adelante es sustituir esta
 * clase por modulos sin tocar ni ViewModels ni pantallas.
 *
 * Todo es `lazy`: abrir la base de datos, el DataStore o el cliente HTTP no ocurre hasta que
 * alguien los pide.
 */
class AppContainer(context: Context) {

    private val appContext: Context = context.applicationContext

    val clock: AppClock = SystemAppClock()

    private val idGenerator: IdGenerator = UuidGenerator

    /** Se registra en `FitterApplication` para que la biometria encuentre una actividad viva. */
    val activityProvider: CurrentActivityProvider = CurrentActivityProvider()

    private val database: FitterDatabase by lazy { FitterDatabase.build(appContext) }

    private val dataStore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.create {
            appContext.preferencesDataStoreFile(PREFERENCES_NAME)
        }
    }

    val connectivityObserver: ConnectivityObserver by lazy {
        AndroidConnectivityObserver(appContext)
    }

    private val cryptoBox: CryptoBox by lazy { KeystoreCryptoBox() }

    private val biometricGate: BiometricGate by lazy {
        AndroidBiometricGate(appContext, activityProvider)
    }

    private val tokenStorage: TokenStorage by lazy {
        DataStoreTokenStorage(dataStore, cryptoBox, biometricGate)
    }

    val baseUrlStore: BaseUrlStore by lazy { BaseUrlStoreImpl(dataStore) }

    private val baseUrlProbe: BaseUrlProbe by lazy { OkHttpBaseUrlProbe() }

    val baseUrlResolver: BaseUrlResolver by lazy {
        DefaultBaseUrlResolver(
            candidates = BaseUrlCandidates.from(BuildConfig.API_BASE_URLS),
            store = baseUrlStore,
            probe = baseUrlProbe,
            probeEnabled = BuildConfig.API_PROBE_ENABLED
        )
    }

    private val deviceNameProvider: DeviceNameProvider by lazy {
        AndroidDeviceNameProvider(appContext.getString(R.string.auth_device_name))
    }

    /**
     * Cliente sin autenticador, reservado a la renovacion de tokens.
     *
     * Tenerlo aparte es lo que impide una recursion: si la renovacion falla con 401, ese 401 no
     * puede volver a entrar en el autenticador y pedir otra renovacion.
     */
    private val bareClient: OkHttpClient by lazy {
        NetworkClients.okHttp(
            baseUrlInterceptor = BaseUrlInterceptor(baseUrlResolver),
            authHeaderInterceptor = AuthHeaderInterceptor(tokenStorage),
            authenticator = null
        )
    }

    private val tokenRefresher: TokenRefresher by lazy {
        RemoteTokenRefresher(
            authApi = NetworkClients.retrofit(bareClient, NetworkClients.json)
                .create(AuthApi::class.java),
            json = NetworkClients.json
        )
    }

    private val client: OkHttpClient by lazy {
        NetworkClients.okHttp(
            baseUrlInterceptor = BaseUrlInterceptor(baseUrlResolver),
            authHeaderInterceptor = AuthHeaderInterceptor(tokenStorage),
            authenticator = TokenAuthenticator(
                tokenStorage = tokenStorage,
                refresher = tokenRefresher,
                connectivityObserver = connectivityObserver,
                onSessionRejected = { sessionRepository.clearSession() }
            )
        )
    }

    private val retrofit: Retrofit by lazy {
        NetworkClients.retrofit(client, NetworkClients.json)
    }

    val preferencesRepository: PreferencesRepository by lazy {
        PreferencesRepositoryImpl(dataStore)
    }

    val sessionRepository: SessionRepository by lazy {
        SessionRepositoryImpl(dataStore, tokenStorage, clock)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(
            authApi = retrofit.create(AuthApi::class.java),
            profileApi = retrofit.create(ProfileApi::class.java),
            deviceNameProvider = deviceNameProvider,
            json = NetworkClients.json
        )
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

    val routineRepository: RoutineRepository by lazy {
        RoutineRepositoryImpl(
            trainingDao = database.trainingDao(),
            userDao = database.userDao(),
            clock = clock,
            idGenerator = idGenerator
        )
    }

    val workoutRepository: WorkoutRepository by lazy {
        WorkoutRepositoryImpl(
            trainingDao = database.trainingDao(),
            clock = clock,
            idGenerator = idGenerator
        )
    }

    val exerciseRepository: ExerciseRepository by lazy {
        ExerciseRepositoryImpl(
            exerciseDao = database.exerciseDao(),
            trainingDao = database.trainingDao(),
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

    private val syncSetupFlag: SyncSetupFlagUseCase by lazy {
        SyncSetupFlagUseCase(
            authRepository = authRepository,
            sessionRepository = sessionRepository,
            preferencesRepository = preferencesRepository
        )
    }

    val signIn: SignInUseCase by lazy {
        SignInUseCase(
            authRepository = authRepository,
            sessionRepository = sessionRepository,
            preferencesRepository = preferencesRepository,
            userRepository = userRepository,
            syncSetupFlag = syncSetupFlag
        )
    }

    val signOut: SignOutUseCase by lazy {
        SignOutUseCase(authRepository = authRepository, sessionRepository = sessionRepository)
    }

    val completeOnboarding: CompleteOnboardingUseCase by lazy {
        CompleteOnboardingUseCase(
            userRepository = userRepository,
            trainingRepository = trainingRepository,
            nutritionRepository = nutritionRepository,
            recoveryRepository = recoveryRepository,
            bodyRepository = bodyRepository,
            preferencesRepository = preferencesRepository,
            sessionRepository = sessionRepository,
            syncSetupFlag = syncSetupFlag,
            clock = clock
        )
    }

    private companion object {
        const val PREFERENCES_NAME = "fitter_preferences"
    }
}
