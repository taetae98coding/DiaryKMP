package io.github.taetae98coding.diary.data.file.paging

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.testing.TestPager
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.network.api.file.datasource.FileRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.file.entity.FileCursorRemoteEntity
import io.github.taetae98coding.diary.core.network.api.file.entity.FileRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

private const val PAGE_SIZE = 20

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

private val pagingConfig: PagingConfig =
    PagingConfig(pageSize = PAGE_SIZE, initialLoadSize = PAGE_SIZE, enablePlaceholders = false)

class FilePagingSourceTest :
    FunSpec({
        test("이어서 불러올 때 받은 마지막 파일의 올린 시각과 식별자를 기준으로 불러올 개수만큼 요청한다") {
            val firstPage = List(PAGE_SIZE) { remoteFile() }
            val secondPage = List(PAGE_SIZE) { remoteFile() }
            val fileRemoteDataSource = mockk<FileRemoteDataSource>()
            coEvery { fileRemoteDataSource.fetch(cursor = null, size = PAGE_SIZE) } returns firstPage
            coEvery { fileRemoteDataSource.fetch(cursor = firstPage.last().cursor(), size = PAGE_SIZE) } returns secondPage
            val pager = TestPager(config = pagingConfig, pagingSource = FilePagingSource(fileRemoteDataSource = fileRemoteDataSource))

            pager.refresh()
            val append = pager.append().shouldBeInstanceOf<PagingSource.LoadResult.Page<FileCursorRemoteEntity, *>>()

            append.data.size shouldBe PAGE_SIZE
            coVerify(exactly = 1) { fileRemoteDataSource.fetch(cursor = firstPage.last().cursor(), size = PAGE_SIZE) }
        }

        test("TC-FILE-STORAGE-DATA-003 돌려받은 파일이 20개면 이어서 요청하고 20개보다 적으면 더 요청하지 않는다") {
            mapOf(
                PAGE_SIZE to true,
                PAGE_SIZE - 1 to false,
                0 to false,
            ).forEach { (count, hasNext) ->
                val firstPage = List(count) { remoteFile() }
                val fileRemoteDataSource = mockk<FileRemoteDataSource>()
                coEvery { fileRemoteDataSource.fetch(cursor = null, size = PAGE_SIZE) } returns firstPage
                coEvery { fileRemoteDataSource.fetch(cursor = match { cursor -> cursor != null }, size = PAGE_SIZE) } returns emptyList()
                val pager = TestPager(config = pagingConfig, pagingSource = FilePagingSource(fileRemoteDataSource = fileRemoteDataSource))

                val refresh = pager.refresh().shouldBeInstanceOf<PagingSource.LoadResult.Page<FileCursorRemoteEntity, *>>()
                pager.append()

                if (hasNext) {
                    refresh.nextKey shouldBe firstPage.last().cursor()
                    coVerify(exactly = 2) { fileRemoteDataSource.fetch(cursor = any(), size = any()) }
                } else {
                    refresh.nextKey.shouldBeNull()
                    coVerify(exactly = 1) { fileRemoteDataSource.fetch(cursor = any(), size = any()) }
                }
            }
        }

        test("TC-FILE-STORAGE-DATA-004 이어서 불러오기에 실패해도 이미 불러온 파일은 남고 스스로 다시 요청하지 않는다") {
            val firstPage = List(PAGE_SIZE) { remoteFile() }
            val fileRemoteDataSource = mockk<FileRemoteDataSource>()
            coEvery { fileRemoteDataSource.fetch(cursor = null, size = PAGE_SIZE) } returns firstPage
            coEvery { fileRemoteDataSource.fetch(cursor = firstPage.last().cursor(), size = PAGE_SIZE) } throws IllegalStateException("fetch")
            val pager = TestPager(config = pagingConfig, pagingSource = FilePagingSource(fileRemoteDataSource = fileRemoteDataSource))

            pager.refresh()
            pager.append().shouldBeInstanceOf<PagingSource.LoadResult.Error<FileCursorRemoteEntity, *>>()

            pager.getPages().flatMap { page -> page.data }.size shouldBe PAGE_SIZE
            coVerify(exactly = 1) { fileRemoteDataSource.fetch(cursor = firstPage.last().cursor(), size = PAGE_SIZE) }
        }

        test("다시 불러오면 보던 자리와 관계없이 처음부터 불러온다") {
            val fileRemoteDataSource = mockk<FileRemoteDataSource>()
            coEvery { fileRemoteDataSource.fetch(cursor = null, size = PAGE_SIZE) } returns List(PAGE_SIZE) { remoteFile() }
            val pager = TestPager(config = pagingConfig, pagingSource = FilePagingSource(fileRemoteDataSource = fileRemoteDataSource))

            pager.refresh()

            pager.getLastLoadedPage().shouldBeInstanceOf<PagingSource.LoadResult.Page<*, *>>()
            FilePagingSource(fileRemoteDataSource = fileRemoteDataSource).getRefreshKey(state = pager.getPagingState(anchorPosition = PAGE_SIZE - 1)).shouldBeNull()
        }
    }) {
    public companion object {
        private fun remoteFile(): FileRemoteEntity = fixtureMonkey.giveMeOne<FileRemoteEntity>()

        private fun FileRemoteEntity.cursor(): FileCursorRemoteEntity = FileCursorRemoteEntity(createdAt = createdAt, id = id)
    }
}
