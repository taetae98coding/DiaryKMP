package io.github.taetae98coding.diary.app.shared.share

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import io.github.taetae98coding.diary.domain.share.MAX_SHARE_TEXT_BYTES
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import kotlin.coroutines.cancellation.CancellationException

public class ShareStreamReader internal constructor(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher,
) {
    public suspend fun mimeType(uri: Uri): String =
        withContext(dispatcher) {
            runCatchingReadFailure { context.contentResolver.getType(uri) }.orEmpty()
        }

    // 다른 앱이 이 앱 자신의 provider 주소를 보내 앱 안의 비공개 파일을 읽게 하지 못하도록, 다른 앱의 content 주소만 연다.
    public suspend fun isReadable(uri: Uri): Boolean =
        withContext(dispatcher) {
            val authority = uri.authority.orEmpty()
            if (uri.scheme != ContentResolver.SCHEME_CONTENT || authority.isEmpty()) return@withContext false

            val provider = context.packageManager.resolveContentProvider(authority, PackageManager.ComponentInfoFlags.of(0L))
            provider?.packageName != context.packageName
        }

    public suspend fun readText(uri: Uri): String =
        withContext(dispatcher) {
            runCatchingReadFailure {
                context.contentResolver.openInputStream(uri)?.use { stream -> stream.readTextWithin(MAX_SHARE_TEXT_BYTES) }
            }.orEmpty()
        }
}

// 한도를 넘는 파일은 끝까지 읽지 않고, 넘는다는 것을 알 수 있는 길이까지만 읽는다.
private fun InputStream.readTextWithin(maxBytes: Int): String = readNBytes(maxBytes + 1).decodeToString()

// 보낸 앱이 권한을 주지 않았거나 파일이 사라진 주소는 열지 못한 것으로 다루고, 취소는 그대로 전달한다.
private inline fun <T> runCatchingReadFailure(block: () -> T): T? =
    try {
        block()
    } catch (exception: CancellationException) {
        throw exception
    } catch (_: IOException) {
        null
    } catch (_: SecurityException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    } catch (_: IllegalStateException) {
        null
    }
