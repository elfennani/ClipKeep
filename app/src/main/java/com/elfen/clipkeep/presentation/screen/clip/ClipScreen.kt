package com.elfen.clipkeep.presentation.screen.clip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.ui.compose.material3.Player
import androidx.navigation3.runtime.NavKey
import com.elfen.clipkeep.R

@Composable
fun ClipScreen(
    route: ClipRoute,
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit
) {
    val viewModel = hiltViewModel<ClipViewModel, ClipViewModel.Factory>(
        creationCallback = { it.create(route.id) }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    ClipScreen(
        state = state,
        onBack = onBack
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClipScreen(
    state: ClipUiState = ClipUiState(),
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.baseline_arrow_back_24), null)
                    }
                },
                title = { Text("Clip") }
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                CircularProgressIndicator()
            }
        } else if (state.exoPlayer != null) {
            Player(
                player = state.exoPlayer,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color.Black)
            )
        }
    }
}