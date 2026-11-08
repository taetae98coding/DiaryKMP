package io.github.taetae98coding.diary.work.appaction.music

import io.github.taetae98coding.diary.work.appaction.core.useCase

internal fun musicAppActionOf(vararg useCaseArray: Any): MusicAppAction {
    val useCaseList = useCaseArray.toList()

    return MusicAppAction(
        pageMusicUseCase = useCaseList.useCase(),
        findMusicUseCase = useCaseList.useCase(),
        addMusicUseCase = useCaseList.useCase(),
        updateMusicUseCase = useCaseList.useCase(),
        deleteMusicUseCase = useCaseList.useCase(),
        restoreMusicUseCase = useCaseList.useCase(),
    )
}
