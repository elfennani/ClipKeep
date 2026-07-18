package com.elfen.clipkeep.presentation.screen.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.elfen.clipkeep.R
import com.elfen.clipkeep.domain.model.Clip
import com.elfen.clipkeep.presentation.component.ClipCard
import com.elfen.clipkeep.presentation.screen.clip.ClipRoute
import com.elfen.clipkeep.presentation.screen.clipper.ClipperRoute
import com.elfen.clipkeep.presentation.theme.ClipKeepTheme

@Composable
fun HomeScreen(
    onNavigate: (NavKey) -> Unit
) {
    val viewModel = hiltViewModel<HomeViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle();
    HomeScreen(
        state = state,
        onNavigateToClipper = {
            onNavigate(ClipperRoute(it.toString()))
        },
        onClickClip = {
            onNavigate(ClipRoute(it.id.toInt()))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    state: HomeUiState = HomeUiState(),
    onNavigateToClipper: (Uri?) -> Unit = {},
    onClickClip: (clip: Clip) -> Unit = {}
) {
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            onNavigateToClipper(uri)
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Home")
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = {
                launcher.launch(arrayOf("video/*"))
            }) {
                Icon(painterResource(R.drawable.sharp_video_camera_back_add_24), null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add")
            }
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else if (state.clips.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Nothing to see here!")
            }
        } else {
            LazyVerticalStaggeredGrid(
                modifier = Modifier.padding(innerPadding),
                columns = StaggeredGridCells.Fixed(2),
                verticalItemSpacing = 16.dp,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(state.clips) { clip ->
                    ClipCard(
                        modifier = Modifier
                            .fillMaxWidth(0.33f)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onClickClip(clip) },
                        clip = clip
                    )
                }
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

@Preview
@Composable
private fun HomeScreenClipsPreview() {
    ClipKeepTheme {
        HomeScreen(
            state = HomeUiState(
                isLoading = false,
                clips = Clip.samples
            )
        )
    }
}

@Preview
@Composable
private fun HomeScreenEmptyPreview() {
    ClipKeepTheme {
        HomeScreen(
            state = HomeUiState(
                isLoading = false,
                clips = emptyList()
            )
        )
    }
}