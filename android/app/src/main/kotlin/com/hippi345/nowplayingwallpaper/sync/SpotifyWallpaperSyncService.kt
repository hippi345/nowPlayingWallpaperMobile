package com.hippi345.nowplayingwallpaper.sync

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.hippi345.nowplayingwallpaper.MainActivity
import com.hippi345.nowplayingwallpaper.NowPlayingWallpaperApplication
import com.hippi345.nowplayingwallpaper.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Polls Spotify currently-playing while signed in, including when the user is on the home screen.
 */
class SpotifyWallpaperSyncService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var pollJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        running = true
        promoteToForeground()
        startPolling()
        return START_STICKY
    }

    override fun onDestroy() {
        running = false
        pollJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun promoteToForeground() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startPolling() {
        pollJob?.cancel()
        val engine = (application as NowPlayingWallpaperApplication).wallpaperSyncEngine
        pollJob = serviceScope.launch {
            while (isActive) {
                if (!engine.isSignedIn()) {
                    stopSelf()
                    break
                }
                engine.syncOnce()
                delay(WallpaperSyncEngine.POLL_INTERVAL_MS)
            }
        }
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.wallpaper_sync_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.wallpaper_sync_channel_description)
        }
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val openApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.wallpaper_sync_notification_title))
            .setContentText(getString(R.string.wallpaper_sync_notification_text))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(openApp)
            .setOngoing(true)
            .setSilent(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    companion object {
        private const val TAG = "SpotifyWallpaperSync"
        private const val CHANNEL_ID = "spotify_wallpaper_sync"
        private const val NOTIFICATION_ID = 1
        private const val ACTION_STOP = "com.hippi345.nowplayingwallpaper.action.STOP_WALLPAPER_SYNC"

        @Volatile
        private var running: Boolean = false

        fun start(context: Context) {
            val appContext = context.applicationContext
            val intent = Intent(appContext, SpotifyWallpaperSyncService::class.java)
            try {
                ContextCompat.startForegroundService(appContext, intent)
                running = true
            } catch (e: Exception) {
                Log.e(TAG, "Could not start wallpaper sync foreground service", e)
            }
        }

        fun stop(context: Context) {
            running = false
            val intent = Intent(context.applicationContext, SpotifyWallpaperSyncService::class.java).apply {
                action = ACTION_STOP
            }
            context.applicationContext.startService(intent)
        }

        fun isRunning(): Boolean = running
    }
}
