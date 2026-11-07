package io.github.taetae98coding.diary.core.testing.naver

import io.github.taetae98coding.diary.core.naver.network.api.entity.NaverPlaceRemoteEntity
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.string

public fun Arb.Companion.naverPlaceRemoteEntity(): Arb<NaverPlaceRemoteEntity> =
    arbitrary {
        NaverPlaceRemoteEntity(
            title = Arb.string().bind(),
            address = Arb.string().bind(),
            roadAddress = Arb.string().bind(),
            mapx = Arb.string().bind(),
            mapy = Arb.string().bind(),
        )
    }
