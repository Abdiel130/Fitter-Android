package com.acdev.fitter.ui.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.acdev.fitter.R
import com.acdev.fitter.domain.model.DashboardSnapshot
import com.acdev.fitter.domain.model.NextSession
import com.acdev.fitter.ui.components.FitterCard
import com.acdev.fitter.ui.components.FitterScreen
import com.acdev.fitter.ui.components.MetricChip
import com.acdev.fitter.ui.components.ProgressRing
import com.acdev.fitter.ui.components.PulseLoader
import com.acdev.fitter.ui.components.SectionHeader
import com.acdev.fitter.ui.icons.FitterIcons
import com.acdev.fitter.ui.theme.FitterTheme
import com.acdev.fitter.ui.util.Format

/**
 * Inicio.
 *
 * Orden de lectura deliberado: quien eres y que toca hoy, luego el estado del dia de un vistazo y
 * al final los objetivos. Todo lo que ocurre en segundo plano vive en la isla, arriba, fuera de
 * esta pantalla.
 */
@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onStartSession: () -> Unit,
    onAddWater: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.isLoading) {
        LoadingState(modifier)
        return
    }

    FitterScreen(modifier = modifier) {
        GreetingHeader(state)
        NextSessionCard(state.snapshot.nextSession, onStartSession)
        TodayChips(state.snapshot, onAddWater)
        GoalsSection(state.snapshot)
    }
}

@Composable
private fun LoadingState(modifier: Modifier) {
    FitterScreen(modifier = modifier, scrollable = false) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = FitterTheme.spacing.huge),
            horizontalArrangement = Arrangement.Center
        ) {
            PulseLoader(contentDescription = stringResource(R.string.state_loading))
        }
    }
}

@Composable
private fun GreetingHeader(state: DashboardUiState) {
    val greetingText = when (state.greeting) {
        Greeting.MORNING -> stringResource(R.string.dashboard_greeting_morning)
        Greeting.AFTERNOON -> stringResource(R.string.dashboard_greeting_afternoon)
        Greeting.EVENING -> stringResource(R.string.dashboard_greeting_evening)
    }
    val name = state.snapshot.userName

    Column(verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.xxs)) {
        Text(
            text = greetingText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = name.ifBlank { stringResource(R.string.dashboard_today_title) },
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun NextSessionCard(session: NextSession?, onStartSession: () -> Unit) {
    if (session == null) {
        FitterCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.dashboard_no_routine_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.dashboard_no_routine_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = FitterTheme.spacing.xs)
            )
        }
        return
    }

    FitterCard(modifier = Modifier.fillMaxWidth(), onClick = onStartSession) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(FitterTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.dashboard_next_session).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = session.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = FitterTheme.spacing.xs),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(
                        R.string.dashboard_session_summary,
                        session.exerciseCount,
                        session.setCount
                    ) + " · " + stringResource(
                        R.string.dashboard_session_duration,
                        session.estimatedMinutes
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                shape = RoundedCornerShape(FitterTheme.radius.pill),
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = FitterIcons.Play,
                    contentDescription = stringResource(R.string.action_start_session),
                    modifier = Modifier
                        .padding(FitterTheme.spacing.md)
                        .size(FitterTheme.sizes.iconMedium)
                )
            }
        }
    }
}

@Composable
private fun TodayChips(snapshot: DashboardSnapshot, onAddWater: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm)) {
        SectionHeader(title = stringResource(R.string.dashboard_today_title))

        Row(horizontalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm)) {
            MetricChip(
                icon = FitterIcons.Water,
                label = stringResource(
                    R.string.dashboard_water_value,
                    Format.litres(snapshot.hydration.intakeMl),
                    Format.litres(snapshot.hydration.targetMl)
                ),
                contentDescription = stringResource(R.string.action_add_water),
                onClick = onAddWater
            )
            MetricChip(
                icon = FitterIcons.Moon,
                label = stringResource(
                    R.string.dashboard_sleep_value,
                    snapshot.sleep.hours?.let(Format::oneDecimal) ?: stringResource(R.string.value_empty)
                ),
                accent = MaterialTheme.colorScheme.secondary
            )
        }

        MetricChip(
            icon = FitterIcons.Check,
            label = stringResource(
                R.string.dashboard_supplements_value,
                snapshot.supplements.taken,
                snapshot.supplements.total
            ),
            accent = FitterTheme.colors.positive
        )
    }
}

@Composable
private fun GoalsSection(snapshot: DashboardSnapshot) {
    Column(verticalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm)) {
        SectionHeader(title = stringResource(R.string.dashboard_rings_title))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FitterTheme.spacing.sm)
        ) {
            RingCard(
                progress = snapshot.nutrition.caloriesProgress,
                label = stringResource(
                    R.string.dashboard_ring_calories,
                    snapshot.nutrition.calories.toInt()
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            RingCard(
                progress = snapshot.nutrition.proteinProgress,
                label = stringResource(
                    R.string.dashboard_ring_protein,
                    snapshot.nutrition.protein.toInt()
                ),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            RingCard(
                progress = snapshot.weeklyVolume.progress,
                label = stringResource(
                    R.string.dashboard_ring_sets,
                    snapshot.weeklyVolume.effectiveSets
                ),
                color = FitterTheme.colors.record,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RingCard(
    progress: Float,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    FitterCard(
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(FitterTheme.spacing.md)
    ) {
        ProgressRing(
            progress = progress,
            label = label,
            color = color,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
