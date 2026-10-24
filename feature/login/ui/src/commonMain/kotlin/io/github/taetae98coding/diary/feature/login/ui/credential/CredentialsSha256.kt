package io.github.taetae98coding.diary.feature.login.ui.credential

import org.kotlincrypto.hash.sha2.SHA256

internal fun ByteArray.sha256(): ByteArray = SHA256().digest(this)
