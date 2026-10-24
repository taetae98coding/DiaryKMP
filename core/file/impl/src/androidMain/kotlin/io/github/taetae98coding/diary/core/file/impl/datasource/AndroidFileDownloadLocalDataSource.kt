package io.github.taetae98coding.diary.core.file.impl.datasource

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import io.github.taetae98coding.diary.core.file.api.datasource.FileDownloadLocalDataSource
import io.github.taetae98coding.diary.core.file.api.exception.FileDownloadDirectoryUnavailableException
import io.github.taetae98coding.diary.core.file.impl.di.FileDispatcher
import io.github.taetae98coding.diary.core.model.file.FileDownloadDirectory
import io.github.taetae98coding.diary.core.model.file.FileDownloadResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.io.RawSink
import kotlinx.io.asSink
import org.koin.core.annotation.Factory
import java.io.FileNotFoundException

// 시스템이 이름 뒤에 확장자를 덧붙이지 않도록 형식을 알 수 없는 일반 파일로 만든다. 이름은 받은 그대로다.
private const val SAVE_MIME_TYPE = "application/octet-stream"
private const val DIRECTORY_PERMISSION_FLAGS = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

@Factory
internal class AndroidFileDownloadLocalDataSource(
    private val context: Context,
    @FileDispatcher
    private val dispatcher: CoroutineDispatcher,
) : FileDownloadLocalDataSource {
    override val isDirectorySelectable: Boolean = true

    private val contentResolver: ContentResolver
        get() = context.contentResolver

    override suspend fun save(
        directory: FileDownloadDirectory?,
        nameCandidate: (index: Int) -> String,
        write: suspend (RawSink) -> Unit,
    ): FileDownloadResult =
        withContext(dispatcher) {
            val document = if (directory == null) createDownloadsFile(name = nameCandidate(0)) else createTreeFile(directory = directory, nameCandidate = nameCandidate)

            try {
                val outputStream = contentResolver.openOutputStream(document.uri) ?: throw FileDownloadDirectoryUnavailableException(message = "Document cannot be opened. uri=${document.uri}")

                outputStream.asSink().use { sink -> write(sink) }
                FileDownloadResult.Saved(name = document.complete())
            } catch (throwable: Throwable) {
                document.discard()
                throw throwable
            }
        }

    override suspend fun retainDirectory(uri: String): FileDownloadDirectory =
        withContext(dispatcher) {
            val treeUri = Uri.parse(uri)

            contentResolver.takePersistableUriPermission(treeUri, DIRECTORY_PERMISSION_FLAGS)

            FileDownloadDirectory(uri = uri, name = contentResolver.displayNameOrNull(uri = treeUri.toTreeDocumentUri()).orEmpty())
        }

    override suspend fun releaseDirectory(directory: FileDownloadDirectory) {
        withContext(dispatcher) {
            val treeUri = Uri.parse(directory.uri)

            if (contentResolver.persistedUriPermissions.any { permission -> permission.uri == treeUri }) {
                contentResolver.releasePersistableUriPermission(treeUri, DIRECTORY_PERMISSION_FLAGS)
            }
        }
    }

    // 공용 Download 폴더는 시스템이 겹치는 이름에 번호를 붙여 주므로 첫 이름으로만 만든다.
    // 끝까지 쓰기 전에는 다른 앱에 보이지 않도록 쓰는 중으로 표시해 둔다.
    private fun createDownloadsFile(name: String): DownloadDocument {
        val values =
            ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, name)
                put(MediaStore.Downloads.MIME_TYPE, SAVE_MIME_TYPE)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
        val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: throw FileDownloadDirectoryUnavailableException(message = "Download cannot be created. name=$name")

        return DownloadDocument(
            uri = uri,
            requestedName = name,
            onComplete = {
                contentResolver.update(uri, ContentValues().apply { put(MediaStore.Downloads.IS_PENDING, 0) }, null, null)
            },
            onDiscard = { contentResolver.delete(uri, null, null) },
        )
    }

    private fun createTreeFile(
        directory: FileDownloadDirectory,
        nameCandidate: (index: Int) -> String,
    ): DownloadDocument {
        val treeUri = Uri.parse(directory.uri)

        if (contentResolver.persistedUriPermissions.none { permission -> permission.uri == treeUri && permission.isWritePermission }) {
            throw FileDownloadDirectoryUnavailableException(message = "Directory permission is gone. uri=$treeUri")
        }

        val existingNameSet = unavailableIfThrown(treeUri = treeUri) { contentResolver.childNameSet(treeUri = treeUri) }
        val name = generateSequence(0) { index -> index + 1 }.map(nameCandidate).first { candidate -> candidate !in existingNameSet }
        val uri =
            unavailableIfThrown(treeUri = treeUri) {
                DocumentsContract.createDocument(contentResolver, treeUri.toTreeDocumentUri(), SAVE_MIME_TYPE, name)
            } ?: throw FileDownloadDirectoryUnavailableException(message = "Document cannot be created. uri=$treeUri")

        return DownloadDocument(
            uri = uri,
            requestedName = name,
            onComplete = {},
            onDiscard = { DocumentsContract.deleteDocument(contentResolver, uri) },
        )
    }

    private fun DownloadDocument.complete(): String {
        onComplete()

        return contentResolver.displayNameOrNull(uri = uri) ?: requestedName
    }

    // 지우지 못해도 호출자에게는 원래 실패를 알려야 하므로 정리의 실패는 버린다.
    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private fun DownloadDocument.discard() {
        try {
            onDiscard()
        } catch (_: Exception) {
        }
    }
}

private class DownloadDocument(
    val uri: Uri,
    val requestedName: String,
    val onComplete: () -> Unit,
    val onDiscard: () -> Unit,
)

// 고른 폴더가 지워졌거나 시스템이 허락을 거두면 문서 제공자가 예외를 던지거나 null을 돌려준다.
private inline fun <T> unavailableIfThrown(
    treeUri: Uri,
    block: () -> T,
): T =
    try {
        block()
    } catch (exception: FileNotFoundException) {
        throw FileDownloadDirectoryUnavailableException(message = "Directory is gone. uri=$treeUri", cause = exception)
    } catch (exception: SecurityException) {
        throw FileDownloadDirectoryUnavailableException(message = "Directory permission is gone. uri=$treeUri", cause = exception)
    } catch (exception: IllegalArgumentException) {
        throw FileDownloadDirectoryUnavailableException(message = "Directory is invalid. uri=$treeUri", cause = exception)
    }

private fun Uri.toTreeDocumentUri(): Uri = DocumentsContract.buildDocumentUriUsingTree(this, DocumentsContract.getTreeDocumentId(this))

private fun ContentResolver.childNameSet(treeUri: Uri): Set<String> {
    val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri))
    val cursor = query(childrenUri, arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME), null, null, null) ?: throw FileNotFoundException("Children cannot be listed. uri=$treeUri")

    return cursor.use {
        buildSet {
            while (cursor.moveToNext()) {
                cursor.getString(0)?.let(::add)
            }
        }
    }
}

private fun ContentResolver.displayNameOrNull(uri: Uri): String? =
    query(uri, arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    }
