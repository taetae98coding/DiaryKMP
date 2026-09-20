package io.github.taetae98coding.diary.library.ktor

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js

public actual fun createPlatformHttpClientEngine(): HttpClientEngine = Js.create()
