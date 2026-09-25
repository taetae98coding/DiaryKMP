@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.logger.analytics.impl

import kotlinx.cinterop.ExperimentalForeignApi
import swiftPMImport.DiaryKmp.logger.analytics.logger.analytics.impl.FIRAnalytics
import swiftPMImport.DiaryKmp.logger.analytics.logger.analytics.impl.kFIREventScreenView
import swiftPMImport.DiaryKmp.logger.analytics.logger.analytics.impl.kFIRParameterScreenName

// Objective-C 전역 상수에는 nullability 정보가 없어 cinterop이 String?으로 넘겨준다.
internal actual fun logScreenView(screenName: String) {
    FIRAnalytics.logEventWithName(
        name = kFIREventScreenView.orEmpty(),
        parameters = mapOf(kFIRParameterScreenName.orEmpty() to screenName),
    )
}

internal actual fun logEvent(
    name: String,
    parameters: Map<String, Any>,
) {
    FIRAnalytics.logEventWithName(
        name = name,
        parameters =
            parameters.entries.associate<_, Any?, Any> { (key, value) ->
                key to
                    when (value) {
                        is String, is Int, is Long, is Double -> value
                        else -> value.toString()
                    }
            },
    )
}
