package io.github.taetae98coding.diary.core.file.impl.datasource

import android.content.ContentResolver
import android.content.Context
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

private const val READ_MODE = "r"

// 사진 선택기가 주는 content 위치는 ContentResolver로만 읽을 수 있고, 앱이 만든 파일은 file 위치라 둘을 함께 다룬다.
@Factory
internal class AndroidFileLocalDataSource(
    private val context: Context,
    @FileDispatcher private val dispatcher: CoroutineDispatcher,
) : FileLocalDataSource {
    private val fileDataSource = JavaFileLocalDataSource(dispatcher = dispatcher)

    override suspend fun name(uri: FileUri): String {
        val parsed = Uri.parse(uri.value)

        return if (parsed.isContent()) withContext(dispatcher) { parsed.displayName(uri = uri) } else fileDataSource.name(uri = uri)
    }

    override suspend fun mimeType(uri: FileUri): String {
        val parsed = Uri.parse(uri.value)

        return if (parsed.isContent()) withContext(dispatcher) { context.contentResolver.getType(parsed).orEmpty() } else fileDataSource.mimeType(uri = uri)
    }

    override suspend fun size(uri: FileUri): Long {
        val parsed = Uri.parse(uri.value)

        return if (parsed.isContent()) withContext(dispatcher) { parsed.contentSize(uri = uri) } else fileDataSource.size(uri = uri)
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

    // 제공자가 길이를 미리 알리지 못하는 스트림은 OpenableColumns로 한 번 더 묻는다.
    private fun Uri.contentSize(uri: FileUri): Long {
        val declaredLength =
            context.contentResolver
                .openAssetFileDescriptor(this, READ_MODE)
                ?.use { descriptor -> descriptor.length }
                ?.takeIf { length -> length != AssetFileDescriptor.UNKNOWN_LENGTH }

        return declaredLength ?: queriedSize() ?: error("Content size cannot be read. uri=$uri")
    }

    private fun Uri.queriedSize(): Long? =
        context.contentResolver.query(this, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.SIZE)

            if (index >= 0 && cursor.moveToFirst() && !cursor.isNull(index)) cursor.getLong(index) else null
        }

    private fun Uri.displayName(uri: FileUri): String =
        context.contentResolver.query(this, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)

            if (index >= 0 && cursor.moveToFirst() && !cursor.isNull(index)) cursor.getString(index) else null
        } ?: error("Content name cannot be read. uri=$uri")

    private fun Uri.isContent(): Boolean = scheme == ContentResolver.SCHEME_CONTENT
}
