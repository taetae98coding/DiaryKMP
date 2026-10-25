package io.github.taetae98coding.diary.feature.file.ui

import androidx.navigation3.runtime.NavBackStack
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.file.api.FileAddNavKey
import io.github.taetae98coding.diary.feature.file.api.FileDetailNavKey
import io.github.taetae98coding.diary.feature.file.api.FileHomeNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class FileNavigationTest :
    FunSpec({
        test("TC-ef8a7743-df68-44e9-8a97-a56e91fba5cf 파일 목록의 뒤로가기 동작은 상세 영역의 화면과 관계없이 더보기로 돌아간다") {
            val detailCases =
                listOf(
                    emptyList(),
                    listOf(FileAddNavKey),
                    listOf(FileDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())),
                    listOf(FileDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()), FileAddNavKey),
                )

            detailCases.forEach { detailKeyList ->
                val backStack = fileBackStack(detailKeyList = detailKeyList)

                backStack.navigateUpFromFileHome()

                backStack shouldContainExactly listOf(MoreHomeStubNavKey)
            }
        }

        test("TC-3138af23-fb7b-408c-8a86-92eead539d42 FileDetail이 놓여 있으면 새 파일의 FileDetail로 교체한다") {
            val first = FileDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
            val second = FileDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
            val backStack = fileBackStack(detailKeyList = listOf(first))

            backStack.navigateToFileDetail(id = second.id)

            backStack shouldContainExactly listOf(MoreHomeStubNavKey, FileHomeNavKey, second)
        }

        test("TC-d314030a-4b36-4de7-b07b-eb59d6d8cd8b 목록에서 파일을 선택하면 목록 위에 쌓인 FileDetail과 FileAdd를 모두 걷어내고 그 FileDetail로 바꾼다") {
            val detailCases =
                listOf(
                    listOf(FileAddNavKey),
                    listOf(FileDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()), FileAddNavKey),
                )

            detailCases.forEach { detailKeyList ->
                val selected = FileDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
                val backStack = fileBackStack(detailKeyList = detailKeyList)

                backStack.navigateToFileDetail(id = selected.id)

                backStack shouldContainExactly listOf(MoreHomeStubNavKey, FileHomeNavKey, selected)
            }
        }

        test("상세 영역에 FileDetail이 없으면 목록에서 선택한 FileDetail을 이어서 놓는다") {
            val detail = FileDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
            val backStack = fileBackStack(detailKeyList = emptyList())

            backStack.navigateToFileDetail(id = detail.id)

            backStack shouldContainExactly listOf(MoreHomeStubNavKey, FileHomeNavKey, detail)
        }

        test("파일 목록이 없는 전환 이력에서는 파일 목록 뒤로가기 동작이 전환 이력을 바꾸지 않는다") {
            val backStack = NavBackStack<ScreenNavKey>(MoreHomeStubNavKey, FileAddNavKey)
            val expected = backStack.toList()

            backStack.navigateUpFromFileHome()

            backStack shouldContainExactly expected
        }
    })

// 파일 목록으로 진입하는 `더보기` 화면을 대신한다. `더보기` 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object MoreHomeStubNavKey : ScreenNavKey {
    override val screenName: String
        get() = "MoreHomeStub"
}

private fun fileBackStack(detailKeyList: List<ScreenNavKey>): NavBackStack<ScreenNavKey> =
    NavBackStack(
        MoreHomeStubNavKey,
        FileHomeNavKey,
        *detailKeyList.toTypedArray(),
    )
