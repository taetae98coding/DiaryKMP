package io.github.taetae98coding.diary.work.mcp.tool.checklist

import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.work.mcp.tool.json.put
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

// 도구가 내보내는 JSON의 기대값을 공통 동작의 결과를 거치지 않고 도메인 모델에서 직접 만든다.
internal fun ChecklistSummary.toJson(): JsonObject =
    buildJsonObject {
        put("id", checklist.id)
        put("title", checklist.detail.title)
        put("description", checklist.detail.description)
        put("checkedCount", checkedCount)
        put("totalCount", totalCount)
        put("createdAt", checklist.createdAt)
        put("updatedAt", checklist.updatedAt)
    }
