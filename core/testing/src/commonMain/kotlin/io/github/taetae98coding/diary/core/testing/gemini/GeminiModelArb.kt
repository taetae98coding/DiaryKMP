package io.github.taetae98coding.diary.core.testing.gemini

import io.github.taetae98coding.diary.core.model.gemini.GeminiModel
import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.string

public fun Arb.Companion.geminiModel(): Arb<GeminiModel> =
    arbitrary {
        GeminiModel(
            id = Arb.string().bind(),
            displayName = Arb.string().bind(),
            description = Arb.string().bind(),
        )
    }

public fun Arb.Companion.geminiSetting(): Arb<GeminiSetting> =
    arbitrary {
        GeminiSetting(
            apiKey = Arb.string().bind(),
            model = Arb.string().bind(),
            systemPrompt = Arb.string().bind(),
        )
    }
