package io.github.taetae98coding.diary.data.account.repository

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.file.api.ImageConverter
import io.github.taetae98coding.diary.core.file.api.datasource.FileLocalDataSource
import io.github.taetae98coding.diary.core.model.account.UserData
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.model.image.ImageCropRegion
import io.github.taetae98coding.diary.core.model.image.ImageFormat
import io.github.taetae98coding.diary.core.network.api.profile.datasource.ProfileImageRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.profile.entity.ProfileImageRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseAuth
import io.github.taetae98coding.diary.core.supabase.api.SupabaseUser
import io.github.taetae98coding.diary.core.testing.image.imageCropRegion
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
            coEvery { imageConverter.convert(source = uri, format = any(), cropRegion = any(), maxSideLength = any(), quality = any()) } throws IllegalStateException("Image cannot be converted.")
            val profileImageRemoteDataSource = profileImageRemoteDataSource()
            val repository =
                repository(imageConverter = imageConverter, profileImageRemoteDataSource = profileImageRemoteDataSource)

            shouldThrowAny { repository.updateProfileImage(uri = uri, format = ImageFormat.JPEG, cropRegion = cropRegion(), maxSideLength = MAX_SIDE_LENGTH, quality = QUALITY) }

            coVerify(exactly = 0) { profileImageRemoteDataSource.upload(mimeType = any(), contentLength = any(), openContent = any()) }
        }

        test("TC-PROFILE-IMAGE-DATA-005 JPEG로 바꾼 이미지를 반영 요청에 담는다") {
            val uri = fileUri()
            val converted = fileUri()
            val convertedBytes = imageBytes()
            val mimeTypeSlot = slot<String>()
            val contentLengthSlot = slot<Long>()
            val openContentSlot = slot<suspend () -> RawSource>()
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
                    imageConverter = imageConverter(uri = uri, converted = converted),
                    fileLocalDataSource = fileLocalDataSource(uri = converted, bytes = convertedBytes),
                    profileImageRemoteDataSource = profileImageRemoteDataSource,
                )

            repository.updateProfileImage(uri = uri, format = ImageFormat.JPEG, cropRegion = cropRegion(), maxSideLength = MAX_SIDE_LENGTH, quality = QUALITY)

            mimeTypeSlot.captured shouldBe "image/jpeg"
            contentLengthSlot.captured shouldBe convertedBytes.size.toLong()
            openContentSlot.captured().readBytes() shouldBe convertedBytes
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

                shouldThrowAny { repository.updateProfileImage(uri = uri, format = ImageFormat.JPEG, cropRegion = cropRegion(), maxSideLength = MAX_SIDE_LENGTH, quality = QUALITY) }

                expectNoEvents()
            }
        }

        listOf("반영에 성공", "반영에 실패").forEach { uploadCase ->
            test("$uploadCase 해도 바꾼 이미지 파일을 지운다") {
                val uri = fileUri()
                val converted = fileUri()
                val fileLocalDataSource = fileLocalDataSource(uri = converted)
                val profileImageRemoteDataSource =
                    if (uploadCase == "반영에 성공") profileImageRemoteDataSource() else failingProfileImageRemoteDataSource()
                val repository =
                    repository(
                        imageConverter = imageConverter(uri = uri, converted = converted),
                        fileLocalDataSource = fileLocalDataSource,
                        profileImageRemoteDataSource = profileImageRemoteDataSource,
                    )

                runCatching { repository.updateProfileImage(uri = uri, format = ImageFormat.JPEG, cropRegion = cropRegion(), maxSideLength = MAX_SIDE_LENGTH, quality = QUALITY) }

                coVerify(exactly = 1) { fileLocalDataSource.delete(uri = converted) }
            }
        }

        test("TC-PROFILE-IMAGE-DATA-007 고른 사진의 위치와 남길 영역을 그대로 이미지 변환에 전달한다") {
            val uri = fileUri()
            val cropRegion = cropRegion()
            val converted = fileUri()
            val imageConverter = mockk<ImageConverter>()
            coEvery { imageConverter.convert(source = any(), format = any(), cropRegion = any(), maxSideLength = any(), quality = any()) } returns converted
            val repository = repository(imageConverter = imageConverter, fileLocalDataSource = fileLocalDataSource(uri = converted))

            repository.updateProfileImage(uri = uri, format = ImageFormat.JPEG, cropRegion = cropRegion, maxSideLength = MAX_SIDE_LENGTH, quality = QUALITY)

            coVerify(exactly = 1) { imageConverter.convert(source = uri, format = ImageFormat.JPEG, cropRegion = cropRegion, maxSideLength = MAX_SIDE_LENGTH, quality = QUALITY) }
        }

        test("TC-MORE-HOME-DATA-005 사용자 정보 다시 확인은 인증 제공자에 현재 세션의 사용자 정보를 다시 요청한다") {
            val supabaseAuth = supabaseAuth()
            coEvery { supabaseAuth.retrieveUserForCurrentSession() } just runs
            val repository = repository(supabaseAuth = supabaseAuth)

            repository.refresh()

            coVerify(exactly = 1) { supabaseAuth.retrieveUserForCurrentSession() }
        }

        test("TC-MORE-HOME-DATA-006 사용자 정보 다시 확인에 실패하면 저장된 사용자 정보가 바뀌지 않는다") {
            val previousUser = fixtureMonkey.giveMeOne<SupabaseUser>()
            val supabaseAuth = mockk<SupabaseAuth>()
            every { supabaseAuth.getUserFlow() } returns MutableStateFlow(previousUser)
            coEvery { supabaseAuth.retrieveUserForCurrentSession() } throws IllegalStateException("retrieve failed")
            val repository = repository(supabaseAuth = supabaseAuth)

            repository.get().test {
                awaitItem() shouldBe UserData(id = previousUser.id, email = previousUser.email, profileImage = previousUser.profileImage)

                shouldThrowAny { repository.refresh() }

                expectNoEvents()
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private const val MAX_SIDE_LENGTH = 1024
        private const val QUALITY = 90

        private fun fileUri(): FileUri = FileUri("file://${fixtureMonkey.giveMeOne<String>()}")

        private fun cropRegion(): ImageCropRegion = fixtureMonkey.imageCropRegion()

        private fun imageBytes(): ByteArray = "image-${fixtureMonkey.giveMeOne<String>()}".encodeToByteArray()

        private fun supabaseAuth(): SupabaseAuth =
            mockk<SupabaseAuth>().also { auth ->
                every { auth.getUserFlow() } returns flowOf(fixtureMonkey.giveMeOne<SupabaseUser>())
            }

        private fun fileLocalDataSource(
            uri: FileUri = fileUri(),
            bytes: ByteArray = imageBytes(),
        ): FileLocalDataSource =
            mockk<FileLocalDataSource>().also { dataSource ->
                coEvery { dataSource.size(uri = uri) } returns bytes.size.toLong()
                coEvery { dataSource.openSource(uri = uri) } answers { Buffer().apply { write(bytes) } }
                coEvery { dataSource.delete(uri = any()) } just runs
            }

        private fun imageConverter(
            uri: FileUri = fileUri(),
            converted: FileUri = fileUri(),
        ): ImageConverter =
            mockk<ImageConverter>().also { converter ->
                coEvery { converter.convert(source = uri, format = any(), cropRegion = any(), maxSideLength = any(), quality = any()) } returns converted
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
            fileLocalDataSource: FileLocalDataSource = fileLocalDataSource(),
            profileImageRemoteDataSource: ProfileImageRemoteDataSource = profileImageRemoteDataSource(),
        ): UserDataRepositoryImpl =
            UserDataRepositoryImpl(
                supabaseAuth = supabaseAuth,
                imageConverter = imageConverter,
                fileLocalDataSource = fileLocalDataSource,
                profileImageRemoteDataSource = profileImageRemoteDataSource,
            )
    }
}
