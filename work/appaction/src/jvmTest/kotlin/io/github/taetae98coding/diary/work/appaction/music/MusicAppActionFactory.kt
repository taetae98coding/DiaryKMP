package io.github.taetae98coding.diary.work.appaction.music

import io.github.taetae98coding.diary.core.testing.playlist.music
import io.github.taetae98coding.diary.work.appaction.core.findUseCase
import io.github.taetae98coding.diary.work.appaction.core.useCase
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

internal fun musicAppActionOf(vararg useCaseArray: Any): MusicAppAction {
    val useCaseList = useCaseArray.toList()

    return MusicAppAction(
        pageMusicUseCase = useCaseList.useCase(),
        findMusicUseCase = useCaseList.findUseCase { Arb.music().next().copy(isDeleted = false) },
        getEntityTagUseCase = useCaseList.useCase(),
        addMusicUseCase = useCaseList.useCase(),
        updateMusicUseCase = useCaseList.useCase(),
        deleteMusicUseCase = useCaseList.useCase(),
        restoreMusicUseCase = useCaseList.useCase(),
    )
}
