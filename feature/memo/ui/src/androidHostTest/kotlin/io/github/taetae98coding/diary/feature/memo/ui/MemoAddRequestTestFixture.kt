package io.github.taetae98coding.diary.feature.memo.ui

import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.feature.contact.api.ContactAddedResult
import io.github.taetae98coding.diary.feature.contact.api.contactAddedResultKey
import io.github.taetae98coding.diary.feature.place.api.PlaceAddedResult
import io.github.taetae98coding.diary.feature.place.api.placeAddedResultKey
import io.github.taetae98coding.diary.feature.tag.api.TagAddedResult
import io.github.taetae98coding.diary.feature.tag.api.tagAddedResultKey
import io.github.taetae98coding.diary.feature.web.api.WebAddedResult
import io.github.taetae98coding.diary.feature.web.api.webAddedResultKey
import kotlin.uuid.Uuid

internal val TEST_ADD_REQUEST_KEY: Uuid = Uuid.parse("10000000-0000-0000-0000-000000000001")

/**
 * 추가 화면은 자기를 연 입력의 요청 키로만 결과를 돌려주므로, 테스트도 같은 키로 추가 결과를 보낸다.
 */
internal fun ResultEventBus.sendTagAddedResult(
    id: Uuid,
    requestKey: Uuid = TEST_ADD_REQUEST_KEY,
) {
    sendResult(resultKey = tagAddedResultKey(requestKey = requestKey), result = TagAddedResult(id = id))
}

internal fun ResultEventBus.sendWebAddedResult(
    id: Uuid,
    requestKey: Uuid = TEST_ADD_REQUEST_KEY,
) {
    sendResult(resultKey = webAddedResultKey(requestKey = requestKey), result = WebAddedResult(id = id))
}

internal fun ResultEventBus.sendContactAddedResult(
    id: Uuid,
    requestKey: Uuid = TEST_ADD_REQUEST_KEY,
) {
    sendResult(resultKey = contactAddedResultKey(requestKey = requestKey), result = ContactAddedResult(id = id))
}

internal fun ResultEventBus.sendPlaceAddedResult(
    id: Uuid,
    requestKey: Uuid = TEST_ADD_REQUEST_KEY,
) {
    sendResult(resultKey = placeAddedResultKey(requestKey = requestKey), result = PlaceAddedResult(id = id))
}
