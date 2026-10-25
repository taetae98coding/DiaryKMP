package io.github.taetae98coding.diary.domain.playlist.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.playlist.exception.MusicLinkNotYoutubeException
import io.github.taetae98coding.diary.domain.playlist.exception.MusicTitleBlankException
import io.github.taetae98coding.diary.domain.playlist.repository.AccountMusicRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AddMusicUseCaseTest :
    BehaviorSpec({
        listOf(
            "" to "빈 제목",
            "   " to "공백 문자로만 이루어진 제목",
        ).forEach { (blankTitle, label) ->
            Given("$label 이 입력되어 있다") {
                val getAccountUseCase = mockk<GetAccountUseCase>()
                val accountMusicRepository = mockk<AccountMusicRepository>(relaxed = true)
                val useCase =
                    AddMusicUseCase(
                        getAccountUseCase = getAccountUseCase,
                        requestSyncUseCase = requestSyncUseCase(),
                        accountMusicRepository = accountMusicRepository,
                        clock = Clock.System,
                    )

                When("곡을 추가한다") {
                    Then("TC-84c0c12a-534d-4285-83bb-863412699814 제목 공백 예외로 실패하고 곡을 저장하지 않는다") {
                        val result = useCase(parameter = detail(title = blankTitle))

                        result.shouldBeFailure().shouldBeInstanceOf<MusicTitleBlankException>()
                        verify(exactly = 0) { getAccountUseCase(parameter = Unit) }
                        coVerify(exactly = 0) { accountMusicRepository.upsert(account = any(), music = any()) }
                    }

                    Then("TC-d432b763-204e-427e-b000-84da4263bb40 링크 형식도 성립하지 않으면 제목 공백 예외를 먼저 알린다") {
                        val result = useCase(parameter = detail(title = blankTitle, link = "https://vimeo.com/76979871"))

                        result.shouldBeFailure().shouldBeInstanceOf<MusicTitleBlankException>()
                        coVerify(exactly = 0) { accountMusicRepository.upsert(account = any(), music = any()) }
                    }
                }
            }
        }

        Given("YouTube 주소가 아닌 링크가 입력되어 있다") {
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountMusicRepository = mockk<AccountMusicRepository>(relaxed = true)
            val useCase =
                AddMusicUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    accountMusicRepository = accountMusicRepository,
                    clock = Clock.System,
                )

            When("공백이 아닌 제목으로 곡을 추가한다") {
                listOf(
                    "https://vimeo.com/76979871",
                    "https://notyoutube.com/watch?v=dQw4w9WgXcQ",
                    "https://youtube.com.attacker.example/watch?v=dQw4w9WgXcQ",
                    "www.youtube.com/watch?v=dQw4w9WgXcQ",
                    "ftp://www.youtube.com/watch?v=dQw4w9WgXcQ",
                    "곡 링크",
                ).forEach { link ->
                    Then("TC-84c0c12a-534d-4285-83bb-863412699814 링크 형식 예외로 실패하고 곡을 저장하지 않는다: $link") {
                        val result = useCase(parameter = detail(link = link))

                        result.shouldBeFailure().shouldBeInstanceOf<MusicLinkNotYoutubeException>()
                        verify(exactly = 0) { getAccountUseCase(parameter = Unit) }
                        coVerify(exactly = 0) { accountMusicRepository.upsert(account = any(), music = any()) }
                    }
                }
            }
        }

        Given("현재 계정이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val musicSlot = slot<Music>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountMusicRepository = mockk<AccountMusicRepository>()
            coEvery { accountMusicRepository.upsert(account = account, music = capture(musicSlot)) } just Runs
            val now = fixtureMonkey.giveMeOne<Instant>()
            val clock = mockk<Clock>()
            every { clock.now() } returns now
            val useCase =
                AddMusicUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    accountMusicRepository = accountMusicRepository,
                    clock = clock,
                )

            When("공백이 아닌 제목으로 곡을 추가한다") {
                Then("TC-6da37a86-d056-4e06-b84a-cb32a64209c8 TC-1cc51dde-b6f5-405f-849d-72eca6c3abc9 현재 계정과 연결된 고유한 곡으로 저장한다") {
                    val firstId = useCase(parameter = detail()).shouldBeSuccess()
                    val secondId = useCase(parameter = detail()).shouldBeSuccess()

                    firstId shouldNotBe Uuid.NIL
                    secondId shouldNotBe Uuid.NIL
                    firstId shouldNotBe secondId
                    coVerify(exactly = 2) { accountMusicRepository.upsert(account = account, music = any()) }
                }

                Then("TC-4119bccc-4052-4b36-877c-9f1a244fd018 제목, 가수와 링크가 같아도 서로 다른 곡으로 저장한다") {
                    val sameDetail = detail()

                    val firstId = useCase(parameter = sameDetail).shouldBeSuccess()
                    val secondId = useCase(parameter = sameDetail).shouldBeSuccess()

                    firstId shouldNotBe secondId
                }

                Then("TC-f0597753-436f-4f25-907b-21e0dd93d4d2 입력한 제목과 가수를 앞뒤 공백까지 그대로 저장한다") {
                    val expected = detail(title = "  곡 제목  ", artist = "  가수  ")

                    val result = useCase(parameter = expected)

                    result.shouldBeSuccess(musicSlot.captured.id)
                    musicSlot.captured.detail shouldBe expected
                }

                listOf(
                    "" to "빈 가수",
                    "   " to "공백 문자로만 이루어진 가수",
                ).forEach { (artist, label) ->
                    Then("TC-a542a241-f434-4e75-ac7d-aae829e16d89 TC-98531eb1-f6cd-4691-a115-f61ad55c3b3d $label 로도 곡을 저장하고 입력한 값을 그대로 기록한다") {
                        useCase(parameter = detail(artist = artist)).shouldBeSuccess()

                        musicSlot.captured.detail.artist shouldBe artist
                    }
                }

                listOf(
                    "" to "빈 링크",
                    "   " to "공백 문자로만 이루어진 링크",
                ).forEach { (link, label) ->
                    Then("TC-92cae7df-dc95-4282-8028-69137fc4f223 TC-98531eb1-f6cd-4691-a115-f61ad55c3b3d $label 로도 곡을 저장하고 링크를 빈 값으로 기록한다") {
                        useCase(parameter = detail(link = link)).shouldBeSuccess()

                        musicSlot.captured.detail.link shouldBe ""
                    }
                }

                Then("TC-98531eb1-f6cd-4691-a115-f61ad55c3b3d 가수와 링크가 모두 비어 있어도 곡을 저장한다") {
                    useCase(parameter = detail(artist = "", link = "")).shouldBeSuccess()

                    musicSlot.captured.detail.artist shouldBe ""
                    musicSlot.captured.detail.link shouldBe ""
                }

                Then("TC-7eded492-d855-445f-b3c9-e16bd41d208f 링크의 앞뒤 공백을 없앤 값을 저장한다") {
                    useCase(parameter = detail(link = "  $YOUTUBE_LINK  ")).shouldBeSuccess()

                    musicSlot.captured.detail.link shouldBe YOUTUBE_LINK
                }

                listOf(
                    "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                    "https://youtu.be/dQw4w9WgXcQ",
                    "https://m.youtube.com/watch?v=dQw4w9WgXcQ",
                    "https://music.youtube.com/watch?v=dQw4w9WgXcQ",
                    "https://WWW.YouTube.COM/watch?v=dQw4w9WgXcQ",
                    "https://www.youtube.com/watch?v=dQw4w9WgXcQ&list=PLabc&t=42s",
                    "https://www.youtube.com/shorts/dQw4w9WgXcQ",
                    "http://www.youtube.com/watch?v=dQw4w9WgXcQ",
                    "https://www.youtube.com/@channel",
                ).forEach { link ->
                    Then("TC-48f834e5-bf64-46f8-a82a-965b9eb865cb YouTube 주소를 곡의 링크로 저장한다: $link") {
                        useCase(parameter = detail(link = link)).shouldBeSuccess()

                        musicSlot.captured.detail.link shouldBe link
                    }
                }

                Then("TC-2a643d2a-9044-4be0-b647-f5d008f01fee 미삭제 상태와 추가 시각을 저장한다") {
                    useCase(parameter = detail()).shouldBeSuccess()

                    musicSlot.captured.isDeleted shouldBe false
                    musicSlot.captured.createdAt shouldBe now
                    musicSlot.captured.updatedAt shouldBe now
                }
            }
        }

        Given("곡을 추가할 수 있는 현재 계정이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountMusicRepository = mockk<AccountMusicRepository>(relaxed = true)
            val requestSyncUseCase = requestSyncUseCase()
            val useCase =
                AddMusicUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMusicRepository = accountMusicRepository,
                    clock = Clock.System,
                )

            When("곡 추가에 성공한다") {
                Then("TC-541e8b7e-a72b-4768-ae1e-8a46e49afb9c TC-acdaeb38-f121-43e5-aa28-9ca82631e309 추가한 곡을 서버와 맞추기 위한 동기화를 요청한다") {
                    useCase(parameter = detail()).shouldBeSuccess()

                    coVerify(exactly = 1) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }

        Given("곡 저장은 성공하지만 서버 반영이 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountMusicRepository = mockk<AccountMusicRepository>(relaxed = true)
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns
                Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
            val useCase =
                AddMusicUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMusicRepository = accountMusicRepository,
                    clock = Clock.System,
                )

            When("공백이 아닌 제목으로 곡을 추가한다") {
                Then("TC-5cbc1b1e-a875-448a-910f-d4d9d32590ee 추가는 성공으로 전달되고 저장한 곡을 되돌리지 않는다") {
                    val musicSlot = slot<Music>()
                    coEvery { accountMusicRepository.upsert(account = account, music = capture(musicSlot)) } just Runs

                    useCase(parameter = detail()).shouldBeSuccess()

                    coVerify(exactly = 1) { accountMusicRepository.upsert(account = account, music = any()) }
                    musicSlot.captured.isDeleted.shouldBeFalse()
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val accountMusicRepository = mockk<AccountMusicRepository>(relaxed = true)
            val useCase =
                AddMusicUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    accountMusicRepository = accountMusicRepository,
                    clock = Clock.System,
                )

            When("공백이 아닌 제목으로 곡을 추가한다") {
                Then("TC-2f52d42b-9036-4965-b86e-b982acb09c9f 계정 조회 실패를 전달하고 곡을 저장하지 않는다") {
                    val result = useCase(parameter = detail())

                    result.shouldBeFailure() shouldBeSameInstanceAs throwable
                    coVerify(exactly = 0) { accountMusicRepository.upsert(account = any(), music = any()) }
                }
            }
        }

        Given("곡 저장이 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountMusicRepository = mockk<AccountMusicRepository>()
            coEvery { accountMusicRepository.upsert(account = account, music = any()) } throws throwable
            val useCase =
                AddMusicUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    accountMusicRepository = accountMusicRepository,
                    clock = Clock.System,
                )

            When("공백이 아닌 제목으로 곡을 추가한다") {
                Then("TC-9ff7cfb6-4805-4bd7-b69a-f1f1539d8486 추가를 성공으로 다루지 않고 저장 실패를 전달한다") {
                    val result = useCase(parameter = detail())

                    result.shouldBeFailure() shouldBeSameInstanceAs throwable
                }
            }
        }
    }) {
    public companion object {
        private const val YOUTUBE_LINK: String = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun requestSyncUseCase(): RequestSyncUseCase = mockk(relaxed = true)

        private fun detail(
            title: String = "title-${fixtureMonkey.giveMeOne<String>()}",
            artist: String = "artist-${fixtureMonkey.giveMeOne<String>()}",
            link: String = YOUTUBE_LINK,
        ): MusicDetail =
            MusicDetail(
                title = title,
                artist = artist,
                link = link,
            )
    }
}
