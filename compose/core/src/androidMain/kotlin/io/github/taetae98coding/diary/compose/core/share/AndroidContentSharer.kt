package io.github.taetae98coding.diary.compose.core.share

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

public class AndroidContentSharerFactory(
    private val dispatcher: CoroutineDispatcher,
) : ContentSharerFactory {
    @Composable
    override fun rememberContentSharer(): ContentSharer {
        val context = LocalContext.current

        return remember(context) { AndroidContentSharer(context = context, dispatcher = dispatcher) }
    }
}

internal class AndroidContentSharer(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher,
) : ContentSharer {
    override val isTextSupported: Boolean = true

    override fun isFileSupported(type: ShareFileType): Boolean = true

    override suspend fun share(content: ShareContent): ShareResult {
        val intent =
            try {
                content.toSendIntent()
            } catch (_: IOException) {
                return ShareResult.FAILED
            }

        return try {
            context.startActivity(Intent.createChooser(intent, null).withNewTaskIfNeeded())
            ShareResult.COMPLETED
        } catch (_: ActivityNotFoundException) {
            ShareResult.FAILED
        }
    }

    private suspend fun ShareContent.toSendIntent(): Intent =
        when (this) {
            is ShareContent.Text ->
                Intent(Intent.ACTION_SEND)
                    .setType(TEXT_MIME_TYPE)
                    .putExtraIfNotEmpty(Intent.EXTRA_TEXT, text)
                    .putTitle(title)

            is ShareContent.File -> {
                val uri = write(content = this)

                Intent(Intent.ACTION_SEND)
                    .setType(type.mimeType)
                    .putExtra(Intent.EXTRA_STREAM, uri)
                    .putExtraIfNotEmpty(Intent.EXTRA_TEXT, text)
                    .putTitle(title)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .apply { clipData = ClipData.newUri(context.contentResolver, fileName, uri) }
            }
        }

    private suspend fun write(content: ShareContent.File): Uri =
        withContext(dispatcher) {
            val directory = File(context.cacheDir, SHARE_DIRECTORY_NAME)
            directory.deleteRecursively()
            if (!directory.mkdirs()) throw IOException("Share directory cannot be created. directory=$directory")

            val file = File(directory, content.fileName)
            file.writeBytes(content.bytes)

            FileProvider.getUriForFile(context, "${context.packageName}$SHARE_AUTHORITY_SUFFIX", file, content.fileName)
        }

    // 액티비티가 아닌 Context로 액티비티를 열려면 새 작업 플래그가 있어야 한다.
    private fun Intent.withNewTaskIfNeeded(): Intent = if (context.findActivity() == null) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) else this
}

// 공유 시트 위쪽의 미리 보기는 EXTRA_TITLE을, 메일처럼 제목을 받는 앱은 EXTRA_SUBJECT를 읽는다.
private fun Intent.putTitle(title: String): Intent = putExtraIfNotEmpty(Intent.EXTRA_SUBJECT, title).putExtraIfNotEmpty(Intent.EXTRA_TITLE, title)

private fun Intent.putExtraIfNotEmpty(
    name: String,
    value: String,
): Intent = if (value.isEmpty()) this else putExtra(name, value)

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

private const val TEXT_MIME_TYPE = "text/plain"

// 매니페스트의 res/xml/compose_core_share_file_paths.xml과 provider 선언이 이 이름과 짝을 이룬다.
private const val SHARE_DIRECTORY_NAME = "share"
private const val SHARE_AUTHORITY_SUFFIX = ".share"
