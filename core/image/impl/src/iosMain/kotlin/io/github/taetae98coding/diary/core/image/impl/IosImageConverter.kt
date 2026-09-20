@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.core.image.impl

import cnames.structs.__CFString
import cnames.structs.__CFURL
import io.github.taetae98coding.diary.core.image.api.ImageConverter
import io.github.taetae98coding.diary.core.image.api.JpegSource
import io.github.taetae98coding.diary.core.model.file.FileUri
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSNumber
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.ImageIO.CGImageDestinationAddImageFromSource
import platform.ImageIO.CGImageDestinationCreateWithURL
import platform.ImageIO.CGImageDestinationFinalize
import platform.ImageIO.CGImageDestinationRef
import platform.ImageIO.CGImageSourceCreateWithURL
import platform.ImageIO.CGImageSourceRef
import platform.ImageIO.kCGImageDestinationLossyCompressionQuality
import platform.UniformTypeIdentifiers.UTTypeJPEG

private const val PERCENT = 100.0
private const val FIRST_IMAGE_INDEX = 0uL
private const val IMAGE_COUNT = 1uL

// ImageIO는 원본 파일에서 목적지 파일로 바로 옮겨 쓰므로, 인코딩 결과를 NSData로 들고 있지 않는다.
// 원본의 방향 메타데이터도 함께 옮기고, 여러 장면이 있는 이미지는 첫 장면만 쓴다.
internal class IosImageConverter : ImageConverter {
    override suspend fun toJpeg(uri: FileUri): JpegSource {
        val sourceUrl = checkNotNull(NSURL.URLWithString(uri.value)) { "File uri is not a url. uri=$uri" }
        val path = NSTemporaryDirectory() + "profile-image-${NSUUID().UUIDString}.jpg"
        val destinationUrl = NSURL.fileURLWithPath(path)

        runCatching {
            useCFRetained(sourceUrl) { sourceRef ->
                val source = checkNotNull(CGImageSourceCreateWithURL(sourceRef.reinterpret<__CFURL>(), null)) { "Image cannot be read. uri=$uri" }

                useCF(source) { writeJpeg(source = source, destinationUrl = destinationUrl, uri = uri) }
            }
        }.onFailure { SystemFileSystem.delete(Path(path), mustExist = false) }
            .getOrThrow()

        return FileJpegSource(path = Path(path))
    }

    private fun writeJpeg(
        source: CGImageSourceRef,
        destinationUrl: NSURL,
        uri: FileUri,
    ) {
        useCFRetained(destinationUrl) { destinationRef ->
            useCFRetained(UTTypeJPEG.identifier) { typeRef ->
                val destination =
                    checkNotNull(
                        CGImageDestinationCreateWithURL(destinationRef.reinterpret<__CFURL>(), typeRef.reinterpret<__CFString>(), IMAGE_COUNT, null),
                    ) { "Image cannot be written. uri=$uri" }

                useCF(destination) { addFirstImage(destination = destination, source = source, uri = uri) }
            }
        }
    }

    private fun addFirstImage(
        destination: CGImageDestinationRef,
        source: CGImageSourceRef,
        uri: FileUri,
    ) {
        useCFRetained(NSNumber(double = JPEG_QUALITY_PERCENT / PERCENT)) { qualityRef ->
            val options =
                checkNotNull(
                    CFDictionaryCreateMutable(null, 1, kCFTypeDictionaryKeyCallBacks.ptr, kCFTypeDictionaryValueCallBacks.ptr),
                ) { "Image cannot be converted to jpeg. uri=$uri" }

            useCF(options) {
                CFDictionarySetValue(options, kCGImageDestinationLossyCompressionQuality, qualityRef)
                CGImageDestinationAddImageFromSource(destination, source, FIRST_IMAGE_INDEX, options)

                check(CGImageDestinationFinalize(destination)) { "Image cannot be converted to jpeg. uri=$uri" }
            }
        }
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
