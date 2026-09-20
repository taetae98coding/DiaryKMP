package io.github.taetae98coding.diary.data.account.repository

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.file.api.FileReader
import io.github.taetae98coding.diary.core.file.api.FileSource
import io.github.taetae98coding.diary.core.model.account.UserData
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.network.api.profile.datasource.ProfileImageRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.profile.entity.ProfileImageRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseAuth
import io.github.taetae98coding.diary.core.supabase.api.SupabaseUser
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.io.Buffer
import kotlinx.io.RawSource
import kotlinx.io.readByteArray
import kotlin.uuid.Uuid

class UserDataRepositoryImplTest :
    FunSpec({
        test("TC-ACCOUNT-DATA-001 인증 제공자에 사용자 정보가 없으면 저장된 사용자 정보도 없다") {
            val supabaseAuth = mockk<SupabaseAuth>()
            every { supabaseAuth.getUserFlow() } returns flowOf(null)
            val repository = repository(supabaseAuth = supabaseAuth)

            repository.get().test {
                awaitItem().shouldBeNull()
                awaitComplete()
            }
        }

        listOf("프로필 이미지가 있음", "프로필 이미지가 없음").forEach { profileImageCase ->
            test("TC-ACCOUNT-DATA-002 $profileImageCase 인증 제공자의 사용자 정보를 저장된 사용자 정보로 사용한다") {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val email = fixtureMonkey.giveMeOne<String>()
                val profileImage = if (profileImageCase == "프로필 이미지가 있음") fixtureMonkey.giveMeOne<String>() else null
                val supabaseUser = SupabaseUser(id = id, email = email, profileImage = profileImage)
                val supabaseAuth = mockk<SupabaseAuth>()
                every { supabaseAuth.getUserFlow() } returns flowOf(supabaseUser)
                val repository = repository(supabaseAuth = supabaseAuth)

                repository.get().test {
                    awaitItem() shouldBe UserData(id = id, email = email, profileImage = profileImage)
                    awaitComplete()
                }
            }
        }

        test("TC-PROFILE-IMAGE-DATA-001 고른 사진에서 읽은 이미지 내용과 형식을 반영 요청에 담는다") {
            val uri = fileUri()
            val bytes = "profile-image-${fixtureMonkey.giveMeOne<String>()}".encodeToByteArray()
            val fileReader = fileReader(uri = uri, mimeType = "image/webp", bytes = bytes)
            val mimeTypeSlot = slot<String>()
            val contentLengthSlot = slot<Long>()
            val openContentSlot = slot<() -> RawSource>()
            val profileImageRemoteDataSource = mockk<ProfileImageRemoteDataSource>()
            coEvery {
                profileImageRemoteDataSource.upload(
                    mimeType = capture(mimeTypeSlot),
                    contentLength = capture(contentLengthSlot),
                    openContent = capture(openContentSlot),
                )
            } returns fixtureMonkey.giveMeOne<ProfileImageRemoteEntity>()
            val repository =
                repository(fileReader = fileReader, profileImageRemoteDataSource = profileImageRemoteDataSource)

            repository.updateProfileImage(uri = uri)

            mimeTypeSlot.captured shouldBe "image/webp"
            contentLengthSlot.captured shouldBe bytes.size.toLong()
            openContentSlot.captured().readBytes() shouldBe bytes
        }

        test("TC-PROFILE-IMAGE-DATA-002 고른 사진을 읽지 못하면 반영을 요청하지 않고 실패한다") {
            val uri = fileUri()
            val fileReader = mockk<FileReader>()
            coEvery { fileReader.open(uri = uri) } throws IllegalStateException("File cannot be read.")
            val profileImageRemoteDataSource = profileImageRemoteDataSource()
            val repository =
                repository(fileReader = fileReader, profileImageRemoteDataSource = profileImageRemoteDataSource)

            shouldThrowAny { repository.updateProfileImage(uri = uri) }

            coVerify(exactly = 0) { profileImageRemoteDataSource.upload(mimeType = any(), contentLength = any(), openContent = any()) }
        }

        test("TC-PROFILE-IMAGE-DATA-004 반영에 실패하면 프로필 이미지가 직전 주소를 유지한다") {
            val uri = fileUri()
            val previousUser =
                SupabaseUser(
                    id = fixtureMonkey.giveMeOne<Uuid>(),
                    email = fixtureMonkey.giveMeOne<String>(),
                    profileImage = "previous-${fixtureMonkey.giveMeOne<String>()}",
                )
            val supabaseAuth = mockk<SupabaseAuth>()
            every { supabaseAuth.getUserFlow() } returns MutableStateFlow(previousUser)
            val profileImageRemoteDataSource = mockk<ProfileImageRemoteDataSource>()
            coEvery {
                profileImageRemoteDataSource.upload(mimeType = any(), contentLength = any(), openContent = any())
            } throws IllegalStateException("Profile image upload failed.")
            val repository =
                repository(
                    supabaseAuth = supabaseAuth,
                    fileReader = fileReader(uri = uri, mimeType = "image/png", bytes = fixtureMonkey.giveMeOne<ByteArray>()),
                    profileImageRemoteDataSource = profileImageRemoteDataSource,
                )

            repository.get().test {
                awaitItem()?.profileImage shouldBe previousUser.profileImage

                shouldThrowAny { repository.updateProfileImage(uri = uri) }

                expectNoEvents()
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun fileUri(): FileUri = FileUri("file://${fixtureMonkey.giveMeOne<String>()}")

        private fun supabaseAuth(): SupabaseAuth =
            mockk<SupabaseAuth>().also { auth ->
                every { auth.getUserFlow() } returns flowOf(fixtureMonkey.giveMeOne<SupabaseUser>())
            }

        private fun fileReader(
            uri: FileUri,
            mimeType: String,
            bytes: ByteArray,
        ): FileReader =
            mockk<FileReader>().also { reader ->
                coEvery { reader.open(uri = uri) } returns fileSource(mimeType = mimeType, bytes = bytes)
            }

        private fun fileSource(
            mimeType: String,
            bytes: ByteArray,
        ): FileSource =
            mockk<FileSource>().also { source ->
                every { source.mimeType } returns mimeType
                every { source.size } returns bytes.size.toLong()
                every { source.openSource() } answers { Buffer().apply { write(bytes) } }
            }

        private fun profileImageRemoteDataSource(): ProfileImageRemoteDataSource =
            mockk<ProfileImageRemoteDataSource>().also { dataSource ->
                coEvery {
                    dataSource.upload(mimeType = any(), contentLength = any(), openContent = any())
                } returns fixtureMonkey.giveMeOne<ProfileImageRemoteEntity>()
            }

        private fun RawSource.readBytes(): ByteArray =
            use { source ->
                Buffer().apply { source.readAtMostTo(this, Long.MAX_VALUE) }.readByteArray()
            }

        private fun repository(
            supabaseAuth: SupabaseAuth = supabaseAuth(),
            fileReader: FileReader = mockk(),
            profileImageRemoteDataSource: ProfileImageRemoteDataSource = profileImageRemoteDataSource(),
        ): UserDataRepositoryImpl =
            UserDataRepositoryImpl(
                supabaseAuth = supabaseAuth,
                fileReader = fileReader,
                profileImageRemoteDataSource = profileImageRemoteDataSource,
            )
    }
}
