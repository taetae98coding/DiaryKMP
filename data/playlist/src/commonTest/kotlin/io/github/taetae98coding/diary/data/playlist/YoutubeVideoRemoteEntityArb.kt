package io.github.taetae98coding.diary.data.playlist

import io.github.taetae98coding.diary.core.youtube.network.api.entity.YoutubeVideoRemoteEntity
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.string

internal fun Arb.Companion.youtubeVideoRemoteEntity(): Arb<YoutubeVideoRemoteEntity> =
    arbitrary {
        YoutubeVideoRemoteEntity(
            title = Arb.string().bind(),
            authorName = Arb.string().bind(),
        )
    }
