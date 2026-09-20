package io.github.taetae98coding.diary.library.ktor

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

public actual fun createPlatformHttpClientEngine(): HttpClientEngine = Darwin.create()
