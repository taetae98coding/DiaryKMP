package io.github.taetae98coding.diary.library.foundation

import io.github.taetae98coding.diary.library.objc.ObjCRuntime
import io.github.taetae98coding.diary.library.objc.nsString
import io.github.taetae98coding.diary.library.objc.release
import io.github.taetae98coding.diary.library.objc.send
import io.github.taetae98coding.diary.library.objc.sendBoolean
import io.github.taetae98coding.diary.library.objc.sendVoid
import io.github.taetae98coding.diary.library.objc.utf8String
import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

private const val CREATION_WITH_SECURITY_SCOPE: Long = 1L shl 11
private const val RESOLUTION_WITH_SECURITY_SCOPE: Long = 1L shl 10
private const val BASE64_OPTIONS: Long = 0L

private val msgSendBookmarkData =
    ObjCRuntime.msgSendHandle(
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
            ValueLayout.JAVA_LONG,
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
        ),
    )
private val msgSendResolveBookmark =
    ObjCRuntime.msgSendHandle(
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
            ValueLayout.JAVA_LONG,
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
        ),
    )
private val msgSendIdLongReturnId =
    ObjCRuntime.msgSendHandle(
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG),
    )
private val msgSendLongReturnId =
    ObjCRuntime.msgSendHandle(FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG))

/**
 * 샌드박스 앱은 열기 창으로 고른 폴더를 그 실행에서만 쓸 수 있다. 보안 범위 북마크로 바꿔 두면 다음 실행에서 다시 접근을 얻는다.
 */
public object SecurityScopedBookmark {
    public fun create(path: String): String =
        ObjCRuntime.withAutoreleasePool {
            Arena.ofConfined().use { arena ->
                val url = ObjCRuntime.objcClass("NSURL").send(ObjCRuntime.selector("fileURLWithPath:"), nsString(path))
                val data =
                    msgSendBookmarkData.invoke(
                        url,
                        ObjCRuntime.selector("bookmarkDataWithOptions:includingResourceValuesForKeys:relativeToURL:error:"),
                        CREATION_WITH_SECURITY_SCOPE,
                        MemorySegment.NULL,
                        MemorySegment.NULL,
                        arena.allocate(ValueLayout.ADDRESS),
                    ) as MemorySegment

                check(data != MemorySegment.NULL) { "Bookmark cannot be created. path=$path" }

                (msgSendLongReturnId.invoke(data, ObjCRuntime.selector("base64EncodedStringWithOptions:"), BASE64_OPTIONS) as MemorySegment).utf8String()
            }
        }

    // 샌드박스 밖에서는 범위를 얻을 필요가 없어 접근 시작이 거절될 수 있으므로, 거절돼도 쓰기는 그대로 시도하게 둔다.
    public fun startAccessing(bookmark: String): AutoCloseable =
        ObjCRuntime.withAutoreleasePool {
            Arena.ofConfined().use { arena ->
                val data =
                    msgSendIdLongReturnId.invoke(
                        ObjCRuntime.objcClass("NSData").send(ObjCRuntime.selector("alloc")),
                        ObjCRuntime.selector("initWithBase64EncodedString:options:"),
                        nsString(bookmark),
                        BASE64_OPTIONS,
                    ) as MemorySegment

                check(data != MemorySegment.NULL) { "Bookmark is invalid." }

                val url =
                    try {
                        msgSendResolveBookmark.invoke(
                            ObjCRuntime.objcClass("NSURL"),
                            ObjCRuntime.selector("URLByResolvingBookmarkData:options:relativeToURL:bookmarkDataIsStale:error:"),
                            data,
                            RESOLUTION_WITH_SECURITY_SCOPE,
                            MemorySegment.NULL,
                            arena.allocate(ValueLayout.JAVA_BYTE),
                            arena.allocate(ValueLayout.ADDRESS),
                        ) as MemorySegment
                    } finally {
                        data.release()
                    }

                check(url != MemorySegment.NULL) { "Bookmark cannot be resolved." }

                val retainedUrl = url.send(ObjCRuntime.selector("retain"))

                SecurityScopedAccess(
                    url = retainedUrl,
                    isAccessing = retainedUrl.sendBoolean(ObjCRuntime.selector("startAccessingSecurityScopedResource")),
                )
            }
        }
}

private class SecurityScopedAccess(
    private val url: MemorySegment,
    private val isAccessing: Boolean,
) : AutoCloseable {
    override fun close() {
        ObjCRuntime.withAutoreleasePool {
            if (isAccessing) url.sendVoid(ObjCRuntime.selector("stopAccessingSecurityScopedResource"))
            url.release()
        }
    }
}
