package io.github.taetae98coding.diary.app.shared.web

import io.github.taetae98coding.diary.domain.web.usecase.SaveWebLoginUseCase
import kotlinx.coroutines.runBlocking
import org.koin.mp.KoinPlatform

internal class WebLoginExitSaver(
    private val saveWebLoginUseCase: SaveWebLoginUseCase,
) {
    suspend fun save() {
        saveWebLoginUseCase(parameter = Unit)
    }
}

// 앱 창이 닫히면 화면과 함께 화면이 시작한 작업도 끝나므로, 프로세스를 끝내기 전에 여기서 보관을 마친다.
public fun saveWebLoginOnExit() {
    runBlocking { WebLoginExitSaver(saveWebLoginUseCase = KoinPlatform.getKoin().get()).save() }
}
