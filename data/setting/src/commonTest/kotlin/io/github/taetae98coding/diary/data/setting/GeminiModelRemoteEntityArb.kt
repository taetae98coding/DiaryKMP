package io.github.taetae98coding.diary.data.setting

import io.github.taetae98coding.diary.core.gemini.network.api.entity.GeminiModelRemoteEntity
import io.github.taetae98coding.diary.library.fixture.MAX_COLLECTION_SIZE
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string

internal fun Arb.Companion.geminiModelRemoteEntity(): Arb<GeminiModelRemoteEntity> =
    arbitrary {
        GeminiModelRemoteEntity(
            id = Arb.string().bind(),
            displayName = Arb.string().bind(),
            description = Arb.string().bind(),
            supportedGenerationMethods = Arb.list(Arb.string(), 0..MAX_COLLECTION_SIZE).bind(),
        )
    }
