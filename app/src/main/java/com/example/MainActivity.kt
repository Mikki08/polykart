package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.GroupPickerDialog
import com.example.ui.components.UiStudioDialog
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PolyGreenPrimary
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PolyAppRoot()
        }
    }
}

@Composable
fun PolyAppRoot(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MyApplicationTheme(themeMode = uiState.uiThemeMode) {
        // Handle back button on sub-tabs
        BackHandler(enabled = uiState.activeTab != AppTab.HOME) {
            viewModel.selectTab(AppTab.HOME)
        }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(
                tonalElevation = 6.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                // 1. Главная
                NavigationBarItem(
                    selected = uiState.activeTab == AppTab.HOME,
                    onClick = { viewModel.selectTab(AppTab.HOME) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.activeTab == AppTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Главная"
                        )
                    },
                    label = { Text("Главная", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = PolyGreenPrimary
                    )
                )

                // 2. Расписание
                NavigationBarItem(
                    selected = uiState.activeTab == AppTab.SCHEDULE,
                    onClick = { viewModel.selectTab(AppTab.SCHEDULE) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.activeTab == AppTab.SCHEDULE) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                            contentDescription = "Расписание"
                        )
                    },
                    label = { Text("Расписание", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = PolyGreenPrimary
                    )
                )

                // 3. Поиск
                NavigationBarItem(
                    selected = uiState.activeTab == AppTab.SEARCH,
                    onClick = { viewModel.selectTab(AppTab.SEARCH) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.activeTab == AppTab.SEARCH) Icons.Filled.Search else Icons.Outlined.Search,
                            contentDescription = "Поиск"
                        )
                    },
                    label = { Text("Поиск", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = PolyGreenPrimary
                    )
                )

                // 4. Карта
                NavigationBarItem(
                    selected = uiState.activeTab == AppTab.POLYMAP,
                    onClick = { viewModel.selectTab(AppTab.POLYMAP) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.activeTab == AppTab.POLYMAP) Icons.Filled.Map else Icons.Outlined.Map,
                            contentDescription = "Карта"
                        )
                    },
                    label = { Text("Карта", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = PolyGreenPrimary
                    )
                )

                // 5. Сервисы
                NavigationBarItem(
                    selected = uiState.activeTab == AppTab.SERVICES,
                    onClick = { viewModel.selectTab(AppTab.SERVICES) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.activeTab == AppTab.SERVICES) Icons.Filled.Widgets else Icons.Outlined.Widgets,
                            contentDescription = "Сервисы"
                        )
                    },
                    label = { Text("Сервисы", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = PolyGreenPrimary
                    )
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Crossfade(
            targetState = uiState.activeTab,
            label = "tab_crossfade",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { tab ->
            when (tab) {
                AppTab.HOME -> HomeScreen(viewModel = viewModel, uiState = uiState)
                AppTab.POLYMAP -> PolyMapScreen(viewModel = viewModel, uiState = uiState)
                AppTab.SCHEDULE -> ScheduleScreen(viewModel = viewModel, uiState = uiState)
                AppTab.SEARCH -> SearchScreen(viewModel = viewModel, uiState = uiState)
                AppTab.SERVICES -> ServicesScreen(viewModel = viewModel, uiState = uiState)
            }
        }
    }

    // Group Picker Dialog with Filters
    GroupPickerDialog(
        isOpen = uiState.isGroupPickerOpen,
        query = uiState.groupPickerQuery,
        selectedInstitute = uiState.groupFilterInstitute,
        selectedCourse = uiState.groupFilterCourse,
        results = uiState.filteredGroups,
        onQueryChanged = { viewModel.onGroupPickerQueryChanged(it) },
        onInstituteSelected = { viewModel.onGroupFilterInstituteSelected(it) },
        onCourseSelected = { viewModel.onGroupFilterCourseSelected(it) },
        onGroupSelected = { viewModel.selectAndSaveGroup(it) },
        onDismiss = { viewModel.closeGroupPicker() }
    )

    // UI Studio Theme Selection Dialog
    UiStudioDialog(
        isOpen = uiState.isUiStudioOpen,
        activeTheme = uiState.uiThemeMode,
        onSelectTheme = { viewModel.selectUiTheme(it) },
        onDismiss = { viewModel.closeUiStudio() }
    )
    }
}
