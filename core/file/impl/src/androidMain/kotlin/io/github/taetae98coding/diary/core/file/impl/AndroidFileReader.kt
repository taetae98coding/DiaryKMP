package io.github.taetae98coding.diary.core.file.impl

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import io.github.taetae98coding.diary.core.file.api.FileReader
import io.github.taetae98coding.diary.core.file.api.FileSource
import io.github.taetae98coding.diary.core.model.file.FileUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.RawSource
import kotlinx.io.asSource
import java.io.InputStream

internal class AndroidFileReader(
    private val context: Context,
) : FileReader {
    override suspend fun open(uri: FileUri): FileSource =
        withContext(Dispatchers.IO) {
            val contentUri = Uri.parse(uri.value)
            val mimeType = checkNotNull(context.contentResolver.getType(contentUri)) { "File mime type is unknown. uri=$uri" }

            AndroidFileSource(
                mimeType = mimeType,
                size = contentUri.size(),
                openStream = {
                    checkNotNull(context.contentResolver.openInputStream(contentUri)) { "File cannot be opened. uri=$uri" }
                },
            )
        }

    private fun Uri.size(): Long =
        checkNotNull(
            context.contentResolver
                .query(this, arrayOf(OpenableColumns.SIZE), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst() && !cursor.isNull(0)) cursor.getLong(0) else null
                },
        ) { "File size is unknown. uri=$this" }
}

private class AndroidFileSource(
    override val mimeType: String,
    override val size: Long,
    private val openStream: () -> InputStream,
) : FileSource {
    override fun openSource(): RawSource = openStream().asSource()
}
