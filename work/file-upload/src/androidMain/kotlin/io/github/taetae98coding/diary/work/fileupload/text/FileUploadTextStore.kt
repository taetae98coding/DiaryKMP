package io.github.taetae98coding.diary.work.fileupload.text

import android.content.Context
import io.github.taetae98coding.diary.work.fileupload.di.FileUploadTextDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import kotlin.uuid.Uuid

private const val DIRECTORY_NAME: String = "file-upload-text"

// WorkManager의 입력 값은 모두 합쳐 10KB까지만 담을 수 있어, 길이 제한이 없는 제목과 설명은 앱 전용 파일에 두고 작업에는 그 이름만 넘긴다.
@Factory
internal class FileUploadTextStore(
    private val context: Context,
    @FileUploadTextDispatcher private val dispatcher: CoroutineDispatcher,
) {
    suspend fun write(text: FileUploadText): String =
        withContext(dispatcher) {
            val id = Uuid.random().toString()

            directory().mkdirs()
            DataOutputStream(file(id = id).outputStream().buffered()).use { output ->
                output.writeText(text.title)
                output.writeText(text.description)
            }

            id
        }

    suspend fun read(id: String): FileUploadText =
        withContext(dispatcher) {
            DataInputStream(file(id = id).inputStream().buffered()).use { input ->
                FileUploadText(title = input.readText(), description = input.readText())
            }
        }

    suspend fun delete(id: String) {
        withContext(dispatcher) { file(id = id).delete() }
    }

    private fun directory(): File = File(context.noBackupFilesDir, DIRECTORY_NAME)

    private fun file(id: String): File = File(directory(), id)
}

internal data class FileUploadText(
    val title: String,
    val description: String,
)

// writeUTF는 64KB를 넘는 문자열을 담지 못하므로 길이와 UTF-8 바이트를 직접 적는다.
private fun DataOutputStream.writeText(value: String) {
    val bytes = value.encodeToByteArray()

    writeInt(bytes.size)
    write(bytes)
}

private fun DataInputStream.readText(): String {
    val bytes = ByteArray(readInt())

    readFully(bytes)

    return bytes.decodeToString()
}
