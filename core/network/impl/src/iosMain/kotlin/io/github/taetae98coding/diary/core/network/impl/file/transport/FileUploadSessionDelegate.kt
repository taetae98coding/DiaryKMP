package io.github.taetae98coding.diary.core.network.impl.file.transport

import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionDataDelegateProtocol
import platform.Foundation.NSURLSessionDataTask
import platform.Foundation.NSURLSessionTask
import platform.darwin.NSObject

/**
 * 시스템이 앱을 깨워 이 전송의 이벤트를 모두 전달하고 나면 이 이름의 알림을 보낸다.
 * 앱 델리게이트(iosApp의 AppDelegate.swift)는 같은 이름의 알림을 받은 뒤에야 시스템이 넘겨 준 완료 처리기를 불러 앱을 다시 멈추게 해도 된다고 알린다.
 * Swift는 이 모듈을 직접 참조하지 않으므로 이름을 바꾸면 앱 델리게이트의 이름도 함께 바꾼다.
 */
private const val FILE_UPLOAD_BACKGROUND_EVENTS_FINISHED_NOTIFICATION: String = "io.github.taetae98coding.diary.fileUpload.backgroundEventsFinished"

internal class FileUploadSessionDelegate(
    private val onSent: (task: NSURLSessionTask, sentBytes: Long, contentLength: Long) -> Unit,
    private val onReceive: (task: NSURLSessionTask, data: NSData) -> Unit,
    private val onComplete: (task: NSURLSessionTask, error: NSError?) -> Unit,
) : NSObject(),
    NSURLSessionDataDelegateProtocol {
    override fun URLSession(
        session: NSURLSession,
        task: NSURLSessionTask,
        didSendBodyData: Long,
        totalBytesSent: Long,
        totalBytesExpectedToSend: Long,
    ) {
        onSent(task, totalBytesSent, totalBytesExpectedToSend)
    }

    override fun URLSession(
        session: NSURLSession,
        dataTask: NSURLSessionDataTask,
        didReceiveData: NSData,
    ) {
        onReceive(dataTask, didReceiveData)
    }

    override fun URLSession(
        session: NSURLSession,
        task: NSURLSessionTask,
        didCompleteWithError: NSError?,
    ) {
        onComplete(task, didCompleteWithError)
    }

    override fun URLSessionDidFinishEventsForBackgroundURLSession(session: NSURLSession) {
        NSNotificationCenter.defaultCenter.postNotificationName(aName = FILE_UPLOAD_BACKGROUND_EVENTS_FINISHED_NOTIFICATION, `object` = null)
    }
}
