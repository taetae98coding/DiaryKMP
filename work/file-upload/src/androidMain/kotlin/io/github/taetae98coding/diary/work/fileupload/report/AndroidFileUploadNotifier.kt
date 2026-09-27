package io.github.taetae98coding.diary.work.fileupload.report

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import androidx.work.ForegroundInfo
import io.github.taetae98coding.diary.core.navigation.ScreenDeepLink
import io.github.taetae98coding.diary.library.kotlin.math.FULL_PERCENT
import io.github.taetae98coding.diary.work.file.upload.R
import org.koin.core.annotation.Factory

internal const val FILE_UPLOAD_NOTIFICATION_CHANNEL_ID: String = "fileUpload"

internal const val FILE_UPLOAD_PROGRESS_NOTIFICATION_ID: Int = 1
internal const val FILE_UPLOAD_RESULT_NOTIFICATION_ID: Int = 2

private const val OPEN_FILE_HOME_REQUEST_CODE = 0

@Factory
internal class AndroidFileUploadNotifier(
    private val context: Context,
) : FileUploadNotifier {
    fun createForegroundInfo(
        name: String,
        percent: Int?,
    ): ForegroundInfo {
        createChannel()

        val notification =
            Notification
                .Builder(context, FILE_UPLOAD_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_file_upload_notification)
                .setContentTitle(context.getString(R.string.file_upload_notification_progress_title))
                .setContentText(name)
                .setProgress(FULL_PERCENT, percent ?: 0, percent == null)
                .setCategory(Notification.CATEGORY_PROGRESS)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(context.openFileHomePendingIntent())
                .build()

        return ForegroundInfo(FILE_UPLOAD_PROGRESS_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
    }

    override fun notifyResult(result: FileUploadResult) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        createChannel()

        val notification =
            Notification
                .Builder(context, FILE_UPLOAD_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_file_upload_notification)
                .setContentTitle(result.title())
                .setBodyIfPresent(result.body())
                .setContentIntent(context.openFileHomePendingIntent())
                .setAutoCancel(true)
                .build()

        manager.notify(FILE_UPLOAD_RESULT_NOTIFICATION_ID, notification)
    }

    private fun Notification.Builder.setBodyIfPresent(body: String): Notification.Builder = if (body.isEmpty()) this else setContentText(body)

    // 알림을 보내기 직전에 만든다. 이미 있으면 시스템이 그대로 두므로 사용자가 바꾼 채널 설정도 유지된다.
    private fun createChannel() {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel =
            NotificationChannel(
                FILE_UPLOAD_NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.file_upload_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            ).also { channel -> channel.description = context.getString(R.string.file_upload_notification_channel_description) }

        manager.createNotificationChannel(channel)
    }

    private fun FileUploadResult.title(): String =
        when (this) {
            is FileUploadResult.Succeeded -> context.getString(R.string.file_upload_notification_succeeded_title)
            is FileUploadResult.TooLarge, is FileUploadResult.Failed -> context.getString(R.string.file_upload_notification_failed_title)
        }

    private fun FileUploadResult.body(): String =
        when (this) {
            is FileUploadResult.Succeeded -> name
            is FileUploadResult.TooLarge -> context.getString(R.string.file_upload_notification_too_large_body)
            is FileUploadResult.Failed -> name
        }
}

// 이 모듈이 앱의 화면 구성을 알지 않도록 런처 진입점에 FileHome 주소만 실어 연다.
private fun Context.openFileHomePendingIntent(): PendingIntent? {
    val intent =
        packageManager.getLaunchIntentForPackage(packageName)?.apply {
            data = Uri.parse(ScreenDeepLink.FILE_HOME)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        } ?: return null

    return PendingIntent.getActivity(
        this,
        OPEN_FILE_HOME_REQUEST_CODE,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
}
