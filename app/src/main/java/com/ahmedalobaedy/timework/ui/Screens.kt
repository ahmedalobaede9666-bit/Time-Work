package com.ahmedalobaedy.timework.ui

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.ahmedalobaedy.timework.BuildConfig
import com.ahmedalobaedy.timework.R
import com.ahmedalobaedy.timework.data.WorkRecord
import com.ahmedalobaedy.timework.data.WorkSessionStore
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.max

@Composable
fun ReportsScreen(records: List<WorkRecord>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.recent_records),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (records.isEmpty()) {
            item {
                Text(text = stringResource(R.string.no_records))
            }
        } else {
            items(
                items = records.asReversed(),
                key = { "${it.startMillis}-${it.endMillis}" }
            ) { record ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = record.date.format(
                                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                            ),
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
}

@Composable
fun StatsScreen(records: List<WorkRecord>) {
    val today = LocalDate.now()
    val weekStart = today.minusDays(6)
    val currentMonth = YearMonth.now()

    val weekly = records.filter {
        !it.date.isBefore(weekStart) && !it.date.isAfter(today)
    }

    val monthly = records.filter {
        YearMonth.from(it.date) == currentMonth
    }

    val weeklyMillis = weekly.sumOf { it.durationMillis }
    val monthlyMillis = monthly.sumOf { it.durationMillis }

    val last7Days = (6 downTo 0)
        .map { today.minusDays(it.toLong()) }
        .map { day ->
            day to records
                .filter { it.date == day }
                .sumOf { it.durationMillis }
        }

    val maxHours = max(
        8.0,
        last7Days.maxOfOrNull {
            it.second / 3_600_000.0
        } ?: 8.0
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = stringResource(R.string.weekly_hours),
                    value = formatDuration(weeklyMillis),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = stringResource(R.string.monthly_hours),
                    value = formatDuration(monthlyMillis),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.weekly_chart),
                        fontWeight = FontWeight.Bold
                    )

                    last7Days.forEach { (day, millis) ->
                        val hours = millis / 3_600_000.0

                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = day.format(
                                        DateTimeFormatter.ofPattern("EEE")
                                    )
                                )
                                Text(
                                    text = "${formatNumber(hours)} " +
                                        stringResource(R.string.hours_suffix)
                                )
                            }

                            LinearProgressIndicator(
                                progress = {
                                    (hours / maxHours)
                                        .toFloat()
                                        .coerceIn(0f, 1f)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.work_days),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = monthly
                            .map { it.date }
                            .distinct()
                            .size
                            .toString(),
                        style = MaterialTheme.typography.displaySmall
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    store: WorkSessionStore,
    onRefresh: () -> Unit
) {
    var rate by remember { mutableStateOf(store.hourlyRate().toString()) }
    var standardHours by remember {
        mutableStateOf(store.standardHours().toString())
    }
    var overtimeMultiplier by remember {
        mutableStateOf(store.overtimeMultiplier().toString())
    }
    var currency by remember { mutableStateOf(store.currency()) }
    var saved by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.language),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { setAppLanguage("ar") }
                ) {
                    Text(text = stringResource(R.string.arabic))
                }

                Button(
                    onClick = { setAppLanguage("en") }
                ) {
                    Text(text = stringResource(R.string.english))
                }
            }
        }

        item {
            HorizontalDivider()
        }

        item {
            OutlinedTextField(
                value = rate,
                onValueChange = {
                    rate = it
                    saved = false
                },
                label = {
                    Text(text = stringResource(R.string.hourly_rate))
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = standardHours,
                onValueChange = {
                    standardHours = it
                    saved = false
                },
                label = {
                    Text(text = stringResource(R.string.standard_hours))
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = overtimeMultiplier,
                onValueChange = {
                    overtimeMultiplier = it
                    saved = false
                },
                label = {
                    Text(
                        text = stringResource(
                            R.string.overtime_multiplier
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = currency,
                onValueChange = {
                    currency = it
                    saved = false
                },
                label = {
                    Text(text = stringResource(R.string.currency_hint))
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            Button(
                onClick = {
                    store.savePaySettings(
                        rate = rate.toDoubleOrNull() ?: 0.0,
                        standardHours = standardHours.toDoubleOrNull() ?: 8.0,
                        multiplier = overtimeMultiplier.toDoubleOrNull() ?: 1.5,
                        currency = currency
                    )
                    saved = true
                    onRefresh()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (saved) {
                        stringResource(R.string.saved)
                    } else {
                        stringResource(R.string.save)
                    }
                )
            }
        }
    }
}

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = stringResource(R.string.about_text))
                }
            }
        }

        item {
            ContactRow(
                icon = Icons.Default.Info,
                label = stringResource(R.string.version),
                value = BuildConfig.VERSION_NAME
            )
        }

        item {
            ContactRow(
                icon = Icons.Default.Person,
                label = stringResource(R.string.developer),
                value = stringResource(R.string.developer_name)
            )
        }

        item {
            ContactRow(
                icon = Icons.Default.Phone,
                label = stringResource(R.string.phone),
                value = stringResource(R.string.developer_phone),
                onClick = {
                    val phone = context.getString(R.string.developer_phone)
                    context.startActivity(
                        Intent(
                            Intent.ACTION_DIAL,
                            Uri.parse("tel:$phone")
                        )
                    )
                }
            )
        }

        item {
            ContactRow(
                icon = Icons.Default.Email,
                label = stringResource(R.string.email),
                value = stringResource(R.string.developer_email),
                onClick = {
                    val email = context.getString(R.string.developer_email)
                    context.startActivity(
                        Intent(
                            Intent.ACTION_SENDTO,
                            Uri.parse("mailto:$email")
                        )
                    )
                }
            )
        }

        item {
            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.home))
            }
        }
    }
}

@Composable
private fun ContactRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = value,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private fun setAppLanguage(tag: String) {
    AppCompatDelegate.setApplicationLocales(
        LocaleListCompat.forLanguageTags(tag)
    )
}
