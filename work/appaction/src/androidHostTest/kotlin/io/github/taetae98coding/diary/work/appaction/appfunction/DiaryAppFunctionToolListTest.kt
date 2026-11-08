package io.github.taetae98coding.diary.work.appaction.appfunction

import android.content.Context
import androidx.appfunctions.testing.AppFunctionTestRule
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionServiceTester
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryAppFunctionToolListTest {
    private val context: Context = RuntimeEnvironment.getApplication()

    @get:Rule
    val appFunctionTestRule = AppFunctionTestRule(context)

    @Test
    fun `TC-e250b762-a14f-4790-a548-b05753aec7a6 데스크톱 MCP 도구와 같은 기능을 제공한다`() =
        runBlocking<Unit> {
            val tester = AppFunctionServiceTester(context = context, appFunctionManager = appFunctionTestRule.getAppFunctionManager())

            tester.functionNameList() shouldContainExactlyInAnyOrder mcpToolNameList.map { name -> name.toLowerCamel() }
        }
}

private fun String.toLowerCamel(): String = split('_').mapIndexed { index, word -> if (index == 0) word else word.replaceFirstChar(Char::uppercaseChar) }.joinToString("")

// 데스크톱 MCP 도구 이름(work/mcp의 McpToolRegistry가 제공하는 목록). :work:mcp는 JVM 전용 서버를 담아 이 모듈의 테스트가 의존하지 않으므로 이름을 옮겨 적는다.
private val mcpToolNameList: List<String> =
    listOf(
        "calendar_birthday_list",
        "calendar_dday_list",
        "calendar_memo_list",
        "checklist_add",
        "checklist_delete",
        "checklist_get",
        "checklist_item_add",
        "checklist_item_check",
        "checklist_item_delete",
        "checklist_item_uncheck",
        "checklist_list",
        "checklist_reset",
        "checklist_restore",
        "checklist_update",
        "contact_add",
        "contact_delete",
        "contact_favorite",
        "contact_get",
        "contact_list",
        "contact_memo_finished_list",
        "contact_memo_list",
        "contact_restore",
        "contact_unfavorite",
        "contact_update",
        "dday_add",
        "dday_delete",
        "dday_get",
        "dday_list",
        "dday_restore",
        "dday_tag_add",
        "dday_tag_remove",
        "dday_update",
        "holiday_list",
        "lunar_list",
        "memo_add",
        "memo_contact_add",
        "memo_contact_remove",
        "memo_copy",
        "memo_delete",
        "memo_finish",
        "memo_finished_list",
        "memo_get",
        "memo_list",
        "memo_place_add",
        "memo_place_remove",
        "memo_primary_tag_set",
        "memo_primary_tag_unset",
        "memo_restart",
        "memo_restore",
        "memo_tag_add",
        "memo_tag_remove",
        "memo_update",
        "memo_web_add",
        "memo_web_remove",
        "music_add",
        "music_delete",
        "music_get",
        "music_list",
        "music_restore",
        "music_update",
        "place_add",
        "place_delete",
        "place_get",
        "place_list",
        "place_list_in_area",
        "place_memo_finished_list",
        "place_memo_list",
        "place_restore",
        "place_tag_add",
        "place_tag_remove",
        "place_update",
        "qr_add",
        "qr_delete",
        "qr_get",
        "qr_list",
        "qr_restore",
        "qr_update",
        "search_dday",
        "search_memo",
        "search_place",
        "search_tag",
        "search_web",
        "tag_add",
        "tag_delete",
        "tag_finish",
        "tag_finished_list",
        "tag_get",
        "tag_link_add",
        "tag_link_remove",
        "tag_list",
        "tag_memo_finished_list",
        "tag_memo_list",
        "tag_place_list",
        "tag_restart",
        "tag_restore",
        "tag_update",
        "tag_web_list",
        "web_add",
        "web_delete",
        "web_get",
        "web_list",
        "web_memo_finished_list",
        "web_memo_list",
        "web_restore",
        "web_tag_add",
        "web_tag_remove",
        "web_update",
    )
