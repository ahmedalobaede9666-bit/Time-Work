package com.ahmedalobaedy.timework.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.ahmedalobaedy.timework.R
import com.ahmedalobaedy.timework.data.WorkSessionStore

private enum class Screen {
    HOME,
    REPORTS,
    STATS,
    SETTINGS,
    ABOUT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeWorkApp() {
    val context = LocalContext.current
    val store = remember { WorkSessionStore(context.applicationContext) }

    var screen by remember { mutableStateOf(Screen.HOME) }
    var refreshKey by remember { mutableLongStateOf(0L) }

    val refresh = {
        refreshKey = System.currentTimeMillis()
    }

    refreshKey

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { screen = Screen.ABOUT }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.about)
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (screen != Screen.ABOUT) {
                ShortNavigationBar {
                    NavigationItem(
                        target = Screen.HOME,
                        current = screen,
                        label = stringResource(R.string.home),
                        icon = Icons.Default.Home
                    ) { screen = it }

                    NavigationItem(
                        target = Screen.REPORTS,
                        current = screen,
                        label = stringResource(R.string.reports),
                        icon = Icons.Default.Assessment
                    ) { screen = it }

                    NavigationItem(
                        target = Screen.STATS,
                        current = screen,
                        label = stringResource(R.string.statistics),
                        icon = Icons.Default.ShowChart
                    ) { screen = it }

                    NavigationItem(
                        target = Screen.SETTINGS,
                        current = screen,
                        label = stringResource(R.string.settings),
                        icon = Icons.Default.Settings
                    ) { screen = it }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (screen) {
                Screen.HOME -> HomeScreen(store = store, onRefresh = refresh)
                Screen.REPORTS -> ReportsScreen(records = store.records())
                Screen.STATS -> StatsScreen(records = store.records())
                Screen.SETTINGS -> SettingsScreen(store = store, onRefresh = refresh)
                Screen.ABOUT -> AboutScreen(onBack = { screen = Screen.HOME })
            }
        }
    }
}

@Composable
private fun NavigationItem(
    target: Screen,
    current: Screen,
    label: String,
    icon: ImageVector,
    onClick: (Screen) -> Unit
) {
    ShortNavigationBarItem(
        selected = current == target,
        onClick = { onClick(target) },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
        label = {
            Text(text = label)
        }
    )
}
