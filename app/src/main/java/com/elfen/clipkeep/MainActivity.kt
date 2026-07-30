package com.elfen.clipkeep

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobScheduler
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
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
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Transformer
import com.elfen.clipkeep.data.local.dao.ClipDao
import com.elfen.clipkeep.presentation.screen.Navigation
import com.elfen.clipkeep.presentation.theme.ClipKeepTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlin.math.abs
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@kotlin.OptIn(ExperimentalUuidApi::class)
@AndroidEntryPoint
@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var clipDao: ClipDao

    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default
    )

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel(this)
        enableEdgeToEdge()

        scope.launch { rotateThumbnails() }

        setContent {
            ClipKeepTheme {
                Navigation()
            }
        }
    }

    private suspend fun rotateThumbnails() {
        val clips = clipDao.queryClipsFlow().first()

        clips.filter { !it.hasRotatedThumbnail }.forEach { clip ->
            val rotation = clip.rotation
            val thumbnail = try {
                clip.thumbnailUri.toUri().toFile()
            } catch (_: Exception) {
                return@forEach
            }

            if (!thumbnail.exists())
                return@forEach

            val bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(thumbnail))

            val matrix = Matrix().apply {
                postRotate(abs(rotation % 180f))
            }

            val rotatedBitmap = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            )

            val thumbnailFile = File(
                filesDir,
                "${Uuid.generateV4()}.png"
            )

            withContext(Dispatchers.IO) {
                FileOutputStream(thumbnailFile).use {
                    rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
            }

            clipDao.updateThumbnail(clip.id, thumbnailFile.toUri().toString(), true)
        }
    }

    fun createNotificationChannel(context: Context) {
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(
            "RENDER_NOTIFICATION",
            "Render Notification",
            importance
        ).apply {
            description = "Displays information about the render progress"
        }
        // Register the channel with the system.
        val notificationManager: NotificationManager =
            context.getSystemService(NotificationManager::class.java) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}