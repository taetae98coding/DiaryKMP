package io.github.taetae98coding.diary.domain.playlist.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.playlist.exception.MusicLinkNotYoutubeException
import io.github.taetae98coding.diary.domain.playlist.repository.AccountMusicRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class UpdateMusicUseCaseTest :
    BehaviorSpec({
        Given("저장된 곡과 현재 계정이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val stored = music()
            val detailSlot = slot<MusicDetail>()
            val updatedAtSlot = slot<Instant>()
            val accountMusicRepository = mockk<AccountMusicRepository>()
            coEvery {
                accountMusicRepository.updateDetail(
                    account = account,
                    musicId = stored.id,
                    detail = capture(detailSlot),
                    updatedAt = capture(updatedAtSlot),
                )
            } returns 1
            val now = instant()
            val requestSyncUseCase = requestSyncUseCase()
            val useCase =
                useCase(
                    account = account,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMusicRepository = accountMusicRepository,
                    stored = stored,
                    now = now,
                )

            When("공백이 아닌 링크와 제목과 가수로 수정한다") {
                Then("TC-MUSIC-DETAIL-DATA-003 입력한 내용과 수정 시점을 반영한다") {
                    val detail = detail(title = "new-title", artist = "new-artist")

                    useCase(parameter = UpdateMusicUseCase.Parameter(id = stored.id, detail = detail)).shouldBeSuccess(1)

                    detailSlot.captured shouldBe detail
                    updatedAtSlot.captured shouldBe now
                }

                Then("TC-MUSIC-DETAIL-DOMAIN-005 링크의 앞뒤 공백만 없애고 제목과 가수는 그대로 기록한다") {
                    val detail = detail(link = "  $YOUTUBE_LINK  ", title = "  곡 제목  ", artist = "  가수  ")

                    useCase(parameter = UpdateMusicUseCase.Parameter(id = stored.id, detail = detail)).shouldBeSuccess(1)

                    detailSlot.captured.link shouldBe YOUTUBE_LINK
                    detailSlot.captured.title shouldBe "  곡 제목  "
                    detailSlot.captured.artist shouldBe "  가수  "
                }

                Then("TC-MUSIC-DETAIL-DOMAIN-006 삭제 여부와 생성 시각과 계정 연결을 바꾸는 저장을 하지 않는다") {
                    useCase(parameter = UpdateMusicUseCase.Parameter(id = stored.id, detail = detail())).shouldBeSuccess(1)

                    coVerify(exactly = 0) {
                        accountMusicRepository.updateDeleted(account = any(), musicId = any(), isDeleted = any(), updatedAt = any())
                    }
                    coVerify(exactly = 0) { accountMusicRepository.upsert(account = any(), music = any()) }
                }

                Then("TC-MUSIC-DETAIL-DATA-007 로컬 저장 결과로 성공을 판단하고 동기화를 요청한다") {
                    useCase(parameter = UpdateMusicUseCase.Parameter(id = stored.id, detail = detail())).shouldBeSuccess(1)

                    coVerify(atLeast = 1) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }

            When("링크나 제목이나 가수를 비운 채 수정한다") {
                listOf(
                    detail(link = "") to "빈 링크",
                    detail(link = "   ") to "공백 문자로만 이루어진 링크",
                    detail(title = "") to "빈 제목",
                    detail(title = "   ") to "공백 문자로만 이루어진 제목",
                    detail(artist = "") to "빈 가수",
                    detail(artist = "   ") to "공백 문자로만 이루어진 가수",
                    detail(link = "   ", title = "   ", artist = "   ") to "모두 공백",
                ).forEach { (detail, label) ->
                    Then("TC-MUSIC-DETAIL-DOMAIN-002 비운 값만 저장된 기존 값으로 채워 반영한다: $label") {
                        useCase(parameter = UpdateMusicUseCase.Parameter(id = stored.id, detail = detail)).shouldBeSuccess(1)

                        detailSlot.captured.link shouldBe detail.link.trim().ifEmpty { stored.detail.link }
                        detailSlot.captured.title shouldBe detail.title.ifBlank { stored.detail.title }
                        detailSlot.captured.artist shouldBe detail.artist.ifBlank { stored.detail.artist }
                    }
                }
            }
        }

        Given("링크가 비어 있는 곡이 저장되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val stored = music(link = "")
            val detailSlot = slot<MusicDetail>()
            val accountMusicRepository = mockk<AccountMusicRepository>()
            coEvery {
                accountMusicRepository.updateDetail(account = account, musicId = stored.id, detail = capture(detailSlot), updatedAt = any())
            } returns 1
            val useCase =
                useCase(
                    account = account,
                    requestSyncUseCase = requestSyncUseCase(),
                    accountMusicRepository = accountMusicRepository,
                    stored = stored,
                )

            When("링크를 비운 채 새 제목과 가수로 수정한다") {
                Then("TC-MUSIC-DETAIL-DOMAIN-004 제목과 가수만 반영하고 링크는 비어 있는 채로 둔다") {
                    val detail = detail(link = "", title = "new-title", artist = "new-artist")

                    useCase(parameter = UpdateMusicUseCase.Parameter(id = stored.id, detail = detail)).shouldBeSuccess(1)

                    detailSlot.captured.link shouldBe ""
                    detailSlot.captured.title shouldBe "new-title"
                    detailSlot.captured.artist shouldBe "new-artist"
                }
            }
        }

        Given("YouTube 주소가 아닌 링크로 수정하려 한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val stored = music()
            val accountMusicRepository = mockk<AccountMusicRepository>(relaxed = true)
            val requestSyncUseCase = requestSyncUseCase()
            val useCase =
                useCase(
                    account = account,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMusicRepository = accountMusicRepository,
                    stored = stored,
                )

            When("수정을 실행한다") {
                listOf(
                    "https://vimeo.com/76979871",
                    "https://youtube.com.attacker.example/watch?v=dQw4w9WgXcQ",
                    "ftp://www.youtube.com/watch?v=dQw4w9WgXcQ",
                    "곡 링크",
                ).forEach { link ->
                    Then("TC-MUSIC-DETAIL-DOMAIN-003 링크 형식 예외로 실패하고 아무 내용도 수정하지 않는다: $link") {
                        val result =
                            useCase(
                                parameter =
                                    UpdateMusicUseCase.Parameter(
                                        id = stored.id,
                                        detail = detail(link = link, title = "new-title", artist = "new-artist"),
                                    ),
                            )

                        result.shouldBeFailure().shouldBeInstanceOf<MusicLinkNotYoutubeException>()
                        coVerify(exactly = 0) {
                            accountMusicRepository.updateDetail(account = any(), musicId = any(), detail = any(), updatedAt = any())
                        }
                        coVerify(exactly = 0) { requestSyncUseCase(parameter = any()) }
                    }
                }
            }
        }

        Given("대상 곡이 없도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val musicId = Uuid.random()
            val accountMusicRepository = mockk<AccountMusicRepository>()
            coEvery {
                accountMusicRepository.updateDetail(account = account, musicId = musicId, detail = any(), updatedAt = any())
            } returns 0
            val useCase =
                useCase(
                    account = account,
                    requestSyncUseCase = requestSyncUseCase(),
                    accountMusicRepository = accountMusicRepository,
                    stored = null,
                )

            When("수정을 실행한다") {
                Then("TC-MUSIC-DETAIL-DATA-004 아무것도 바꾸지 않는다") {
                    useCase(parameter = UpdateMusicUseCase.Parameter(id = musicId, detail = detail())).shouldBeSuccess(0)
                }
            }
        }

        Given("수정 저장이 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val stored = music()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val accountMusicRepository = mockk<AccountMusicRepository>()
            coEvery {
                accountMusicRepository.updateDetail(account = account, musicId = any(), detail = any(), updatedAt = any())
            } throws throwable
            val requestSyncUseCase = requestSyncUseCase()
            val useCase =
                useCase(
                    account = account,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMusicRepository = accountMusicRepository,
                    stored = stored,
                )

            When("수정을 실행한다") {
                Then("TC-MUSIC-DETAIL-DATA-006 실패를 그대로 전달하고 동기화를 요청하지 않는다") {
                    useCase(parameter = UpdateMusicUseCase.Parameter(id = stored.id, detail = detail()))
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)

                    coVerify(exactly = 0) { requestSyncUseCase(parameter = any()) }
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val accountMusicRepository = mockk<AccountMusicRepository>(relaxed = true)
            val useCase =
                UpdateMusicUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase(),
                    findMusicUseCase = findMusicUseCase(stored = null),
                    accountMusicRepository = accountMusicRepository,
                    clock = Clock.System,
                )

            When("수정을 실행한다") {
                Then("계정 조회 실패를 전달하고 수정을 저장하지 않는다") {
                    useCase(parameter = UpdateMusicUseCase.Parameter(id = Uuid.random(), detail = detail()))
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)

                    coVerify(exactly = 0) {
                        accountMusicRepository.updateDetail(account = any(), musicId = any(), detail = any(), updatedAt = any())
                    }
                }
            }
        }
    }) {
    public companion object {
        private const val YOUTUBE_LINK: String = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun useCase(
            account: Account,
            requestSyncUseCase: RequestSyncUseCase,
            accountMusicRepository: AccountMusicRepository,
            stored: Music?,
            now: Instant? = null,
        ): UpdateMusicUseCase {
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val clock =
                if (now == null) {
                    Clock.System
                } else {
                    mockk<Clock>().also { clock -> every { clock.now() } returns now }
                }

            return UpdateMusicUseCase(
                getAccountUseCase = getAccountUseCase,
                requestSyncUseCase = requestSyncUseCase,
                findMusicUseCase = findMusicUseCase(stored = stored),
                accountMusicRepository = accountMusicRepository,
                clock = clock,
            )
        }

        private fun findMusicUseCase(stored: Music?): FindMusicUseCase {
            val useCase = mockk<FindMusicUseCase>()
            every { useCase(parameter = any()) } returns flowOf(Result.success(stored))

            return useCase
        }

        private fun requestSyncUseCase(): RequestSyncUseCase {
            val useCase = mockk<RequestSyncUseCase>()
            coEvery { useCase(parameter = any()) } returns Result.success(Unit)

            return useCase
        }

        private fun music(link: String = YOUTUBE_LINK): Music =
            fixtureMonkey
                .giveMeKotlinBuilder<Music>()
                .setExp(Music::detail, storedDetail(link = link))
                .setExp(Music::updatedAt, instant())
                .setExp(Music::createdAt, instant())
                .sample()

        private fun storedDetail(link: String): MusicDetail =
            MusicDetail(
                link = link,
                title = "stored-title-${fixtureMonkey.giveMeOne<String>()}",
                artist = "stored-artist-${fixtureMonkey.giveMeOne<String>()}",
                thumbnail = "https://i.ytimg.com/vi/${fixtureMonkey.giveMeOne<String>()}/hqdefault.jpg",
            )

        private fun detail(
            link: String = YOUTUBE_LINK,
            title: String = "title-${fixtureMonkey.giveMeOne<String>()}",
            artist: String = "artist-${fixtureMonkey.giveMeOne<String>()}",
            thumbnail: String = "",
        ): MusicDetail =
            MusicDetail(
                link = link,
                title = title,
                artist = artist,
                thumbnail = thumbnail,
            )

        private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
    }
}
