package io.github.taetae98coding.diary.core.datastore.impl

import kotlinx.serialization.Serializable

// 이전 버전이 GeminiSettingLocalEntity를 그대로 저장했으므로, 이미 저장된 파일을 읽을 수 있게 항목 이름과 기본값을 그대로 유지한다.
@Serializable
internal data class GeminiSettingData(
    val apiKey: String = "",
    val model: String = "",
    val systemPrompt: String = "",
)
