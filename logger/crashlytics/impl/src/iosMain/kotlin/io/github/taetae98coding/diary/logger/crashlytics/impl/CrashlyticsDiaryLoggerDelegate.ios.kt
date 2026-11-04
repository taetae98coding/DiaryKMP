@file:OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)

package io.github.taetae98coding.diary.logger.crashlytics.impl

import io.github.taetae98coding.diary.logger.crashlytics.api.CrashlyticsLog
import kotlinx.cinterop.ExperimentalForeignApi
import swiftPMImport.DiaryKmp.logger.crashlytics.logger.crashlytics.impl.FIRCrashlytics
import swiftPMImport.DiaryKmp.logger.crashlytics.logger.crashlytics.impl.FIRExceptionModel
import swiftPMImport.DiaryKmp.logger.crashlytics.logger.crashlytics.impl.FIRStackFrame
import kotlin.experimental.ExperimentalNativeApi

private const val UNKNOWN_EXCEPTION_NAME = "Throwable"

internal actual fun recordException(log: CrashlyticsLog) {
    val throwable = log.throwable
    val symbolList = if (log.issueKey.isEmpty()) throwable.getStackTrace().toList() else listOf(log.issueKey)
    val exceptionModel =
        FIRExceptionModel(
            name = throwable::class.simpleName ?: UNKNOWN_EXCEPTION_NAME,
            reason = throwable.message.orEmpty(),
        ).apply {
            stackTrace = symbolList.map { symbol -> FIRStackFrame.stackFrameWithSymbol(symbol = symbol, file = "", line = 0L) }
        }

    FIRCrashlytics.crashlytics().apply {
        log(msg = log.message)
        recordExceptionModel(exceptionModel = exceptionModel)
    }
}
