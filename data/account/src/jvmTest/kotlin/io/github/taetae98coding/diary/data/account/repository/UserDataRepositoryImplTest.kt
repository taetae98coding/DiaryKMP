package io.github.taetae98coding.diary.data.account.repository

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.image.api.ImageConverter
import io.github.taetae98coding.diary.core.image.api.JpegSource
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
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
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

        test("TC-PROFILE-IMAGE-DATA-006 고른 사진을 JPEG로 바꾸지 못하면 반영을 요청하지 않고 실패한다") {
            val uri = fileUri()
            val imageConverter = mockk<ImageConverter>()
            coEvery { imageConverter.toJpeg(uri = uri) } throws IllegalStateException("Image cannot be converted.")
            val profileImageRemoteDataSource = profileImageRemoteDataSource()
            val repository =
                repository(imageConverter = imageConverter, profileImageRemoteDataSource = profileImageRemoteDataSource)

            shouldThrowAny { repository.updateProfileImage(uri = uri) }

            coVerify(exactly = 0) { profileImageRemoteDataSource.upload(mimeType = any(), contentLength = any(), openContent = any()) }
        }

        test("TC-PROFILE-IMAGE-DATA-005 JPEG로 바꾼 이미지를 반영 요청에 담는다") {
            val uri = fileUri()
            val jpegBytes = imageBytes()
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
                repository(
                    imageConverter = imageConverter(uri = uri, jpegBytes = jpegBytes),
                    profileImageRemoteDataSource = profileImageRemoteDataSource,
                )

            repository.updateProfileImage(uri = uri)

            mimeTypeSlot.captured shouldBe "image/jpeg"
            contentLengthSlot.captured shouldBe jpegBytes.size.toLong()
            openContentSlot.captured().readBytes() shouldBe jpegBytes
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
            val repository =
                repository(
                    supabaseAuth = supabaseAuth,
                    imageConverter = imageConverter(uri = uri),
                    profileImageRemoteDataSource = failingProfileImageRemoteDataSource(),
                )

            repository.get().test {
                awaitItem()?.profileImage shouldBe previousUser.profileImage

                shouldThrowAny { repository.updateProfileImage(uri = uri) }

                expectNoEvents()
            }
        }

        listOf("반영에 성공", "반영에 실패").forEach { uploadCase ->
            test("$uploadCase 해도 바꾼 이미지를 정리한다") {
                val uri = fileUri()
                val jpegSource = jpegSource(bytes = imageBytes())
                val imageConverter = mockk<ImageConverter>()
                coEvery { imageConverter.toJpeg(uri = uri) } returns jpegSource
                val profileImageRemoteDataSource =
                    if (uploadCase == "반영에 성공") profileImageRemoteDataSource() else failingProfileImageRemoteDataSource()
                val repository =
                    repository(imageConverter = imageConverter, profileImageRemoteDataSource = profileImageRemoteDataSource)

                runCatching { repository.updateProfileImage(uri = uri) }

                verify(exactly = 1) { jpegSource.close() }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun fileUri(): FileUri = FileUri("file://${fixtureMonkey.giveMeOne<String>()}")

        private fun imageBytes(): ByteArray = "image-${fixtureMonkey.giveMeOne<String>()}".encodeToByteArray()

        private fun supabaseAuth(): SupabaseAuth =
            mockk<SupabaseAuth>().also { auth ->
                every { auth.getUserFlow() } returns flowOf(fixtureMonkey.giveMeOne<SupabaseUser>())
            }

        private fun jpegSource(bytes: ByteArray): JpegSource =
            mockk<JpegSource>().also { source ->
                every { source.size } returns bytes.size.toLong()
                every { source.openSource() } answers { Buffer().apply { write(bytes) } }
                every { source.close() } just runs
            }

        private fun imageConverter(
            uri: FileUri = fileUri(),
            jpegBytes: ByteArray = imageBytes(),
        ): ImageConverter =
            mockk<ImageConverter>().also { converter ->
                coEvery { converter.toJpeg(uri = uri) } returns jpegSource(bytes = jpegBytes)
            }

        private fun profileImageRemoteDataSource(): ProfileImageRemoteDataSource =
            mockk<ProfileImageRemoteDataSource>().also { dataSource ->
                coEvery {
                    dataSource.upload(mimeType = any(), contentLength = any(), openContent = any())
                } returns fixtureMonkey.giveMeOne<ProfileImageRemoteEntity>()
            }

        private fun failingProfileImageRemoteDataSource(): ProfileImageRemoteDataSource =
            mockk<ProfileImageRemoteDataSource>().also { dataSource ->
                coEvery {
                    dataSource.upload(mimeType = any(), contentLength = any(), openContent = any())
                } throws IllegalStateException("Profile image upload failed.")
            }

        private fun RawSource.readBytes(): ByteArray =
            use { source ->
                Buffer().apply { source.readAtMostTo(this, Long.MAX_VALUE) }.readByteArray()
            }

        private fun repository(
            supabaseAuth: SupabaseAuth = supabaseAuth(),
            imageConverter: ImageConverter = mockk(),
            profileImageRemoteDataSource: ProfileImageRemoteDataSource = profileImageRemoteDataSource(),
        ): UserDataRepositoryImpl =
            UserDataRepositoryImpl(
                supabaseAuth = supabaseAuth,
                imageConverter = imageConverter,
                profileImageRemoteDataSource = profileImageRemoteDataSource,
            )
    }
}
