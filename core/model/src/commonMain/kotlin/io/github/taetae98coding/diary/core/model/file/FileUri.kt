package io.github.taetae98coding.diary.core.model.file

import kotlin.jvm.JvmInline

/**
 * 플랫폼마다 파일을 가리키는 형식이 달라 스킴이 다른 URI 문자열로 통일한다.
 * android는 content://, ios·jvm은 file://, wasmJs는 blob:을 사용하고 해석은 이 모듈의 구현이 맡는다.
 */
@JvmInline
public value class FileUri(
    public val value: String,
)
