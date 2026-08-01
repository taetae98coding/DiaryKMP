package io.github.taetae98coding.diary.feature.memo.ui

import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.feature.tag.api.TagAddedResult
import io.github.taetae98coding.diary.feature.tag.api.tagAddedResultKey
import kotlin.uuid.Uuid

internal val TEST_TAG_ADD_REQUEST_KEY: Uuid = Uuid.parse("10000000-0000-0000-0000-000000000001")

/**
 * TagAdd 화면은 자기를 연 태그 입력의 요청 키로만 결과를 돌려주므로, 테스트도 같은 키로 추가 결과를 보낸다.
 */
internal fun ResultEventBus.sendTagAddedResult(
    id: Uuid,
    requestKey: Uuid = TEST_TAG_ADD_REQUEST_KEY,
) {
    sendResult(resultKey = tagAddedResultKey(requestKey = requestKey), result = TagAddedResult(id = id))
}
