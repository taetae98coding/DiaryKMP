@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package io.github.taetae98coding.diary.core.network.impl.file.transport

import io.github.taetae98coding.diary.core.network.api.file.entity.ContinuedFileUploadRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.ContinuedFileUploadResultRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.FileRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.exception.FileTooLargeRemoteException
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunctionRequest
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.io.RawSource
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSMutableData
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSURL
import platform.Foundation.NSURLErrorCancelled
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionConfiguration
import platform.Foundation.NSURLSessionDataDelegateProtocol
import platform.Foundation.NSURLSessionDataTask
import platform.Foundation.NSURLSessionTask
import platform.Foundation.NSUserDomainMask
import platform.Foundation.appendData
import platform.Foundation.setHTTPMethod
import platform.Foundation.setValue
import platform.darwin.NSObject
import platform.posix.memcpy
import kotlin.uuid.Uuid

private const val FILE_UPLOAD_SESSION_IDENTIFIER: String = "io.github.taetae98coding.diary.fileUpload"
private const val UPLOAD_DIRECTORY_NAME: String = "file-upload"

/**
 * 시스템이 앱을 깨워 이 전송의 이벤트를 모두 전달하고 나면 이 이름의 알림을 보낸다.
 * 앱 델리게이트(iosApp의 AppDelegate.swift)는 같은 이름의 알림을 받은 뒤에야 시스템이 넘겨 준 완료 처리기를 불러 앱을 다시 멈추게 해도 된다고 알린다.
 * Swift는 이 모듈을 직접 참조하지 않으므로 이름을 바꾸면 앱 델리게이트의 이름도 함께 바꾼다.
 */
private const val FILE_UPLOAD_BACKGROUND_EVENTS_FINISHED_NOTIFICATION: String = "io.github.taetae98coding.diary.fileUpload.backgroundEventsFinished"

// 시스템의 백그라운드 전송은 앱이 멈추거나 정리되어도 파일을 끝까지 보낸다. 대신 메모리의 내용이 아니라 기기의 파일에서만 보낼 수 있어,
// 보낼 내용을 앱의 캐시 디렉터리에 옮겨 두고 전송이 끝나면 지운다. 앞선 실행에서 시작한 전송은 이 실행의 호출자가 없으므로 따로 알린다.
internal class BackgroundSessionFileUploadTransport(
    private val supabaseFunction: SupabaseFunction,
    private val dispatcher: CoroutineDispatcher,
) : FileUploadTransport {
    // 전송 수단은 이벤트를 메인 큐로 전달하므로, 아래 상태는 메인 큐에서만 읽고 쓴다.
    private val pendingUploadMap = mutableMapOf<ULong, PendingUpload>()
    private val responseDataMap = mutableMapOf<ULong, NSMutableData>()

    private val continuedUpload = MutableStateFlow<ContinuedFileUploadRemoteEntity?>(null)
    private val continuedUploadResult = Channel<ContinuedFileUploadResultRemoteEntity>(Channel.BUFFERED)

    private val delegate = SessionDelegate()

    // 같은 식별자로 세션을 다시 만들어야 앞선 실행에서 시작한 전송의 이벤트를 이어 받는다.
    private val session: NSURLSession =
        NSURLSession.sessionWithConfiguration(
            configuration =
                NSURLSessionConfiguration.backgroundSessionConfigurationWithIdentifier(FILE_UPLOAD_SESSION_IDENTIFIER).apply {
                    sessionSendsLaunchEvents = true
                    discretionary = false
                },
            delegate = delegate,
            delegateQueue = NSOperationQueue.mainQueue,
        )

    init {
        restoreContinuedUpload()
    }

    override suspend fun upload(
        name: String,
        title: String,
        description: String,
        mimeType: String,
        contentLength: Long,
        openContent: suspend () -> RawSource,
        onSent: (sentBytes: Long) -> Unit,
    ): FileRemoteEntity {
        val path = Path(uploadDirectoryPath(), Uuid.random().toString())
        val multipart =
            FileUploadMultipart(
                name = name,
                title = title,
                description = description,
                mimeType = mimeType,
                contentLength = contentLength,
            )
        var isTaskStarted = false

        // 전송을 시작한 뒤에는 전송이 끝날 때 사본을 지우므로, 그 전에 실패하거나 취소된 경우에만 여기서 지운다.
        try {
            copyToUploadFile(path = path, multipart = multipart, openContent = openContent)
            val request = supabaseFunction.createRequest(function = UPLOAD_FILE_FUNCTION).toUploadRequest(multipart = multipart)

            return withContext(Dispatchers.Main) {
                suspendCancellableCoroutine { continuation ->
                    val task = session.uploadTaskWithRequest(request = request, fromFile = NSURL.fileURLWithPath(path.toString()))

                    task.taskDescription =
                        uploadTaskJson.encodeToString(
                            UploadTaskDescription(
                                name = name,
                                path = path.toString(),
                                headBytes = multipart.head.size.toLong(),
                                contentLength = contentLength,
                            ),
                        )
                    pendingUploadMap[task.taskIdentifier] = PendingUpload(continuation = continuation, onSent = { totalSentBytes -> onSent(multipart.contentSentBytes(totalSentBytes)) })
                    continuation.invokeOnCancellation { task.cancel() }
                    task.resume()
                    isTaskStarted = true
                }
            }
        } catch (throwable: Throwable) {
            if (!isTaskStarted) {
                withContext(NonCancellable + dispatcher) { SystemFileSystem.delete(path, mustExist = false) }
            }

            throw throwable
        }
    }

    override fun getContinuedUpload(): Flow<ContinuedFileUploadRemoteEntity?> = continuedUpload

    override fun getContinuedUploadResult(): Flow<ContinuedFileUploadResultRemoteEntity> = continuedUploadResult.receiveAsFlow()

    override suspend fun cancelContinuedUpload() {
        withContext(Dispatchers.Main) {
            session.getTasksWithCompletionHandler { _, uploadTaskList, _ ->
                uploadTaskList
                    .orEmpty()
                    .filterIsInstance<NSURLSessionTask>()
                    .filter { task -> task.taskIdentifier !in pendingUploadMap }
                    .forEach { task -> task.cancel() }
            }
            continuedUpload.value = null
        }
    }

    private suspend fun copyToUploadFile(
        path: Path,
        multipart: FileUploadMultipart,
        openContent: suspend () -> RawSource,
    ) {
        withContext(dispatcher) {
            path.parent?.let(SystemFileSystem::createDirectories)
            SystemFileSystem.sink(path).buffered().use { sink ->
                sink.write(multipart.head)
                openContent().use { source -> sink.transferFrom(source) }
                sink.write(multipart.tail)
            }
        }
    }

    private fun restoreContinuedUpload() {
        session.getTasksWithCompletionHandler { _, uploadTaskList, _ ->
            val task =
                uploadTaskList
                    .orEmpty()
                    .filterIsInstance<NSURLSessionTask>()
                    .firstOrNull { task -> task.taskIdentifier !in pendingUploadMap }

            if (task != null) {
                continuedUpload.value = task.toContinuedUpload(totalSentBytes = task.countOfBytesSent, totalLength = task.countOfBytesExpectedToSend)
            }
        }
    }

    private fun onSent(
        task: NSURLSessionTask,
        sentBytes: Long,
        contentLength: Long,
    ) {
        val pendingUpload = pendingUploadMap[task.taskIdentifier]

        if (pendingUpload != null) {
            pendingUpload.onSent(sentBytes)
        } else {
            continuedUpload.value = task.toContinuedUpload(totalSentBytes = sentBytes, totalLength = contentLength)
        }
    }

    private fun onReceive(
        task: NSURLSessionTask,
        data: NSData,
    ) {
        responseDataMap.getOrPut(task.taskIdentifier) { NSMutableData() }.appendData(data)
    }

    private fun onComplete(
        task: NSURLSessionTask,
        error: NSError?,
    ) {
        val description = task.uploadTaskDescription()
        val responseBody =
            responseDataMap
                .remove(task.taskIdentifier)
                ?.toByteArray()
                ?.decodeToString()
                .orEmpty()
        val result = task.toResult(error = error, responseBody = responseBody)

        description?.path?.let { path -> NSFileManager.defaultManager.removeItemAtPath(path, error = null) }

        val pendingUpload = pendingUploadMap.remove(task.taskIdentifier)

        if (pendingUpload != null) {
            pendingUpload.continuation.resumeWith(result)
        } else {
            continuedUpload.value = null
            if (error?.code != NSURLErrorCancelled) {
                continuedUploadResult.trySend(result.toContinuedResult(name = description?.name.orEmpty()))
            }
        }
    }

    private inner class SessionDelegate :
        NSObject(),
        NSURLSessionDataDelegateProtocol {
        override fun URLSession(
            session: NSURLSession,
            task: NSURLSessionTask,
            didSendBodyData: Long,
            totalBytesSent: Long,
            totalBytesExpectedToSend: Long,
        ) {
            onSent(task = task, sentBytes = totalBytesSent, contentLength = totalBytesExpectedToSend)
        }

        override fun URLSession(
            session: NSURLSession,
            dataTask: NSURLSessionDataTask,
            didReceiveData: NSData,
        ) {
            onReceive(task = dataTask, data = didReceiveData)
        }

        override fun URLSession(
            session: NSURLSession,
            task: NSURLSessionTask,
            didCompleteWithError: NSError?,
        ) {
            onComplete(task = task, error = didCompleteWithError)
        }

        override fun URLSessionDidFinishEventsForBackgroundURLSession(session: NSURLSession) {
            NSNotificationCenter.defaultCenter.postNotificationName(aName = FILE_UPLOAD_BACKGROUND_EVENTS_FINISHED_NOTIFICATION, `object` = null)
        }
    }

    private class PendingUpload(
        val continuation: CancellableContinuation<FileRemoteEntity>,
        val onSent: (sentBytes: Long) -> Unit,
    )
}

private fun NSURLSessionTask.toResult(
    error: NSError?,
    responseBody: String,
): Result<FileRemoteEntity> {
    val statusCode = (response as? NSHTTPURLResponse)?.statusCode?.toInt()

    return when {
        error != null -> Result.failure(IllegalStateException("File upload failed. error=${error.localizedDescription}"))

        statusCode == HttpStatusCode.PayloadTooLarge.value -> Result.failure(FileTooLargeRemoteException(message = responseBody))

        statusCode != null && statusCode in HttpStatusCode.OK.value..<HttpStatusCode.MultipleChoices.value ->
            runCatching { uploadTaskJson.decodeFromString<FileRemoteEntity>(responseBody) }

        else -> Result.failure(IllegalStateException("File upload failed. status=$statusCode, body=$responseBody"))
    }
}

private fun Result<FileRemoteEntity>.toContinuedResult(name: String): ContinuedFileUploadResultRemoteEntity =
    fold(
        onSuccess = { file -> ContinuedFileUploadResultRemoteEntity.Succeeded(name = name, file = file) },
        onFailure = { throwable ->
            if (throwable is FileTooLargeRemoteException) {
                ContinuedFileUploadResultRemoteEntity.TooLarge(name = name)
            } else {
                ContinuedFileUploadResultRemoteEntity.Failed(name = name)
            }
        },
    )

// 이 앱이 만든 전송은 언제나 설명을 남기므로, 설명을 읽지 못하는 전송은 이름과 사본의 위치를 모르는 것으로 두고 전송 결과는 그대로 다룬다.
private fun NSURLSessionTask.uploadTaskDescription(): UploadTaskDescription? = taskDescription?.let { value -> runCatching { uploadTaskJson.decodeFromString<UploadTaskDescription>(value) }.getOrNull() }

// 앞선 버전이 시작한 전송은 파일 내용만 보냈으므로 앞부분이 없고, 파일 크기는 전송 수단이 알려 준 전체 크기와 같다.
@Serializable
private data class UploadTaskDescription(
    val name: String,
    val path: String,
    val headBytes: Long = 0,
    val contentLength: Long? = null,
)

private fun NSURLSessionTask.toContinuedUpload(
    totalSentBytes: Long,
    totalLength: Long,
): ContinuedFileUploadRemoteEntity {
    val description = uploadTaskDescription()
    val contentLength = description?.contentLength ?: totalLength
    val headBytes = description?.headBytes ?: 0

    return ContinuedFileUploadRemoteEntity(
        name = description?.name.orEmpty(),
        contentLength = contentLength,
        sentBytes = (totalSentBytes - headBytes).coerceIn(0, contentLength),
    )
}

private val uploadTaskJson = Json { ignoreUnknownKeys = true }

private fun SupabaseFunctionRequest.toUploadRequest(multipart: FileUploadMultipart): NSMutableURLRequest =
    NSMutableURLRequest(uRL = checkNotNull(NSURL.URLWithString(url)) { "Function url is invalid." }).apply {
        setHTTPMethod("POST")
        headers.forEach { (key, value) -> setValue(value, forHTTPHeaderField = key) }
        setValue(multipart.contentType.toString(), forHTTPHeaderField = HttpHeaders.ContentType)
    }

private fun uploadDirectoryPath(): String {
    val cacheDirectory = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true).first() as String

    return "$cacheDirectory/$UPLOAD_DIRECTORY_NAME"
}

private fun NSData.toByteArray(): ByteArray {
    val byteArray = ByteArray(length.toInt())

    if (byteArray.isNotEmpty()) {
        byteArray.usePinned { pinned -> memcpy(pinned.addressOf(0), bytes, length) }
    }

    return byteArray
}
