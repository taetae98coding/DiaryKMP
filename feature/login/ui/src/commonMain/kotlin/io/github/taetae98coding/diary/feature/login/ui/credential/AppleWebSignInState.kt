package io.github.taetae98coding.diary.feature.login.ui.credential

import kotlin.io.encoding.Base64

// Apple은 state를 그대로 돌려주므로, 서버 콜백이 앱 복귀 주소를 되찾을 수 있도록 토큰 뒤에 주소를 함께 담는다.
internal object AppleWebSignInState {
    private const val SEPARATOR = '.'
    private val returnUriEncoder = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)

    fun encode(
        token: String,
        returnUri: String,
    ): String = "$token$SEPARATOR${returnUriEncoder.encode(returnUri.encodeToByteArray())}"

    fun decodeReturnUri(state: String): String? {
        val encoded = state.substringAfter(SEPARATOR, missingDelimiterValue = "")

        return runCatching { returnUriEncoder.decode(encoded).decodeToString() }
            .getOrNull()
            ?.takeIf { it.isNotEmpty() }
    }
}
