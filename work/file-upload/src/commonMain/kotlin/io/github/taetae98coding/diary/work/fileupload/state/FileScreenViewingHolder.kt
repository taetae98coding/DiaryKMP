package io.github.taetae98coding.diary.work.fileupload.state

import io.github.taetae98coding.diary.core.model.file.FileScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

// 앱 프로세스가 새로 시작되면 파일 화면이 아직 보이지 않으므로 보고 있지 않은 상태에서 시작한다.
@Single
internal class FileScreenViewingHolder {
    private val state = MutableStateFlow<FileScreen?>(null)

    val viewingScreen: FileScreen?
        get() = state.value

    fun start(screen: FileScreen) {
        state.value = screen
    }

    // 다음 화면이 보이기 시작한 뒤에 앞 화면이 멈출 수 있으므로, 자기 화면을 보고 있을 때만 지운다.
    fun stop(screen: FileScreen) {
        state.update { current -> if (current == screen) null else current }
    }
}
