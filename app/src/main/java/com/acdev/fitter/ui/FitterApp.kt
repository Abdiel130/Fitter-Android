package com.acdev.fitter.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.res.stringResource
import com.acdev.fitter.R
import com.acdev.fitter.domain.model.AppPreferences
import com.acdev.fitter.ui.components.FitterBottomBar
import com.acdev.fitter.ui.components.PlaceholderScreen
import com.acdev.fitter.ui.components.PulseLoader
import com.acdev.fitter.ui.components.island.DynamicIsland
import com.acdev.fitter.ui.components.island.resolveIslandState
import com.acdev.fitter.ui.di.FitterViewModels
import com.acdev.fitter.ui.feature.dashboard.DashboardScreen
import com.acdev.fitter.ui.feature.onboarding.OnboardingActions
import com.acdev.fitter.ui.feature.onboarding.OnboardingScreen
import com.acdev.fitter.ui.feature.settings.SettingsScreen
import com.acdev.fitter.ui.icons.FitterIcons
import com.acdev.fitter.ui.navigation.FitterRoute
import com.acdev.fitter.ui.navigation.FitterTransitions
import com.acdev.fitter.ui.navigation.TopLevelDestination
import com.acdev.fitter.ui.theme.FitterMotion
import com.acdev.fitter.ui.theme.FitterTheme

/**
 * Raiz de la interfaz.
 *
 * Decide tres cosas y nada mas: si ya hay preferencias cargadas, con que tema pintar y si toca el
 * asistente o la app. Toda la navegacion cuelga de aqui.
 */
@Composable
fun FitterApp() {
    val appViewModel = FitterViewModels.app()
    val preferences by appViewModel.preferences.collectAsStateWithLifecycle()

    val current = preferences
    if (current == null) {
        BootScreen()
        return
    }

    FitterTheme(palette = current.palette, themeMode = current.themeMode) {
        AnimatedContent(
            targetState = current.onboardingCompleted,
            transitionSpec = {
                fadeIn(FitterMotion.standardTween(FitterMotion.Duration.EMPHASIZED)) togetherWith
                    fadeOut(FitterMotion.standardTween())
            },
            label = "appRoot"
        ) { completed ->
            if (completed) {
                MainScaffold(appViewModel = appViewModel, preferences = current)
            } else {
                OnboardingRoute()
            }
        }
    }
}

/**
 * Pantalla de arranque mientras se leen las preferencias.
 *
 * Usa el color de fondo de Midnight oscuro porque es el tema por defecto y evita el destello
 * blanco del sistema.
 */
@Composable
private fun BootScreen() {
    FitterTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            PulseLoader(contentDescription = stringResource(R.string.state_loading))
        }
    }
}

@Composable
private fun OnboardingRoute() {
    val viewModel = FitterViewModels.onboarding()
    val state by viewModel.state.collectAsStateWithLifecycle()

    OnboardingScreen(
        state = state,
        actions = remember(viewModel) {
            OnboardingActions(
                onNext = viewModel::next,
                onBack = viewModel::back,
                onFinish = viewModel::finish,
                onPaletteChange = viewModel::selectPalette,
                onThemeModeChange = viewModel::selectThemeMode,
                onWeightUnitChange = viewModel::selectWeightUnit,
                onWeightChange = viewModel::updateWeight,
                onWaistChange = viewModel::updateWaist,
                onChestChange = viewModel::updateChest,
                onNameChange = viewModel::updateName,
                onEmailChange = viewModel::updateEmail
            )
        }
    )
}

@Composable
private fun MainScaffold(appViewModel: AppViewModel, preferences: AppPreferences) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val syncStatus by appViewModel.syncStatus.collectAsStateWithLifecycle()

    val selectedDestination = TopLevelDestination.entries.firstOrNull { destination ->
        backStackEntry?.destination?.hasRoute(destination.route::class) == true
    } ?: TopLevelDestination.DASHBOARD

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        bottomBar = {
            FitterBottomBar(
                destinations = TopLevelDestination.entries,
                selected = selectedDestination,
                onSelect = { destination -> navController.navigateToTab(destination) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // La isla vive fuera del grafo: cualquier pantalla la hereda sin declararla.
            DynamicIsland(
                state = resolveIslandState(record = null, rest = null, syncStatus = syncStatus),
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(bottom = FitterTheme.spacing.md)
                    .align(Alignment.CenterHorizontally)
            )

            FitterNavHost(
                navController = navController,
                preferences = preferences,
                appViewModel = appViewModel,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun FitterNavHost(
    navController: NavHostController,
    preferences: AppPreferences,
    appViewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = FitterRoute.Dashboard,
        modifier = modifier,
        enterTransition = FitterTransitions.tabEnter,
        exitTransition = FitterTransitions.tabExit,
        popEnterTransition = FitterTransitions.backEnter,
        popExitTransition = FitterTransitions.backExit
    ) {
        composable<FitterRoute.Dashboard> {
            val viewModel = FitterViewModels.dashboard()
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            DashboardScreen(
                state = state,
                onStartSession = { navController.navigate(FitterRoute.Training) },
                onAddWater = viewModel::addWaterGlass
            )
        }

        composable<FitterRoute.Training> {
            PlaceholderScreen(
                title = stringResource(R.string.nav_training),
                message = stringResource(R.string.placeholder_training),
                icon = FitterIcons.Dumbbell
            )
        }

        composable<FitterRoute.Nutrition> {
            PlaceholderScreen(
                title = stringResource(R.string.nav_nutrition),
                message = stringResource(R.string.placeholder_nutrition),
                icon = FitterIcons.Nutrition
            )
        }

        composable<FitterRoute.Progress> {
            PlaceholderScreen(
                title = stringResource(R.string.nav_progress),
                message = stringResource(R.string.placeholder_progress),
                icon = FitterIcons.Chart
            )
        }

        composable<FitterRoute.Profile> {
            SettingsScreen(
                preferences = preferences,
                onPaletteChange = appViewModel::setPalette,
                onThemeModeChange = appViewModel::setThemeMode,
                onWeightUnitChange = appViewModel::setWeightUnit
            )
        }

        composable<FitterRoute.Settings>(
            enterTransition = FitterTransitions.forwardEnter,
            exitTransition = FitterTransitions.forwardExit
        ) {
            SettingsScreen(
                preferences = preferences,
                onPaletteChange = appViewModel::setPalette,
                onThemeModeChange = appViewModel::setThemeMode,
                onWeightUnitChange = appViewModel::setWeightUnit
            )
        }
    }
}

/**
 * Cambio de pestana.
 *
 * Guarda el estado de la pestana que se abandona y restaura el de la que se abre, de modo que
 * volver a Inicio no reinicia el desplazamiento ni recarga la base.
 */
private fun NavHostController.navigateToTab(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
