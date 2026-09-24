@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.core.file.impl.image

import cnames.structs.__CFNumber
import cnames.structs.__CFString
import cnames.structs.__CFURL
import io.github.taetae98coding.diary.core.file.api.ImageConverter
import io.github.taetae98coding.diary.core.file.impl.di.FileDispatcher
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.model.image.ImageCropRegion
import io.github.taetae98coding.diary.core.model.image.ImageFormat
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.value
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.koin.core.annotation.Factory
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionaryGetValue
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFNumberGetValue
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFNumberIntType
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.CoreGraphics.CGImageCreateWithImageInRect
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetWidth
import platform.CoreGraphics.CGImageRef
import platform.CoreGraphics.CGRectMake
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSNumber
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.ImageIO.CGImageDestinationAddImage
import platform.ImageIO.CGImageDestinationCreateWithURL
import platform.ImageIO.CGImageDestinationFinalize
import platform.ImageIO.CGImageDestinationRef
import platform.ImageIO.CGImageSourceCopyPropertiesAtIndex
import platform.ImageIO.CGImageSourceCreateThumbnailAtIndex
import platform.ImageIO.CGImageSourceCreateWithURL
import platform.ImageIO.CGImageSourceRef
import platform.ImageIO.kCGImageDestinationLossyCompressionQuality
import platform.ImageIO.kCGImagePropertyOrientation
import platform.ImageIO.kCGImagePropertyPixelHeight
import platform.ImageIO.kCGImagePropertyPixelWidth
import platform.ImageIO.kCGImageSourceCreateThumbnailFromImageAlways
import platform.ImageIO.kCGImageSourceCreateThumbnailWithTransform
import platform.ImageIO.kCGImageSourceThumbnailMaxPixelSize
import platform.UniformTypeIdentifiers.UTType
import platform.UniformTypeIdentifiers.UTTypeJPEG

private const val PERCENT = 100.0
private const val FIRST_IMAGE_INDEX = 0uL
private const val IMAGE_COUNT = 1uL
private const val EXIF_ORIENTATION_NORMAL = 1
private const val EXIF_ORIENTATION_FIRST_TRANSPOSED = 5
private const val THUMBNAIL_OPTION_COUNT = 3L
private const val DESTINATION_OPTION_COUNT = 1L

// ImageIO의 썸네일 생성은 촬영 방향을 반영하고 긴 변을 한도까지 줄인 이미지를 한 번에 만들어 주므로,
// 원본 크기와 방향만 먼저 읽어 한도를 정한 뒤 그 이미지에서 남길 영역을 잘라 목표 형식으로 쓴다.
@Factory
internal class IosImageConverter(
    @FileDispatcher private val dispatcher: CoroutineDispatcher,
) : ImageConverter {
    override suspend fun convert(
        source: FileUri,
        format: ImageFormat,
        cropRegion: ImageCropRegion,
        maxSideLength: Int,
        quality: Int,
    ): FileUri =
        withContext(dispatcher) {
            val sourceUrl = checkNotNull(NSURL.URLWithString(source.value)) { "File uri is not a url. uri=$source" }
            val path = NSTemporaryDirectory() + "converted-image-${NSUUID().UUIDString}.${format.extension()}"
            val destinationUrl = NSURL.fileURLWithPath(path)

            runCatching {
                useCFRetained(sourceUrl) { sourceRef ->
                    val imageSource = checkNotNull(CGImageSourceCreateWithURL(sourceRef.reinterpret<__CFURL>(), null)) { readFailureMessage(source) }

                    useCF(imageSource) {
                        writeCropped(
                            source = imageSource,
                            cropRegion = cropRegion,
                            maxSideLength = maxSideLength,
                            destination = ImageDestination(url = destinationUrl, type = format.toUTType(), quality = quality),
                            uri = source,
                        )
                    }
                }
            }.onFailure { SystemFileSystem.delete(Path(path), mustExist = false) }
                .getOrThrow()

            FileUri(checkNotNull(destinationUrl.absoluteString) { "Converted image has no url. path=$path" })
        }

    private fun writeCropped(
        source: CGImageSourceRef,
        cropRegion: ImageCropRegion,
        maxSideLength: Int,
        destination: ImageDestination,
        uri: FileUri,
    ) {
        val orientedSize = readOrientedSize(source = source, uri = uri)
        val scale = cropRegion.toPixelRect(imageWidth = orientedSize.width, imageHeight = orientedSize.height).scaleToFit(maxSideLength)
        val oriented = createOrientedImage(source = source, maxPixelSize = orientedSize.scaled(scale).longestSide, uri = uri)

        useCF(oriented) {
            val crop = cropRegion.toPixelRect(imageWidth = CGImageGetWidth(oriented).toInt(), imageHeight = CGImageGetHeight(oriented).toInt())
            val rect = CGRectMake(crop.left.toDouble(), crop.top.toDouble(), crop.width.toDouble(), crop.height.toDouble())
            val cropped = checkNotNull(CGImageCreateWithImageInRect(oriented, rect)) { "Image cannot be cropped. uri=$uri" }

            useCF(cropped) { write(image = cropped, destination = destination, uri = uri) }
        }
    }

    private fun readOrientedSize(
        source: CGImageSourceRef,
        uri: FileUri,
    ): PixelSize {
        val properties = checkNotNull(CGImageSourceCopyPropertiesAtIndex(source, FIRST_IMAGE_INDEX, null)) { readFailureMessage(uri) }

        return useCF(properties) {
            val width = checkNotNull(properties.intValue(kCGImagePropertyPixelWidth)) { "Image width cannot be read. uri=$uri" }
            val height = checkNotNull(properties.intValue(kCGImagePropertyPixelHeight)) { "Image height cannot be read. uri=$uri" }
            val orientation = properties.intValue(kCGImagePropertyOrientation) ?: EXIF_ORIENTATION_NORMAL

            // 방향 5~8은 90도 회전을 포함하므로 촬영 방향을 반영하면 가로와 세로가 바뀐다.
            if (orientation >= EXIF_ORIENTATION_FIRST_TRANSPOSED) {
                PixelSize(width = height, height = width)
            } else {
                PixelSize(width = width, height = height)
            }
        }
    }

    private fun createOrientedImage(
        source: CGImageSourceRef,
        maxPixelSize: Int,
        uri: FileUri,
    ): CGImageRef =
        useCFRetained(NSNumber(int = maxPixelSize)) { maxPixelSizeRef ->
            val options =
                checkNotNull(
                    CFDictionaryCreateMutable(null, THUMBNAIL_OPTION_COUNT, kCFTypeDictionaryKeyCallBacks.ptr, kCFTypeDictionaryValueCallBacks.ptr),
                ) { readFailureMessage(uri) }

            useCF(options) {
                CFDictionarySetValue(options, kCGImageSourceCreateThumbnailFromImageAlways, kCFBooleanTrue)
                CFDictionarySetValue(options, kCGImageSourceCreateThumbnailWithTransform, kCFBooleanTrue)
                CFDictionarySetValue(options, kCGImageSourceThumbnailMaxPixelSize, maxPixelSizeRef)

                checkNotNull(CGImageSourceCreateThumbnailAtIndex(source, FIRST_IMAGE_INDEX, options)) { readFailureMessage(uri) }
            }
        }

    private fun write(
        image: CGImageRef,
        destination: ImageDestination,
        uri: FileUri,
    ) {
        useCFRetained(destination.url) { destinationRef ->
            useCFRetained(destination.type.identifier) { typeRef ->
                val imageDestination =
                    checkNotNull(
                        CGImageDestinationCreateWithURL(destinationRef.reinterpret<__CFURL>(), typeRef.reinterpret<__CFString>(), IMAGE_COUNT, null),
                    ) { "Image cannot be written. uri=$uri" }

                useCF(imageDestination) { addImage(destination = imageDestination, image = image, quality = destination.quality, uri = uri) }
            }
        }
    }

    private fun addImage(
        destination: CGImageDestinationRef,
        image: CGImageRef,
        quality: Int,
        uri: FileUri,
    ) {
        useCFRetained(NSNumber(double = quality / PERCENT)) { qualityRef ->
            val options =
                checkNotNull(
                    CFDictionaryCreateMutable(null, DESTINATION_OPTION_COUNT, kCFTypeDictionaryKeyCallBacks.ptr, kCFTypeDictionaryValueCallBacks.ptr),
                ) { "Image cannot be converted. uri=$uri" }

            useCF(options) {
                CFDictionarySetValue(options, kCGImageDestinationLossyCompressionQuality, qualityRef)
                CGImageDestinationAddImage(destination, image, options)

                check(CGImageDestinationFinalize(destination)) { "Image cannot be converted. uri=$uri" }
            }
        }
    }
}

private class ImageDestination(
    val url: NSURL,
    val type: UTType,
    val quality: Int,
)

private fun ImageFormat.toUTType(): UTType =
    when (this) {
        ImageFormat.JPEG -> UTTypeJPEG
    }

private fun ImageFormat.extension(): String =
    when (this) {
        ImageFormat.JPEG -> "jpg"
    }

private fun readFailureMessage(uri: FileUri): String = "Image cannot be read. uri=$uri"

private fun CFDictionaryRef.intValue(key: CFStringRef?): Int? {
    val number = CFDictionaryGetValue(this, key) ?: return null

    return memScoped {
        val result = alloc<IntVar>()

        if (CFNumberGetValue(number.reinterpret<__CFNumber>(), kCFNumberIntType, result.ptr)) result.value else null
    }
}

private inline fun <T> useCFRetained(
    value: Any,
    block: (CFTypeRef) -> T,
): T {
    val ref = checkNotNull(CFBridgingRetain(value)) { "CoreFoundation bridging failed. value=$value" }

    return useCF(ref) { block(ref) }
}

private inline fun <T> useCF(
    ref: CFTypeRef,
    block: () -> T,
): T =
    try {
        block()
    } finally {
        CFRelease(ref)
    }
