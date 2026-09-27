package com.example.utils

import android.content.Context
import android.os.Build
import android.view.Choreographer
import android.view.Display
import android.view.WindowManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.TimeUnit

object PerformanceHelper {

    fun getDisplayRefreshRate(context: Context): Float {
        return try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.display?.refreshRate ?: 60f
            } else {
                @Suppress("DEPRECATION")
                windowManager?.defaultDisplay?.refreshRate ?: 60f
            }
        } catch (e: Exception) {
            60f
        }
    }

    /**
     * Measures live frame times of the launcher UI itself using Choreographer.
     * Note: Third-party apps cannot legally measure global system FPS of other games without root or adb permissions.
     */
    fun monitorLauncherFpsFlow(): Flow<Float> = callbackFlow {
        var lastFrameTimeNanos = 0L
        var frameCount = 0
        var accumulatedNanos = 0L

        val callback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if (lastFrameTimeNanos > 0L) {
                    val frameDelta = frameTimeNanos - lastFrameTimeNanos
                    accumulatedNanos += frameDelta
                    frameCount++

                    if (accumulatedNanos >= TimeUnit.SECONDS.toNanos(1)) {
                        val fps = (frameCount.toDouble() * 1_000_000_000.0 / accumulatedNanos).toFloat()
                        trySend(fps)
                        frameCount = 0
                        accumulatedNanos = 0L
                    }
                }
                lastFrameTimeNanos = frameTimeNanos
                try {
                    Choreographer.getInstance().postFrameCallback(this)
                } catch (e: Exception) {
                    // Closed
                }
            }
        }

        try {
            Choreographer.getInstance().postFrameCallback(callback)
        } catch (e: Exception) {
            trySend(60f)
        }

        awaitClose {
            try {
                Choreographer.getInstance().removeFrameCallback(callback)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
