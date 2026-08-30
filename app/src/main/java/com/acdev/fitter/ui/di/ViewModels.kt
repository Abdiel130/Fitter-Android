package com.acdev.fitter.ui.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.acdev.fitter.core.di.AppContainer
import com.acdev.fitter.ui.AppViewModel
import com.acdev.fitter.ui.feature.dashboard.DashboardViewModel
import com.acdev.fitter.ui.feature.onboarding.OnboardingViewModel

/**
 * Acceso al grafo de dependencias desde la composicion.
 *
 * Se provee una unica vez en `MainActivity`. Ningun composable busca el contenedor por su cuenta
 * ni lo recibe como parametro, y por eso las vistas de previsualizacion pueden proveer uno falso.
 */
val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer no disponible: falta CompositionLocalProvider en la actividad.")
}

/**
 * Fabricas de ViewModel.
 *
 * Cada una construye su ViewModel con dependencias explicitas. Al migrar a un inyector con
 * generacion de codigo, estas funciones desaparecen y las pantallas no cambian.
 */
object FitterViewModels {

    @Composable
    fun app(): AppViewModel {
        val container = LocalAppContainer.current
        return viewModel(
            factory = viewModelFactory {
                initializer {
                    AppViewModel(
                        preferencesRepository = container.preferencesRepository,
                        syncRepository = container.syncRepository
                    )
                }
            }
        )
    }

    @Composable
    fun dashboard(): DashboardViewModel {
        val container = LocalAppContainer.current
        return viewModel(
            factory = viewModelFactory {
                initializer {
                    DashboardViewModel(
                        observeDashboard = container.observeDashboard,
                        userRepository = container.userRepository,
                        recoveryRepository = container.recoveryRepository,
                        clock = container.clock
                    )
                }
            }
        )
    }

    @Composable
    fun onboarding(): OnboardingViewModel {
        val container = LocalAppContainer.current
        return viewModel(
            factory = viewModelFactory {
                initializer {
                    OnboardingViewModel(
                        preferencesRepository = container.preferencesRepository,
                        completeOnboarding = container.completeOnboarding
                    )
                }
            }
        )
    }
}
