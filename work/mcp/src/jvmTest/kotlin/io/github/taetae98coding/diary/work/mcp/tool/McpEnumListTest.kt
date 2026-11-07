package io.github.taetae98coding.diary.work.mcp.tool

import io.github.taetae98coding.diary.core.model.checklist.ChecklistItemSort
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.work.mcp.tool.checklist.checklistItemSortList
import io.github.taetae98coding.diary.work.mcp.tool.contact.contactBirthdayCalendarList
import io.github.taetae98coding.diary.work.mcp.tool.tag.tagScopeList
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

class McpEnumListTest :
    FunSpec({
        test("태그 범위는 이 태그만, 직접 연결, 모든 연결 순서로 알린다") {
            tagScopeList shouldBe listOf(TagScope.SELF, TagScope.CHILD, TagScope.DESCENDANT)
        }

        test("태그 범위 목록은 모든 범위를 한 번씩 담는다") {
            tagScopeList shouldContainExactlyInAnyOrder TagScope.entries
        }

        test("생일 달력은 양력, 음력 순서로 알린다") {
            contactBirthdayCalendarList shouldBe listOf(ContactBirthdayCalendar.SOLAR, ContactBirthdayCalendar.LUNAR)
        }

        test("생일 달력 목록은 모든 달력을 한 번씩 담는다") {
            contactBirthdayCalendarList shouldContainExactlyInAnyOrder ContactBirthdayCalendar.entries
        }

        test("체크 항목 정렬은 추가순, 체크 안 된 항목 우선 순서로 알리고 추가순이 처음 정렬이다") {
            checklistItemSortList shouldBe listOf(ChecklistItemSort.ADDED, ChecklistItemSort.UNCHECKED_FIRST)
        }

        test("체크 항목 정렬 목록은 모든 정렬을 한 번씩 담는다") {
            checklistItemSortList shouldContainExactlyInAnyOrder ChecklistItemSort.entries
        }
    })
