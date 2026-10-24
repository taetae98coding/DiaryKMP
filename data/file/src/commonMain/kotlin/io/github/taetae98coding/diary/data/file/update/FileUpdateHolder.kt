package io.github.taetae98coding.diary.data.file.update

import io.github.taetae98coding.diary.core.model.file.DiaryFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

// 이미 불러온 목록은 다시 불러오기 전까지 서버에서 받은 옛 정보를 들고 있으므로, 고친 정보를 따로 두고 목록이 덧씌워 보이게 한다.
// 고친 뒤에 시작한 불러오기가 그 파일을 받아 오면 서버의 정보가 더 새로우므로 덧씌우기를 걷는다.
@Single
internal class FileUpdateHolder {
    private val state = MutableStateFlow(FileUpdateState())

    val updated: Flow<Map<Uuid, DiaryFile>> = state.map { value -> value.fileMap.mapValues { (_, entry) -> entry.file } }

    fun currentVersion(): Long = state.value.version

    fun put(file: DiaryFile) {
        state.update { value ->
            val version = value.version + 1

            value.copy(
                version = version,
                fileMap = value.fileMap + (file.id to FileUpdateEntry(file = file, version = version)),
            )
        }
    }

    fun onLoaded(
        idSet: Set<Uuid>,
        loadStartVersion: Long,
    ) {
        state.update { value ->
            value.copy(fileMap = value.fileMap.filterNot { (id, entry) -> id in idSet && entry.version <= loadStartVersion })
        }
    }
}

private data class FileUpdateState(
    val version: Long = 0,
    val fileMap: Map<Uuid, FileUpdateEntry> = emptyMap(),
)

private data class FileUpdateEntry(
    val file: DiaryFile,
    val version: Long,
)
