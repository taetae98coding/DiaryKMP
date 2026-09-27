package io.github.taetae98coding.diary.core.file.impl.datasource

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.net.Uri
import android.provider.OpenableColumns
import io.github.taetae98coding.diary.core.file.api.datasource.FileLocalDataSource
import io.github.taetae98coding.diary.core.file.impl.di.FileDispatcher
import io.github.taetae98coding.diary.core.model.file.FileUri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.io.RawSource
import kotlinx.io.asSource
import org.koin.core.annotation.Factory
import java.io.FileNotFoundException

private const val READ_MODE = "r"

// 사진·문서 선택기가 주는 content 위치는 ContentResolver로만 읽을 수 있고, 앱이 만든 파일은 file 위치라 둘을 함께 다룬다.
@Factory
internal class AndroidFileLocalDataSource(
    private val context: Context,
    @FileDispatcher private val dispatcher: CoroutineDispatcher,
) : FileLocalDataSource {
    private val fileDataSource = JavaFileLocalDataSource(dispatcher = dispatcher)

    override suspend fun name(uri: FileUri): String {
        val parsed = Uri.parse(uri.value)

        return if (parsed.isContent()) withContext(dispatcher) { context.contentResolver.displayName(contentUri = parsed, uri = uri) } else fileDataSource.name(uri = uri)
    }

    override suspend fun mimeType(uri: FileUri): String {
        val parsed = Uri.parse(uri.value)

        return if (parsed.isContent()) withContext(dispatcher) { context.contentResolver.getType(parsed).orEmpty() } else fileDataSource.mimeType(uri = uri)
    }

    override suspend fun size(uri: FileUri): Long {
        val parsed = Uri.parse(uri.value)

        return if (parsed.isContent()) withContext(dispatcher) { context.contentResolver.contentSize(contentUri = parsed, uri = uri) } else fileDataSource.size(uri = uri)
    }

    override suspend fun openSource(uri: FileUri): RawSource {
        val parsed = Uri.parse(uri.value)

        return if (parsed.isContent()) {
            withContext(dispatcher) { checkNotNull(context.contentResolver.openInputStream(parsed)) { "Content cannot be opened. uri=$uri" }.asSource() }
        } else {
            fileDataSource.openSource(uri = uri)
        }
    }

    // content 위치는 다른 앱이 소유한 자료라 지우지 않는다. 지울 수 있는 것은 앱이 만든 파일뿐이다.
    override suspend fun delete(uri: FileUri) {
        require(!Uri.parse(uri.value).isContent()) { "Content uri cannot be deleted. uri=$uri" }

        fileDataSource.delete(uri = uri)
    }

    // 문서 선택기가 준 읽기 권한은 그 화면이 살아 있는 동안만 유효하므로, 앱이 다시 시작된 뒤에도 읽을 수 있게 권한을 기기에 남긴다.
    // 제공자가 권한을 남기는 것을 허용하지 않으면 SecurityException이 그대로 전달된다.
    override suspend fun retain(uri: FileUri) {
        val parsed = Uri.parse(uri.value)

        if (!parsed.isContent()) return

        withContext(dispatcher) {
            context.contentResolver.takePersistableUriPermission(parsed, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    // 플랫폼은 남겨 둔 권한이 없다는 것을 SecurityException으로만 알린다. 놓을 것이 없다는 결과와 같으므로 실패로 전달하지 않는다.
    override suspend fun release(uri: FileUri) {
        val parsed = Uri.parse(uri.value)

        if (!parsed.isContent()) return

        withContext(dispatcher) {
            try {
                context.contentResolver.releasePersistableUriPermission(parsed, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) {
                Unit
            }
        }
    }

    // 문서 선택기는 사본을 만들지 않고 원본의 위치를 준다.
    override suspend fun deleteLeftoverCopies(): Unit = Unit
}

// 제공자가 길이를 미리 알리지 못하거나 파일 설명자를 열 수 없는 스트림(일부 클라우드 제공자)은 OpenableColumns로 한 번 더 묻는다.
private fun ContentResolver.contentSize(
    contentUri: Uri,
    uri: FileUri,
): Long {
    val declaredLength =
        try {
            openAssetFileDescriptor(contentUri, READ_MODE)
                ?.use { descriptor -> descriptor.length }
                ?.takeIf { length -> length != AssetFileDescriptor.UNKNOWN_LENGTH }
        } catch (_: FileNotFoundException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }

    return declaredLength ?: queriedSize(contentUri = contentUri) ?: error("Content size cannot be read. uri=$uri")
}

private fun ContentResolver.queriedSize(contentUri: Uri): Long? =
    query(contentUri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.SIZE)

        if (index >= 0 && cursor.moveToFirst() && !cursor.isNull(index)) cursor.getLong(index) else null
    }

private fun ContentResolver.displayName(
    contentUri: Uri,
    uri: FileUri,
): String =
    query(contentUri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)

        if (index >= 0 && cursor.moveToFirst() && !cursor.isNull(index)) cursor.getString(index) else null
    } ?: error("Content name cannot be read. uri=$uri")

private fun Uri.isContent(): Boolean = scheme == ContentResolver.SCHEME_CONTENT
