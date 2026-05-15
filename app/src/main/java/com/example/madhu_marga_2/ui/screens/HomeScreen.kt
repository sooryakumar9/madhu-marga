package com.example.madhu_marga_2.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.madhu_marga_2.data.Hive
import com.example.madhu_marga_2.viewmodel.HiveViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HiveViewModel,
    onHiveClick: (Long) -> Unit,
    onAddHiveClick: () -> Unit,
    onFloraCalendarClick: () -> Unit
) {
    val hives by viewModel.allHives.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Madhu-Marga") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddHiveClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Hive")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Button(
                onClick = onFloraCalendarClick,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text("View Flora Calendar")
            }

            Text(
                "Your Hives",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn {
                items(hives) { hive ->
                    HiveItem(hive = hive, onClick = { onHiveClick(hive.id) })
                }
            }
        }
    }
}

@Composable
fun HiveItem(hive: Hive, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Hive: ${hive.hiveId}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Location: ${hive.location}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
