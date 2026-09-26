package com.ahmedalobaedy.timework.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ahmedalobaedy.timework.R
import com.ahmedalobaedy.timework.data.WorkRecord
import com.ahmedalobaedy.timework.data.WorkSessionStore
import com.ahmedalobaedy.timework.service.WorkTrackingService
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun HomeScreen(
    store: WorkSessionStore,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    var activeStart by remember { mutableStateOf(store.activeStartMillis) }
    var elapsed by remember {
        mutableLongStateOf(
            activeStart?.let { System.currentTimeMillis() - it } ?: 0L
        )
    }
    var showFinishDialog by remember { mutableStateOf(false) }
    var lastFinished by remember { mutableStateOf<WorkRecord?>(null) }

    LaunchedEffect(activeStart) {
        while (activeStart != null) {
            elapsed = (
                System.currentTimeMillis() -
                    (activeStart ?: System.currentTimeMillis())
                ).coerceAtLeast(0L)
            delay(1000)
        }
    }

    val today = LocalDate.now()
    val completedToday = store.records()
        .filter { it.date == today }
        .sumOf { it.durationMillis }

    val todayTotal = completedToday +
        if (activeStart != null) elapsed else 0L

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = today.format(
                    DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
                ),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        }

        item {
            WorkCircle(
                active = activeStart != null,
                elapsedMillis = elapsed,
                onClick = {
                    if (activeStart == null) {
                        store.startWork()
                        WorkTrackingService.start(context.applicationContext)
                        activeStart = store.activeStartMillis
                        elapsed = 0L
                        onRefresh()
                    } else {
                        showFinishDialog = true
                    }
                }
            )
        }

        item {
            Text(
                text = if (activeStart != null) {
                    stringResource(R.string.tap_to_finish)
                } else {
                    stringResource(R.string.start_work)
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = stringResource(R.string.start_time),
                    value = activeStart?.let(::formatTime) ?: "—",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = stringResource(R.string.today_total),
                    value = formatDuration(todayTotal),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = stringResource(R.string.estimated_pay),
                    value = "${formatNumber(store.estimatedPay(todayTotal))} ${store.currency()}",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = stringResource(R.string.work_days),
                    value = store.records()
                        .map { it.date }
                        .distinct()
                        .size
                        .toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        lastFinished?.let { record ->
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.today_summary),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${stringResource(R.string.start_time)}: " +
                                formatTime(record.startMillis)
                        )
                        Text(
                            text = "${stringResource(R.string.end_time)}: " +
                                formatTime(record.endMillis)
                        )
                        Text(
                            text = "${stringResource(R.string.duration)}: " +
                                formatDuration(record.durationMillis)
                        )
                    }
                }
            }
        }
    }

    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = {
                Text(text = stringResource(R.string.workday_ended_question))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        lastFinished = store.finishWork()
                        WorkTrackingService.stop(context.applicationContext)
                        activeStart = null
                        elapsed = 0L
                        showFinishDialog = false
                        onRefresh()
                    }
                ) {
                    Text(text = stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showFinishDialog = false }
                ) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun WorkCircle(
    active: Boolean,
    elapsedMillis: Long,
    onClick: () -> Unit
) {
    val background = if (active) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    val foreground = if (active) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    Box(
        modifier = Modifier
            .size(230.dp)
            .clip(CircleShape)
            .background(background, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (active) {
                    stringResource(R.string.work_in_progress)
                } else {
                    stringResource(R.string.start_work)
                },
                color = foreground,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (active) {
                    formatDuration(elapsedMillis)
                } else {
                    "00:00:00"
                },
                color = foreground,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
