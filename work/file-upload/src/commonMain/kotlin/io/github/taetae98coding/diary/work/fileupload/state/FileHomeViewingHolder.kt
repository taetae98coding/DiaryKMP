package io.github.taetae98coding.diary.work.fileupload.state

import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.annotation.Single

// 앱 프로세스가 새로 시작되면 FileHome이 아직 보이지 않으므로 보고 있지 않은 상태에서 시작한다.
@Single
internal class FileHomeViewingHolder {
    private val state = MutableStateFlow(false)

    var isViewing: Boolean
        get() = state.value
        set(value) {
            state.value = value
        }
}
