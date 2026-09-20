package io.github.taetae98coding.diary.library.ktor

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

public actual fun createPlatformHttpClientEngine(): HttpClientEngine = OkHttp.create()
