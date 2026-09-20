package io.github.taetae98coding.diary.core.image.impl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import io.github.taetae98coding.diary.core.image.api.ImageConverter
import io.github.taetae98coding.diary.core.image.api.JpegSource
import io.github.taetae98coding.diary.core.model.file.FileUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

internal class AndroidImageConverter(
    private val context: Context,
) : ImageConverter {
    override suspend fun toJpeg(uri: FileUri): JpegSource =
        withContext(Dispatchers.IO) {
            val source = ImageDecoder.createSource(context.contentResolver, Uri.parse(uri.value))
            val bitmap =
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    // 하드웨어 비트맵은 화소를 읽을 수 없어 JPEG로 다시 쓰지 못한다.
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
            val file = File(context.cacheDir, "profile-image-${UUID.randomUUID()}.jpg")

            runCatching {
                FileOutputStream(file).use { stream ->
                    check(bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY_PERCENT, stream)) {
                        "Image cannot be converted to jpeg. uri=$uri"
                    }
                }
            }.onFailure { file.delete() }
                .getOrThrow()

            FileJpegSource(path = Path(file.path))
        }
}
