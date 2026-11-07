package io.github.taetae98coding.diary.core.testing.music

import io.github.taetae98coding.diary.core.network.api.music.entity.MusicDetailRemoteEntity
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.string

public fun Arb.Companion.musicDetailRemoteEntity(): Arb<MusicDetailRemoteEntity> =
    arbitrary {
        MusicDetailRemoteEntity(
            link = Arb.string().bind(),
            title = Arb.string().bind(),
            artist = Arb.string().bind(),
            thumbnail = Arb.string().bind(),
        )
    }
