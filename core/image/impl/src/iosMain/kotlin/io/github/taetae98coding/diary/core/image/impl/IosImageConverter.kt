package io.github.taetae98coding.diary.core.image.impl

import io.github.taetae98coding.diary.core.image.api.ImageConverter
import io.github.taetae98coding.diary.core.image.api.JpegSource
import io.github.taetae98coding.diary.core.model.file.FileUri
import kotlinx.io.files.Path
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.writeToFile
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation

private const val PERCENT = 100.0

internal class IosImageConverter : ImageConverter {
    override suspend fun toJpeg(uri: FileUri): JpegSource {
        val sourcePath = checkNotNull(NSURL.URLWithString(uri.value)?.path) { "File uri has no path. uri=$uri" }
        val image = checkNotNull(UIImage.imageWithContentsOfFile(sourcePath)) { "Image cannot be read. uri=$uri" }
        val jpeg =
            checkNotNull(UIImageJPEGRepresentation(image, JPEG_QUALITY_PERCENT / PERCENT)) {
                "Image cannot be converted to jpeg. uri=$uri"
            }
        val path = NSTemporaryDirectory() + "profile-image-${NSUUID().UUIDString}.jpg"

        check(jpeg.writeToFile(path, atomically = true)) { "Image cannot be written. uri=$uri" }

        return FileJpegSource(path = Path(path))
    }
}
