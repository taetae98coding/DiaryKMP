package io.github.taetae98coding.diary.work.fileupload.state

import io.github.taetae98coding.diary.core.model.file.FileScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

// 앱 프로세스가 새로 시작되면 파일 화면이 아직 보이지 않으므로 보고 있지 않은 상태에서 시작한다.
// 목록·상세 배치는 FileHome과 FileAdd를 함께 보이고, 화면이 바뀔 때는 다음 화면이 보이기 시작한 뒤에 앞 화면이 멈출 수 있어,
// 같은 화면이 둘 이상 동시에 보일 수도 있으므로 화면마다 보이는 수를 센다.
@Single
internal class FileScreenViewingHolder {
    private val state = MutableStateFlow<Map<FileScreen, Int>>(emptyMap())

    val viewingScreen: FileScreen?
        get() {
            val countMap = state.value

            return when {
                countMap.isViewing(FileScreen.ADD) -> FileScreen.ADD
                countMap.isViewing(FileScreen.HOME) -> FileScreen.HOME
                else -> null
            }
        }

    fun start(screen: FileScreen) {
        state.update { countMap -> countMap + (screen to countMap.getOrElse(screen) { 0 } + 1) }
    }

    fun stop(screen: FileScreen) {
        state.update { countMap ->
            val count = countMap.getOrElse(screen) { 0 } - 1

            if (count > 0) countMap + (screen to count) else countMap - screen
        }
    }

    private fun Map<FileScreen, Int>.isViewing(screen: FileScreen): Boolean = getOrElse(screen) { 0 } > 0
}
