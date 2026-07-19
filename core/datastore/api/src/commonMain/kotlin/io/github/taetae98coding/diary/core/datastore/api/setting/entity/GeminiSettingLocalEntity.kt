package io.github.taetae98coding.diary.core.datastore.api.setting.entity

import kotlinx.serialization.Serializable

@Serializable
public data class GeminiSettingLocalEntity(
    val apiKey: String = "",
    val model: String = "",
    val systemPrompt: String = "",
)
