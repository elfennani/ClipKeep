package com.elfen.clipkeep.presentation.screen.clipper

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.elfen.clipkeep.R
import com.elfen.clipkeep.presentation.theme.ClipKeepTheme

@Composable
fun ClipperScreen(
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit
) {
    ClipperScreen(
        onNavigateBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClipperScreen(
    onNavigateBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(painterResource(R.drawable.baseline_arrow_back_24), null)
                    }
                },
                title = {
                    Text("Clipper")
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("Select File")
        }
    }
}

@Preview
@Composable
private fun ClipperScreenPrev() {
    ClipKeepTheme() {
        ClipperScreen()
    }
}