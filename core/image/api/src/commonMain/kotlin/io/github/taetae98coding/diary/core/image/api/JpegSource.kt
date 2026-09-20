package io.github.taetae98coding.diary.core.image.api

import kotlinx.io.RawSource

/**
 * 바꾼 이미지를 어디에 두는지는 구현이 정한다. 임시 파일에 두는 구현이 있으므로
 * 다 쓴 뒤에는 [close]로 정리해야 한다.
 */
public interface JpegSource : AutoCloseable {
    public val size: Long

    public fun openSource(): RawSource
}
