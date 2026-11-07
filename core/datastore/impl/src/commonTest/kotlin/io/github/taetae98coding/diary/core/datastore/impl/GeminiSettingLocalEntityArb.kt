package io.github.taetae98coding.diary.core.datastore.impl

import io.github.taetae98coding.diary.core.datastore.api.setting.entity.GeminiSettingLocalEntity
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.string

internal fun Arb.Companion.geminiSettingLocalEntity(): Arb<GeminiSettingLocalEntity> =
    arbitrary {
        GeminiSettingLocalEntity(
            apiKey = Arb.string().bind(),
            model = Arb.string().bind(),
            systemPrompt = Arb.string().bind(),
        )
    }
