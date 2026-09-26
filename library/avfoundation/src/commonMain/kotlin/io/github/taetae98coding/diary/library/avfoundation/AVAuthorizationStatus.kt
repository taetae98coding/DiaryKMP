package io.github.taetae98coding.diary.library.avfoundation

public enum class AVAuthorizationStatus {
    NOT_DETERMINED,
    RESTRICTED,
    DENIED,
    AUTHORIZED,
}

// AVFoundation 헤더가 정한 AVAuthorizationStatus의 NSInteger 값.
private const val NOT_DETERMINED_RAW_VALUE = 0L
private const val RESTRICTED_RAW_VALUE = 1L
private const val AUTHORIZED_RAW_VALUE = 3L

// 알 수 없는 값은 카메라를 쓸 수 없는 쪽으로 본다.
internal fun avAuthorizationStatus(rawValue: Long): AVAuthorizationStatus =
    when (rawValue) {
        NOT_DETERMINED_RAW_VALUE -> AVAuthorizationStatus.NOT_DETERMINED
        RESTRICTED_RAW_VALUE -> AVAuthorizationStatus.RESTRICTED
        AUTHORIZED_RAW_VALUE -> AVAuthorizationStatus.AUTHORIZED
        else -> AVAuthorizationStatus.DENIED
    }
