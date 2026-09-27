package io.github.taetae98coding.diary.core.model.file

import kotlin.jvm.JvmInline

/**
 * 플랫폼마다 파일을 가리키는 형식이 달라 스킴이 다른 URI 문자열로 통일한다.
 * android는 content://, ios·jvm은 file://, wasmJs는 blob:을 사용하고 해석은 그 위치를 읽는 core 모듈의 플랫폼 구현이 맡는다.
 * blob URL은 원래 파일 이름을 잃으므로 wasmJs에서 사용자가 고른 파일은 fragment에 퍼센트 인코딩한 파일 이름을 붙인다.
 */
@JvmInline
public value class FileUri(
    public val value: String,
)
