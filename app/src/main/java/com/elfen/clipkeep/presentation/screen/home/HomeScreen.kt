package com.elfen.clipkeep.presentation.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavKey
import com.elfen.clipkeep.presentation.screen.clipper.ClipperRoute
import com.elfen.clipkeep.presentation.theme.ClipKeepTheme

@Composable
fun HomeScreen(
    onNavigate: (NavKey) -> Unit
) {
    HomeScreen(
        onNavigateToClipper = {
            onNavigate(ClipperRoute)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToClipper: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Home")
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = onNavigateToClipper) {
                Text("Navigate to Clipper")
            }
        }
    }
}

@Preview
@Composable
private fun HomeScreenPrev() {
    ClipKeepTheme {
        HomeScreen()
    }
}