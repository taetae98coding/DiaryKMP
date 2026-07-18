package io.github.taetae98coding.diary.core.model.gemini

public data class GeminiSetting(
    val apiKey: String,
    val model: String,
    val systemPrompt: String,
) {
    public companion object {
        public val EMPTY: GeminiSetting =
            GeminiSetting(
                apiKey = "",
                model = "",
                systemPrompt = "",
            )
    }
}
