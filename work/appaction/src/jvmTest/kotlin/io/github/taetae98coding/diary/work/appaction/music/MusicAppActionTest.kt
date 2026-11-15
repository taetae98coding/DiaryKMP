package io.github.taetae98coding.diary.work.appaction.music

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.testing.playlist.music
import io.github.taetae98coding.diary.core.testing.playlist.musicDetail
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.domain.playlist.exception.MusicLinkNotYoutubeException
import io.github.taetae98coding.diary.domain.playlist.usecase.AddMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.DeleteMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.FindMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.PageMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.RestoreMusicUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.UpdateMusicUseCase
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.domain.tag.usecase.AddEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RemoveEntityTagUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionDone
import io.github.taetae98coding.diary.work.appaction.core.AppActionException
import io.github.taetae98coding.diary.work.appaction.core.AppActionId
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.tag.AppActionEntityTagKind
import io.github.taetae98coding.diary.work.appaction.tag.entityTagAppActionOf
import io.github.taetae98coding.diary.work.appaction.tag.expected
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class MusicAppActionTest :
    FunSpec({
        test("목록은 정렬을 담지 않으면 제목순으로, 담으면 그 정렬로 조회해 MCP와 같은 항목을 돌려준다") {
            runTest {
                val musicList = Arb.list(Arb.music(), 1..3).next()
                val pageMusicUseCase = mockk<PageMusicUseCase>()
                every { pageMusicUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.from(musicList)))
                val action = musicAppActionOf(pageMusicUseCase)

                action.list(sort = null, cursor = null) shouldBe AppActionPage(items = musicList.map { music -> music.expected() }, nextCursor = null)
                action.list(sort = "recently_updated", cursor = null).items shouldBe musicList.map { music -> music.expected() }

                verify(exactly = 1) { pageMusicUseCase(parameter = ListSort.TITLE) }
                verify(exactly = 1) { pageMusicUseCase(parameter = ListSort.RECENTLY_UPDATED) }
            }
        }

        test("목록이 제공하지 않는 정렬은 고를 수 있는 정렬을 알리며 실패한다") {
            runTest {
                val pageMusicUseCase = mockk<PageMusicUseCase>()
                val action = musicAppActionOf(pageMusicUseCase)

                listOf("default", "name").forEach { sort ->
                    shouldThrow<AppActionException.InvalidArgument> { action.list(sort = sort, cursor = null) }.message shouldBe "'sort' must be one of title, recently_updated."
                }
                verify(exactly = 0) { pageMusicUseCase(parameter = any()) }
            }
        }

        test("목록은 100개씩 나눠 다음 페이지 위치를 알리고, 그 위치로 다음 페이지를 받는다") {
            runTest {
                val musicList = Arb.list(Arb.music(), 101..150).next()
                val pageMusicUseCase = mockk<PageMusicUseCase>()
                every { pageMusicUseCase(parameter = any()) } answers { flowOf(Result.success(PagingData.from(musicList))) }
                val action = musicAppActionOf(pageMusicUseCase)

                val firstPage = action.list(sort = null, cursor = null)
                val secondPage = action.list(sort = null, cursor = firstPage.nextCursor)

                firstPage shouldBe AppActionPage(items = musicList.take(100).map { music -> music.expected() }, nextCursor = "100")
                secondPage shouldBe AppActionPage(items = musicList.drop(100).map { music -> music.expected() }, nextCursor = null)
            }
        }

        test("계정 조회가 실패하면 MCP와 같은 메시지로 실패를 알린다") {
            runTest {
                val message = Arb.nonBlankString().next()
                val pageMusicUseCase = mockk<PageMusicUseCase>()
                every { pageMusicUseCase(parameter = any()) } returns flowOf(Result.failure(IllegalStateException(message)))
                val action = musicAppActionOf(pageMusicUseCase)

                shouldThrow<AppActionException.Failure> { action.list(sort = null, cursor = null) }.message shouldBe "IllegalStateException: $message"
            }
        }

        test("TC-4ca7faaf-f740-4b6a-9c4b-33ac9b779292 곡 하나를 연결된 태그와 함께 돌려주고, 없으면 null을 돌려준다") {
            runTest {
                val music = Arb.music().next().copy(isDeleted = false)
                val tagList = Arb.list(Arb.tag(), 1..3).next()
                val missingId = Arb.kotlinUuid().next()
                val findMusicUseCase = mockk<FindMusicUseCase>()
                val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
                every { findMusicUseCase(parameter = music.id) } returns flowOf(Result.success(music))
                every { findMusicUseCase(parameter = missingId) } returns flowOf(Result.success(null))
                every { getEntityTagUseCase(parameter = GetEntityTagUseCase.Parameter(kind = EntityTagKind.MUSIC, entityId = music.id)) } returns flowOf(Result.success(tagList))
                val action = musicAppActionOf(findMusicUseCase, getEntityTagUseCase)

                action.get(musicId = music.id.toString()) shouldBe AppActionMusicDetail(music = music.expected(), tags = tagList.map { tag -> tag.expected() })
                action.get(musicId = missingId.toString()) shouldBe null
            }
        }

        test("곡 id가 id 형식이 아니면 앱 기능을 실행하지 않고 실패를 알린다") {
            runTest {
                val findMusicUseCase = mockk<FindMusicUseCase>()
                val updateMusicUseCase = mockk<UpdateMusicUseCase>()
                val deleteMusicUseCase = mockk<DeleteMusicUseCase>()
                val restoreMusicUseCase = mockk<RestoreMusicUseCase>()
                val action = musicAppActionOf(findMusicUseCase, updateMusicUseCase, deleteMusicUseCase, restoreMusicUseCase)
                val message = "'musicId' must be an id."

                shouldThrow<AppActionException.InvalidArgument> { action.get(musicId = "not-id") }.message shouldBe message
                shouldThrow<AppActionException.InvalidArgument> { action.update(musicId = "not-id", title = null, artist = null, link = null) }.message shouldBe message
                shouldThrow<AppActionException.InvalidArgument> { action.delete(musicId = "not-id") }.message shouldBe message
                shouldThrow<AppActionException.InvalidArgument> { action.restore(musicId = "not-id") }.message shouldBe message
                verify(exactly = 0) { findMusicUseCase(parameter = any()) }
                coVerify(exactly = 0) { updateMusicUseCase(parameter = any()) }
                coVerify(exactly = 0) { deleteMusicUseCase(parameter = any()) }
                coVerify(exactly = 0) { restoreMusicUseCase(parameter = any()) }
            }
        }

        test("곡을 추가하면 받은 값과 연결 태그 없이 추가하고 추가한 곡의 id를 돌려준다") {
            runTest {
                val detail = Arb.musicDetail().next()
                val addedId = Arb.kotlinUuid().next()
                val addMusicUseCase = mockk<AddMusicUseCase>()
                coEvery { addMusicUseCase(parameter = any()) } returns Result.success(addedId)
                val action = musicAppActionOf(addMusicUseCase)

                action.add(title = detail.title, artist = detail.artist, link = detail.link, tagIds = emptyList()) shouldBe AppActionId(id = addedId.toString())
                coVerify(exactly = 1) { addMusicUseCase(parameter = AddMusicUseCase.Parameter(detail = detail, tagIdSet = emptySet())) }
            }
        }

        test("가수와 링크를 담지 않으면 빈 가수와 링크 없음으로 추가한다") {
            runTest {
                val detail = Arb.musicDetail().next().copy(artist = "", link = "")
                val addMusicUseCase = mockk<AddMusicUseCase>()
                coEvery { addMusicUseCase(parameter = any()) } returns Result.success(Arb.kotlinUuid().next())
                val action = musicAppActionOf(addMusicUseCase)

                action.add(title = detail.title, artist = null, link = null, tagIds = emptyList())

                coVerify(exactly = 1) { addMusicUseCase(parameter = AddMusicUseCase.Parameter(detail = detail, tagIdSet = emptySet())) }
            }
        }

        test("YouTube 링크가 아니어서 앱 기능이 실패하면 MCP와 같은 메시지로 실패를 알린다") {
            runTest {
                val addMusicUseCase = mockk<AddMusicUseCase>()
                coEvery { addMusicUseCase(parameter = any()) } returns Result.failure(MusicLinkNotYoutubeException())
                val action = musicAppActionOf(addMusicUseCase)

                shouldThrow<AppActionException.Failure> { action.add(title = Arb.nonBlankString().next(), artist = null, link = "https://example.com", tagIds = emptyList()) }.message shouldBe
                    "MusicLinkNotYoutubeException"
            }
        }

        test("고칠 값을 담지 않은 항목은 지금 값을 유지하고 고친 수를 돌려준다") {
            runTest {
                val music = Arb.music().next().copy(isDeleted = false)
                val artist = Arb.nonBlankString().next()
                val changedCount = Arb.int(0..1).next()
                val findMusicUseCase = mockk<FindMusicUseCase>()
                val updateMusicUseCase = mockk<UpdateMusicUseCase>()
                every { findMusicUseCase(parameter = music.id) } returns flowOf(Result.success(music))
                coEvery { updateMusicUseCase(parameter = any()) } returns Result.success(changedCount)
                val action = musicAppActionOf(findMusicUseCase, updateMusicUseCase)

                action.update(musicId = music.id.toString(), title = null, artist = artist, link = null) shouldBe AppActionChangedCount(changedCount = changedCount)
                coVerify(exactly = 1) { updateMusicUseCase(parameter = UpdateMusicUseCase.Parameter(id = music.id, detail = music.detail.copy(artist = artist))) }
            }
        }

        test("빈 링크를 담으면 링크를 지운다") {
            runTest {
                val music = Arb.music().next().copy(isDeleted = false)
                val findMusicUseCase = mockk<FindMusicUseCase>()
                val updateMusicUseCase = mockk<UpdateMusicUseCase>()
                every { findMusicUseCase(parameter = music.id) } returns flowOf(Result.success(music))
                coEvery { updateMusicUseCase(parameter = any()) } returns Result.success(1)
                val action = musicAppActionOf(findMusicUseCase, updateMusicUseCase)

                action.update(musicId = music.id.toString(), title = null, artist = null, link = "")

                coVerify(exactly = 1) { updateMusicUseCase(parameter = UpdateMusicUseCase.Parameter(id = music.id, detail = music.detail.copy(link = ""))) }
            }
        }

        test("없는 곡을 고치면 고친 수 0을 돌려준다") {
            runTest {
                val musicId = Arb.kotlinUuid().next()
                val findMusicUseCase = mockk<FindMusicUseCase>()
                val updateMusicUseCase = mockk<UpdateMusicUseCase>()
                every { findMusicUseCase(parameter = musicId) } returns flowOf(Result.success(null))
                val action = musicAppActionOf(findMusicUseCase, updateMusicUseCase)

                action.update(musicId = musicId.toString(), title = Arb.nonBlankString().next(), artist = null, link = null) shouldBe AppActionChangedCount(changedCount = 0)
                coVerify(exactly = 0) { updateMusicUseCase(parameter = any()) }
            }
        }

        test("삭제와 되돌리기는 바뀐 수를 돌려준다") {
            runTest {
                val musicId = Arb.kotlinUuid().next()
                val changedCount = Arb.int(0..1).next()
                val deleteMusicUseCase = mockk<DeleteMusicUseCase>()
                val restoreMusicUseCase = mockk<RestoreMusicUseCase>()
                coEvery { deleteMusicUseCase(parameter = musicId) } returns Result.success(changedCount)
                coEvery { restoreMusicUseCase(parameter = musicId) } returns Result.success(changedCount)
                val action = musicAppActionOf(deleteMusicUseCase, restoreMusicUseCase)

                action.delete(musicId = musicId.toString()) shouldBe AppActionChangedCount(changedCount = changedCount)
                action.restore(musicId = musicId.toString()) shouldBe AppActionChangedCount(changedCount = changedCount)
            }
        }

        test("바꾸는 앱 기능이 실패하면 MCP와 같은 메시지로 실패를 알린다") {
            runTest {
                val message = Arb.nonBlankString().next()
                val deleteMusicUseCase = mockk<DeleteMusicUseCase>()
                coEvery { deleteMusicUseCase(parameter = any()) } returns Result.failure(IllegalStateException(message))
                val action = musicAppActionOf(deleteMusicUseCase)

                shouldThrow<AppActionException.Failure> { action.delete(musicId = Arb.kotlinUuid().next().toString()) }.message shouldBe "IllegalStateException: $message"
            }
        }

        test("TC-e17787d4-7a35-4675-8dbd-0b61b2795a73 삭제된 곡의 상세는 없는 곡과 같이 null을 돌려준다") {
            runTest {
                val music = Arb.music().next().copy(isDeleted = true)
                val findMusicUseCase = mockk<FindMusicUseCase>()
                val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
                every { findMusicUseCase(parameter = music.id) } returns flowOf(Result.success(music))
                val action = musicAppActionOf(findMusicUseCase, getEntityTagUseCase)

                action.get(musicId = music.id.toString()) shouldBe null
                verify(exactly = 0) { getEntityTagUseCase(parameter = any()) }
            }
        }

        test("TC-edf2f510-aac0-4a31-afdb-2f9368763906 추가 요청에 담은 태그를 함께 연결한다") {
            runTest {
                val tagIdSet = setOf(Arb.kotlinUuid().next(), Arb.kotlinUuid().next())
                val addedId = Arb.kotlinUuid().next()
                val parameter = slot<AddMusicUseCase.Parameter>()
                val addUseCase = mockk<AddMusicUseCase>()
                coEvery { addUseCase(parameter = capture(parameter)) } returns Result.success(addedId)
                val action = musicAppActionOf(addUseCase)

                action.add(title = Arb.nonBlankString().next(), artist = null, link = null, tagIds = tagIdSet.map { tagId -> tagId.toString() }) shouldBe AppActionId(id = addedId.toString())
                parameter.captured.tagIdSet shouldBe tagIdSet
            }
        }

        test("추가 요청의 태그 id가 id 형식이 아니면 추가하지 않고 MCP와 같은 메시지로 실패한다") {
            runTest {
                val addUseCase = mockk<AddMusicUseCase>()
                val action = musicAppActionOf(addUseCase)

                shouldThrow<AppActionException.InvalidArgument> { action.add(title = Arb.nonBlankString().next(), artist = null, link = null, tagIds = listOf("tag")) }.message shouldBe "'tagIds' must be an id."
                coVerify(exactly = 0) { addUseCase(parameter = any()) }
            }
        }

        test("TC-173de82e-308b-45d4-bc9b-14a00eb1c2fc 태그 연결과 해제는 곡 종류로 요청하고 완료를 돌려준다") {
            runTest {
                val musicId = Arb.kotlinUuid().next()
                val tagId = Arb.kotlinUuid().next()
                val addEntityTagUseCase = mockk<AddEntityTagUseCase>()
                val removeEntityTagUseCase = mockk<RemoveEntityTagUseCase>()
                coEvery { addEntityTagUseCase(parameter = any()) } returns Result.success(Unit)
                coEvery { removeEntityTagUseCase(parameter = any()) } returns Result.success(Unit)
                val action = entityTagAppActionOf(addEntityTagUseCase, removeEntityTagUseCase)

                action.add(kind = AppActionEntityTagKind.MUSIC, entityId = musicId.toString(), tagId = tagId.toString()) shouldBe AppActionDone(done = true)
                action.remove(kind = AppActionEntityTagKind.MUSIC, entityId = musicId.toString(), tagId = tagId.toString()) shouldBe AppActionDone(done = true)

                coVerify(exactly = 1) { addEntityTagUseCase(parameter = AddEntityTagUseCase.Parameter(kind = EntityTagKind.MUSIC, entityId = musicId, tagId = tagId)) }
                coVerify(exactly = 1) { removeEntityTagUseCase(parameter = RemoveEntityTagUseCase.Parameter(kind = EntityTagKind.MUSIC, entityId = musicId, tagId = tagId)) }
            }
        }

        test("태그 연결의 id가 id 형식이 아니면 연결하지 않고, 연결이 실패하면 MCP와 같은 메시지로 실패한다") {
            runTest {
                val message = Arb.nonBlankString().next()
                val addEntityTagUseCase = mockk<AddEntityTagUseCase>()
                val removeEntityTagUseCase = mockk<RemoveEntityTagUseCase>()
                coEvery { removeEntityTagUseCase(parameter = any()) } returns Result.failure(IllegalStateException(message))
                val action = entityTagAppActionOf(addEntityTagUseCase, removeEntityTagUseCase)

                shouldThrow<AppActionException.InvalidArgument> { action.add(kind = AppActionEntityTagKind.MUSIC, entityId = "not-id", tagId = Arb.kotlinUuid().next().toString()) }.message shouldBe "'musicId' must be an id."
                shouldThrow<AppActionException.InvalidArgument> { action.add(kind = AppActionEntityTagKind.MUSIC, entityId = Arb.kotlinUuid().next().toString(), tagId = "tag") }.message shouldBe "'tagId' must be an id."
                shouldThrow<AppActionException.Failure> {
                    action.remove(kind = AppActionEntityTagKind.MUSIC, entityId = Arb.kotlinUuid().next().toString(), tagId = Arb.kotlinUuid().next().toString())
                }.message shouldBe "IllegalStateException: $message"
                coVerify(exactly = 0) { addEntityTagUseCase(parameter = any()) }
            }
        }

        test("삭제된 곡도 포함하는 조회는 삭제된 곡를 돌려준다") {
            runTest {
                val music = Arb.music().next().copy(isDeleted = true)
                val findMusicUseCase = mockk<FindMusicUseCase>()
                val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
                every { findMusicUseCase(parameter = music.id) } returns flowOf(Result.success(music))
                every { getEntityTagUseCase(parameter = GetEntityTagUseCase.Parameter(kind = EntityTagKind.MUSIC, entityId = music.id)) } returns flowOf(Result.success(emptyList()))
                val action = musicAppActionOf(findMusicUseCase, getEntityTagUseCase)

                action.getIncludingDeleted(musicId = music.id.toString())?.music?.id shouldBe music.id.toString()
            }
        }
    })

private fun Music.expected(): AppActionMusic =
    AppActionMusic(
        id = id.toString(),
        title = detail.title,
        artist = detail.artist,
        link = detail.link,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )
