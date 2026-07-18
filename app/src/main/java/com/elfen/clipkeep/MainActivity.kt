package com.elfen.clipkeep

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Transformer
import com.elfen.clipkeep.presentation.screen.Navigation
import com.elfen.clipkeep.presentation.theme.ClipKeepTheme
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MainActivity : ComponentActivity() {
    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClipKeepTheme {
                Navigation()
//                var video by remember { mutableStateOf<Uri?>(null) }
//                val launcher =
//                    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
//                        video = it
//                    }
//                val startSeconds = rememberTextFieldState("0");
//                val endSeconds = rememberTextFieldState("1")
//
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Column(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(innerPadding),
//                        verticalArrangement = Arrangement.spacedBy(16.dp)
//                    ) {
//                        Button(
//                            onClick = {
//                                launcher.launch(arrayOf("video/*"))
//                            }
//                        ) {
//                            Text("Pick Video")
//                        }
//
//                        if (video != null) {
//                            Text("URI: $video", modifier = Modifier.fillMaxWidth())
//
//                            Row(
//                                modifier = Modifier.fillMaxWidth(),
//                                verticalAlignment = Alignment.CenterVertically,
//                                horizontalArrangement = Arrangement.spacedBy(8.dp)
//                            ) {
//                                TextField(
//                                    modifier = Modifier.weight(1f),
//                                    state = startSeconds,
//                                    label = {
//                                        Text("Start Seconds")
//                                    },
//                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                                )
//                                TextField(
//                                    modifier = Modifier.weight(1f),
//                                    state = endSeconds,
//                                    label = {
//                                        Text("End Seconds")
//                                    },
//                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                                )
//                            }
//
//                            Button(
//                                onClick = {
//                                    Log.d("MainActivity", "Starting");
//                                    val inputMediaItem =
//                                        MediaItem.Builder()
//                                            .setUri(video)
//                                            .setClippingConfiguration(
//                                                MediaItem.ClippingConfiguration.Builder()
//                                                    .setStartPositionMs(
//                                                        startSeconds.text.toString().toInt() * 1000L
//                                                    )
//                                                    .setEndPositionMs(
//                                                        endSeconds.text.toString().toInt() * 1000L
//                                                    )
//                                                    .build()
//                                            )
//                                            .build()
//                                    val transformer =
//                                        Transformer.Builder(this@MainActivity)
//                                            .build();
//                                    val outputFile =
//                                        File(filesDir, this@MainActivity.getFileName(video!!))
//
//                                    transformer.start(inputMediaItem, outputFile.absolutePath)
//                                    Log.d("MainActivity", "Finished");
//                                }
//                            ) {
//                                Text("Crop Video")
//                            }
//                        }
//                    }
//                }
            }
        }
    }
}