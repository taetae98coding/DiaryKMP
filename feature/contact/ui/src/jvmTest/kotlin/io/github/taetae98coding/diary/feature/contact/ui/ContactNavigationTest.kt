package io.github.taetae98coding.diary.feature.contact.ui

import androidx.navigation3.runtime.NavBackStack
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactAddNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactHomeNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class ContactNavigationTest :
    FunSpec({
        test("TC-CONTACT-HOME-FEATURE-014 연락처 목록의 뒤로가기 동작은 상세 영역의 화면과 관계없이 더보기로 돌아간다") {
            val detailCases =
                listOf(
                    emptyList(),
                    listOf(ContactAddNavKey),
                    listOf(ContactDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())),
                    listOf(ContactAddNavKey, ContactDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())),
                )

            detailCases.forEach { detailKeyList ->
                val backStack = contactBackStack(detailKeyList = detailKeyList)

                backStack.navigateUpFromContactHome()

                backStack shouldContainExactly listOf(MoreHomeStubNavKey)
            }
        }

        test("TC-CONTACT-LIST-DETAIL-FEATURE-016 목록에서 연락처를 선택하면 목록 위에 쌓인 상세와 연락처 추가를 모두 걷어내고 그 상세로 바꾼다") {
            val detailCases =
                listOf(
                    listOf(ContactAddNavKey),
                    listOf(ContactDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()), ContactAddNavKey),
                    listOf(ContactAddNavKey, ContactDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()), ContactDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())),
                )

            detailCases.forEach { detailKeyList ->
                val selectedId = fixtureMonkey.giveMeOne<Uuid>()
                val backStack = contactBackStack(detailKeyList = detailKeyList)

                backStack.navigateToContactDetail(selectedId)

                backStack shouldContainExactly listOf(MoreHomeStubNavKey, ContactHomeNavKey, ContactDetailNavKey(id = selectedId))
            }
        }

        test("상세 영역에 연락처 상세가 없으면 목록에서 선택한 연락처 상세를 이어 둔다") {
            val selectedId = fixtureMonkey.giveMeOne<Uuid>()
            val backStack = NavBackStack<ScreenNavKey>(MoreHomeStubNavKey, ContactHomeNavKey)

            backStack.navigateToContactDetail(selectedId)

            backStack shouldContainExactly listOf(MoreHomeStubNavKey, ContactHomeNavKey, ContactDetailNavKey(id = selectedId))
        }

        test("연락처 목록이 없는 전환 이력에서는 연락처 목록 뒤로가기 동작이 전환 이력을 바꾸지 않는다") {
            val backStack = NavBackStack<ScreenNavKey>(MoreHomeStubNavKey, ContactDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()))
            val expected = backStack.toList()

            backStack.navigateUpFromContactHome()

            backStack shouldContainExactly expected
        }
    })

// 연락처 목록으로 진입하는 `더보기` 화면을 대신한다. `더보기` 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object MoreHomeStubNavKey : ScreenNavKey {
    override val screenName: String
        get() = "MoreHomeStub"
}

private fun contactBackStack(detailKeyList: List<ScreenNavKey>): NavBackStack<ScreenNavKey> =
    NavBackStack(
        MoreHomeStubNavKey,
        ContactHomeNavKey,
        *detailKeyList.toTypedArray(),
    )
