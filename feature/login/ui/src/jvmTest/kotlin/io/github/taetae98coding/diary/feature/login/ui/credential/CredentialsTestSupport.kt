package io.github.taetae98coding.diary.feature.login.ui.credential

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import java.net.URI
import java.security.MessageDigest
import java.util.Base64

internal fun String.queryParameter(name: String): String? = URI(this).rawQuery.decodeUrlQuery()[name]

// 검증 대상과 다른 구현(JDK)으로 RFC 7636 S256 challenge를 만든다.
internal fun s256CodeChallenge(codeVerifier: String): String =
    Base64
        .getUrlEncoder()
        .withoutPadding()
        .encodeToString(
            MessageDigest
                .getInstance("SHA-256")
                .digest(codeVerifier.encodeToByteArray()),
        )

internal fun FixtureMonkey.appleCredentialsConfig(): AppleCredentialsConfig =
    AppleCredentialsConfig(
        clientId = "io.github.taetae98coding.diary.web-${giveMeOne<String>()}",
        callbackUrl = "https://${giveMeOne<Long>().toULong()}.supabase.co/functions/v1/v1-session-apple-callback",
    )
