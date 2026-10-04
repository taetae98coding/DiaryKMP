package io.github.taetae98coding.diary.core.network.impl.file.transport

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import platform.Foundation.NSURLSessionTask

// 앞선 버전이 시작한 전송은 파일 내용만 보냈으므로 앞부분이 없고, 파일 크기는 전송 수단이 알려 준 전체 크기와 같다.
// 시작한 계정도 남기지 않았으므로, 계정이 없는 전송은 어느 계정의 것도 아닌 것으로 본다.
@Serializable
internal data class UploadTaskDescription(
    val name: String,
    val path: String,
    val headBytes: Long = 0,
    val contentLength: Long? = null,
    val accountId: String? = null,
)

internal val uploadTaskJson: Json = Json { ignoreUnknownKeys = true }

// 이 앱이 만든 전송은 언제나 설명을 남기므로, 설명을 읽지 못하는 전송은 이름과 사본의 위치를 모르는 것으로 두고 전송 결과는 그대로 다룬다.
internal var NSURLSessionTask.uploadTaskDescription: UploadTaskDescription?
    get() = taskDescription?.let { value -> runCatching { uploadTaskJson.decodeFromString<UploadTaskDescription>(value) }.getOrNull() }
    set(value) {
        taskDescription = value?.let { description -> uploadTaskJson.encodeToString(description) }
    }
