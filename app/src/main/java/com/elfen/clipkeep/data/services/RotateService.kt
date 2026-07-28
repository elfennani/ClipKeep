package com.elfen.clipkeep.data.services

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.Service
import android.app.job.JobScheduler
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.DefaultEncoderFactory
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import androidx.media3.transformer.VideoEncoderSettings
import com.elfen.clipkeep.R
import com.elfen.clipkeep.data.local.dao.ClipDao
import com.elfen.clipkeep.domain.model.Clip
import com.elfen.clipkeep.domain.model.EditingClip
import com.elfen.clipkeep.domain.model.EditingClipPart
import com.elfen.clipkeep.domain.model.toMedia3Crop
import com.elfen.clipkeep.domain.repository.ClipRepository
import com.elfen.clipkeep.utils.getFileName
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@AndroidEntryPoint
class RotateService : Service() {
    override fun onBind(p0: Intent?): IBinder? = null

    @Inject
    lateinit var clipRepository: ClipRepository

    @Inject
    lateinit var clipDao: ClipDao

    val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main
    )

    @SuppressLint("InlinedApi")
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val id = intent.extras?.getLong(CLIP_ID_KEY)!!
        val rotation = intent.extras?.getFloat(ROTATION_KEY)!!

        val notification = NotificationCompat.Builder(this, "RENDER_NOTIFICATION")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Rendering Rotated Video")
            .setContentText("Preparing")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setProgress(0, 100, true)
            .setSilent(true)
            .build()

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                100,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROCESSING
            );
        } else {
            startForeground(
                100,
                notification
            );
        }

        scope.launch {
            try {
                rotate(id, rotation)
            } finally {
                stopSelf(startId)
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    private suspend fun rotate(id: Long, rotation: Float) {
        val clip = clipRepository.getClips().first().first { it.id == id }

        val output = renderRotation(clip, rotation) {
            val notification = NotificationCompat.Builder(this, "RENDER_NOTIFICATION")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Rendering Rotated Video")
                .setContentText("${(it * 100).toInt()}%")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setProgress(100, (it * 100).toInt(), false)
                .setSilent(true)
                .build()

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.notify(100, notification)
        }

        val input = clip.uri.toFile()
        clipDao.updateFileAndRotation(id, output.toString(), rotation)
        input.delete()
    }

    @OptIn(ExperimentalUuidApi::class)
    @UnstableApi
    private suspend fun renderRotation(
        clip: Clip,
        rotation: Float,
        onProgress: (Float) -> Unit = {}
    ): Uri =
        suspendCancellableCoroutine { continuation ->
            Log.d("ClipperViewModel", "Starting");
            val uri = clip.uri
            val inputMediaItem =
                MediaItem.Builder()
                    .setUri(uri)
                    .build()
            val editedMediaItem = EditedMediaItem.Builder(inputMediaItem)
                .setEffects(
                    Effects(
                        listOf(),
                        listOf(
                            ScaleAndRotateTransformation.Builder()
                                .setRotationDegrees(rotation)
                                .build()
                        )
                    )
                )
                .build()


            val transformer =
                Transformer.Builder(this)
                    .build();
            val outputFile =
                File(
                    filesDir,
                    "${Uuid.generateV4()}.${uri.toFile().name.substringAfterLast('.')}"
                )

            transformer.addListener(
                object : Transformer.Listener {
                    override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                        super.onCompleted(composition, exportResult)

                        continuation.resume(outputFile.toUri())
                    }

                    override fun onError(
                        composition: Composition,
                        exportResult: ExportResult,
                        exportException: ExportException
                    ) {
                        super.onError(composition, exportResult, exportException)
                        continuation.resumeWithException(exportException)
                    }
                }
            )
            val progressHolder = ProgressHolder()

            transformer.start(editedMediaItem, outputFile.absolutePath)

            scope.launch {
                while (isActive) {
                    when (transformer.getProgress(progressHolder)) {
                        Transformer.PROGRESS_STATE_NOT_STARTED -> {
                            onProgress(0f)
                        }

                        Transformer.PROGRESS_STATE_WAITING_FOR_AVAILABILITY -> {}
                        Transformer.PROGRESS_STATE_AVAILABLE -> {
                            onProgress(progressHolder.progress.toFloat() / 100f)
                        }

                        Transformer.PROGRESS_STATE_UNAVAILABLE -> {
                            break;
                        }
                    }

                    delay(500.milliseconds)
                }
            }

            continuation.invokeOnCancellation { transformer.cancel() }
        }

    companion object {
        const val CLIP_ID_KEY = "CLIP_ID"
        const val ROTATION_KEY = "ROTATION"


        fun start(context: Context, clipId: Long, rotation: Float) {
            val intent = Intent(context, RotateService::class.java).apply {
                action = "ROTATE"
                putExtra(CLIP_ID_KEY, clipId)
                putExtra(ROTATION_KEY, rotation)
            }

            context.startForegroundService(intent)
        }
    }
}