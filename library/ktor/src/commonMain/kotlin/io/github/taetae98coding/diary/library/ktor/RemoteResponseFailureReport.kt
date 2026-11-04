package io.github.taetae98coding.diary.library.ktor

import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.crashlytics.api.CrashlyticsLog
import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequest
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpResponsePipeline
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.NullBody
import io.ktor.http.isSuccess
import io.ktor.serialization.ContentConvertException
import io.ktor.util.AttributeKey

public class RemoteResponseFailureReportConfig {
    public var reportsPath: Boolean = true
}

public val RemoteResponseFailureReport: ClientPlugin<RemoteResponseFailureReportConfig> =
    createClientPlugin(name = "RemoteResponseFailureReport", createConfiguration = ::RemoteResponseFailureReportConfig) {
        val reporter = RemoteResponseFailureReporter(reportsPath = pluginConfig.reportsPath)

        onRequest { request, _ -> request.attributes.put(ReporterKey, reporter) }

        onResponse { response ->
            if (response.status.isReportedFailure()) {
                reporter.reportStatus(response = response)
            }
        }

        // 응답 내용을 읽는 변환은 응답 처리 흐름 뒤쪽에서 일어나므로 그 앞에서 감싸 변환 실패와 변환되지 않은 결과를 함께 본다.
        client.responsePipeline.intercept(HttpResponsePipeline.Receive) {
            val response = context.response
            val result =
                try {
                    proceed()
                } catch (cause: ContentConvertException) {
                    if (response.status.isSuccess()) reporter.reportUnreadableContent(response = response, reason = cause.toReason())
                    throw cause
                }

            val body = result.response
            if (response.status.isSuccess() && body !is NullBody && !result.expectedType.type.isInstance(body)) {
                reporter.reportUnreadableContent(response = response, reason = UNTRANSFORMED_REASON)
            }
        }
    }

// 응답 내용을 직접 나누어 읽는 호출처가 형식이 다른 내용을 만났을 때 같은 기준으로 보고한다. 보고 수단을 설치하지 않은 요청은 보고하지 않는다.
public fun HttpResponse.reportUnreadableContent(cause: Throwable) {
    if (!status.isSuccess()) return

    call.attributes.getOrNull(ReporterKey)?.reportUnreadableContent(response = this, reason = cause.toReason())
}

private class RemoteResponseFailureReporter(
    private val reportsPath: Boolean,
) {
    fun reportStatus(response: HttpResponse) {
        report(response = response, message = "$STATUS_FAILURE_MESSAGE ${response.call.request.toTarget()} ${response.status.value}")
    }

    fun reportUnreadableContent(
        response: HttpResponse,
        reason: String,
    ) {
        report(response = response, message = "$UNREADABLE_CONTENT_MESSAGE ${response.call.request.toTarget()} ${response.status.value} $reason")
    }

    private fun report(
        response: HttpResponse,
        message: String,
    ) {
        DiaryLogger.log(
            log =
                CrashlyticsLog(
                    message = message,
                    throwable = RemoteResponseFailureException(message = message),
                    issueKey = response.call.request.toTarget(),
                ),
        )
    }

    // 주소의 검색 조건에는 인증 키나 사용자가 입력한 값이 들어갈 수 있어 담지 않는다.
    private fun HttpRequest.toTarget(): String =
        if (reportsPath) {
            "${method.value} ${url.host}${url.encodedPath}"
        } else {
            "${method.value} ${url.host}"
        }
}

internal class RemoteResponseFailureException(
    message: String,
) : Exception(message)

// 읽지 못한 이유는 오류 종류로만 남긴다. 해석 오류의 문장에는 응답 내용 일부가 들어 있다.
private fun Throwable.toReason(): String =
    generateSequence(this) { throwable -> throwable.cause }
        .map { throwable -> throwable::class.simpleName.orEmpty() }
        .joinToString(separator = REASON_SEPARATOR)

private fun HttpStatusCode.isReportedFailure(): Boolean = this == HttpStatusCode.Unauthorized || this == HttpStatusCode.Forbidden || value in SERVER_ERROR_RANGE

private val ReporterKey: AttributeKey<RemoteResponseFailureReporter> = AttributeKey(name = "RemoteResponseFailureReporter")

private val SERVER_ERROR_RANGE: IntRange = 500..599

private const val STATUS_FAILURE_MESSAGE = "원격 응답 실패:"
private const val UNREADABLE_CONTENT_MESSAGE = "원격 응답 내용을 읽지 못함:"
private const val UNTRANSFORMED_REASON = "NoTransformationFoundException"
private const val REASON_SEPARATOR = " < "
