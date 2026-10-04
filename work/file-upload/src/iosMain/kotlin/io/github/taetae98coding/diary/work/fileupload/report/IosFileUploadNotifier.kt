package io.github.taetae98coding.diary.work.fileupload.report

import io.github.taetae98coding.diary.core.navigation.ScreenDeepLink
import io.github.taetae98coding.diary.library.locale.DeviceLocale
import org.koin.core.annotation.Factory
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter

private const val RESULT_NOTIFICATION_IDENTIFIER: String = "fileUploadResult"
private const val KOREAN_LANGUAGE_PREFIX: String = "ko"

@Factory
internal class IosFileUploadNotifier : FileUploadNotifier {
    override fun notifyResult(result: FileUploadResult) {
        val text = if (isKorean()) KoreanFileUploadNotificationText else EnglishFileUploadNotificationText
        val content =
            UNMutableNotificationContent().apply {
                setTitle(result.title(text = text))
                result.body(text = text).takeIf { body -> body.isNotEmpty() }?.let(::setBody)
                setUserInfo(mapOf(ScreenDeepLink.USER_INFO_KEY to ScreenDeepLink.FILE_HOME))
            }
        val request =
            UNNotificationRequest.requestWithIdentifier(
                identifier = RESULT_NOTIFICATION_IDENTIFIER,
                content = content,
                trigger = null,
            )

        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request = request, withCompletionHandler = null)
    }

    private fun isKorean(): Boolean = DeviceLocale.currentLanguageTag().startsWith(KOREAN_LANGUAGE_PREFIX)

    private fun FileUploadResult.title(text: FileUploadNotificationText): String =
        when (this) {
            is FileUploadResult.Succeeded -> text.succeededTitle
            is FileUploadResult.TooLarge, is FileUploadResult.Failed -> text.failedTitle
        }

    private fun FileUploadResult.body(text: FileUploadNotificationText): String =
        when (this) {
            is FileUploadResult.Succeeded -> name
            is FileUploadResult.TooLarge -> text.tooLargeBody
            is FileUploadResult.Failed -> name
        }
}

private data class FileUploadNotificationText(
    val succeededTitle: String,
    val failedTitle: String,
    val tooLargeBody: String,
)

private val KoreanFileUploadNotificationText =
    FileUploadNotificationText(
        succeededTitle = "파일을 올렸습니다",
        failedTitle = "파일을 올리지 못했습니다",
        tooLargeBody = "50MB 이하 파일만 올릴 수 있습니다.",
    )

private val EnglishFileUploadNotificationText =
    FileUploadNotificationText(
        succeededTitle = "File uploaded",
        failedTitle = "Couldn't upload the file",
        tooLargeBody = "Only files up to 50 MB can be uploaded.",
    )
