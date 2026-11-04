package io.github.taetae98coding.diary.core.testing.music

import io.github.taetae98coding.diary.core.database.api.music.entity.MusicDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.string

public fun Arb.Companion.musicDetailLocalEntity(): Arb<MusicDetailLocalEntity> =
    arbitrary {
        MusicDetailLocalEntity(
            link = Arb.string().bind(),
            title = Arb.string().bind(),
            artist = Arb.string().bind(),
            thumbnail = Arb.string().bind(),
        )
    }

public fun Arb.Companion.musicLocalEntity(): Arb<MusicLocalEntity> =
    arbitrary {
        MusicLocalEntity(
            id = Arb.kotlinUuid().bind(),
            detail = Arb.musicDetailLocalEntity().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }
