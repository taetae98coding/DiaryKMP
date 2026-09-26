package io.github.taetae98coding.diary.app.shared

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest

class AccountSyncTriggerTest :
    FunSpec({
        test("TC-SYNC-REFRESH-FEATURE-003 인증된 사용자 계정을 처음 확인하면 진행을 표시하는 계정 확인 계기가 된다") {
            runTest {
                val pendingAccount = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = false, isSessionPending = true)
                val confirmedAccount = pendingAccount.copy(isSessionValid = true, isSessionPending = false)

                flowOf<Account>(pendingAccount, confirmedAccount).toSyncTrigger().toList() shouldBe
                    listOf(SyncTrigger.ACCOUNT_CONFIRMED, SyncTrigger.ACCOUNT_CONFIRMED)
            }
        }

        test("TC-SYNC-REFRESH-FEATURE-003 확인된 계정이 다른 계정으로 바뀌면 진행을 표시하는 계정 확인 계기가 된다") {
            runTest {
                val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
                val otherAccount = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)

                flowOf<Account>(account, otherAccount).toSyncTrigger().toList() shouldBe
                    listOf(SyncTrigger.ACCOUNT_CONFIRMED, SyncTrigger.ACCOUNT_CONFIRMED)
            }
        }

        test("TC-SYNC-REFRESH-FEATURE-003 게스트 상태에서 로그인하면 진행을 표시하는 계정 확인 계기가 된다") {
            runTest {
                val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)

                flowOf(account, Account.Guest, account).toSyncTrigger().toList() shouldBe
                    listOf(SyncTrigger.ACCOUNT_CONFIRMED, SyncTrigger.ACCOUNT_CONFIRMED, SyncTrigger.ACCOUNT_CONFIRMED)
            }
        }

        test("TC-SYNC-REFRESH-FEATURE-003 새로 확인을 시작하면 같은 계정도 처음 확인으로 다시 센다") {
            runTest {
                val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true)
                val accountFlow = flowOf<Account>(account).toSyncTrigger()

                accountFlow.toList() shouldBe listOf(SyncTrigger.ACCOUNT_CONFIRMED)
                accountFlow.toList() shouldBe listOf(SyncTrigger.ACCOUNT_CONFIRMED)
            }
        }

        test("TC-SYNC-REFRESH-FEATURE-012 같은 계정의 정보만 바뀌면 진행을 표시하지 않는 계정 갱신 계기가 된다") {
            runTest {
                val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = true, isSessionPending = false)
                val changeList: List<List<Account.User>> =
                    listOf(
                        listOf(account.copy(email = "changed-${account.email}")),
                        listOf(account.copy(profileImage = "changed-${account.profileImage}")),
                        listOf(account.copy(isSessionValid = false), account),
                    )

                changeList.forEach { changedAccountList ->
                    flowOf<Account>(account, *changedAccountList.toTypedArray()).toSyncTrigger().toList() shouldBe
                        listOf(SyncTrigger.ACCOUNT_CONFIRMED) + changedAccountList.map { SyncTrigger.ACCOUNT_UPDATED }
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
