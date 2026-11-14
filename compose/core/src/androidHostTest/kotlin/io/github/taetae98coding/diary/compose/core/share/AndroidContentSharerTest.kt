package io.github.taetae98coding.diary.compose.core.share

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.test.core.app.ApplicationProvider
import io.github.taetae98coding.diary.library.fixture.alphanumericString
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.library.fixture.randomByteArray
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AndroidContentSharerTest {
    private val application: Application = ApplicationProvider.getApplicationContext()

    @Before
    fun clearFileProviderPathCache() {
        val cache =
            FileProvider::class.java
                .getDeclaredField("sCache")
                .apply { isAccessible = true }
                .get(null) as MutableMap<*, *>
        synchronized(cache) { cache.clear() }
    }

    @Test
    fun `TC-debcbba1-3ab4-4edf-aa10-861dfa9fff9e Android는 글과 모든 파일 공유를 제공한다`() =
        runTest {
            val sharer = AndroidContentSharer(context = application, dispatcher = StandardTestDispatcher(testScheduler))

            sharer.isTextSupported shouldBe true
            ShareFileType.entries.forEach { type -> sharer.isFileSupported(type = type) shouldBe true }
        }

    @Test
    fun `TC-425c65e5-12a0-4083-abf1-b6b284a6f676 글과 공유 제목을 담아 공유 시트를 연다`() =
        runTest {
            val sharer = AndroidContentSharer(context = application, dispatcher = StandardTestDispatcher(testScheduler))
            val content = ShareContent.Text(text = Arb.nonBlankString().next(), title = Arb.nonBlankString().next())

            val result = sharer.share(content = content)

            result shouldBe ShareResult.COMPLETED
            val send = nextSendIntent()
            send.action shouldBe Intent.ACTION_SEND
            send.type shouldBe "text/plain"
            send.getStringExtra(Intent.EXTRA_TEXT) shouldBe content.text
            send.getStringExtra(Intent.EXTRA_SUBJECT) shouldBe content.title
            send.getStringExtra(Intent.EXTRA_TITLE) shouldBe content.title
            shadowOf(application).nextStartedActivity shouldBe null
        }

    @Test
    fun `TC-d8bd9545-723d-428e-81c1-3ca379e0f0c4 받는 앱이 읽을 수 있는 파일을 담아 공유 시트를 연다`() =
        runTest {
            val sharer = AndroidContentSharer(context = application, dispatcher = StandardTestDispatcher(testScheduler))

            listOf(
                ShareFileType.VCARD to "text/vcard",
                ShareFileType.CALENDAR to "text/calendar",
                ShareFileType.PNG to "image/png",
            ).forEach { (type, mimeType) ->
                val content =
                    ShareContent.File(
                        bytes = Arb.randomByteArray().next(),
                        name = Arb.alphanumericString(prefix = "Name").next(),
                        type = type,
                        text = Arb.nonBlankString().next(),
                        title = Arb.nonBlankString().next(),
                    )

                sharer.share(content = content) shouldBe ShareResult.COMPLETED

                val send = nextSendIntent()
                val uri = send.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java).shouldNotBeNull()
                send.action shouldBe Intent.ACTION_SEND
                send.type shouldBe mimeType
                (send.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION) shouldBe Intent.FLAG_GRANT_READ_URI_PERMISSION
                send.clipData?.getItemAt(0)?.uri shouldBe uri
                uri.lastPathSegment shouldBe "${content.name}.${type.extension}"
                send.getStringExtra(Intent.EXTRA_TEXT) shouldBe content.text
                send.getStringExtra(Intent.EXTRA_SUBJECT) shouldBe content.title
                application.contentResolver
                    .openInputStream(uri)
                    .shouldNotBeNull()
                    .use { stream -> stream.readBytes() } shouldBe content.bytes
            }
        }

    @Test
    fun `TC-8ac81352-717f-406a-a463-88b01be5d05a 공유 시트를 열지 못하면 공유하지 못한 것으로 끝난다`() =
        runTest {
            shadowOf(application).checkActivities(true)
            val sharer = AndroidContentSharer(context = application, dispatcher = StandardTestDispatcher(testScheduler))

            sharer.share(content = ShareContent.Text(text = Arb.nonBlankString().next())) shouldBe ShareResult.FAILED
        }

    @Test
    fun `빈 글과 제목은 공유 시트에 넘기지 않는다`() =
        runTest {
            val sharer = AndroidContentSharer(context = application, dispatcher = StandardTestDispatcher(testScheduler))
            val content = ShareContent.File(bytes = Arb.randomByteArray().next(), name = Arb.alphanumericString(prefix = "Name").next(), type = ShareFileType.PNG)

            sharer.share(content = content)

            val send = nextSendIntent()
            send.hasExtra(Intent.EXTRA_TEXT) shouldBe false
            send.hasExtra(Intent.EXTRA_SUBJECT) shouldBe false
            send.hasExtra(Intent.EXTRA_TITLE) shouldBe false
        }

    @Test
    fun `다시 공유하면 이전에 공유한 파일은 남지 않는다`() =
        runTest {
            val sharer = AndroidContentSharer(context = application, dispatcher = StandardTestDispatcher(testScheduler))
            val first = ShareContent.File(bytes = Arb.randomByteArray().next(), name = Arb.alphanumericString(prefix = "First").next(), type = ShareFileType.VCARD)
            val second = ShareContent.File(bytes = Arb.randomByteArray().next(), name = Arb.alphanumericString(prefix = "Second").next(), type = ShareFileType.CALENDAR)

            sharer.share(content = first)
            nextSendIntent()
            sharer.share(content = second)
            nextSendIntent()

            application.cacheDir
                .resolve("share")
                .list()
                .orEmpty()
                .toList() shouldBe listOf(second.fileName)
        }

    // 공유 시트는 받는 앱을 고르는 화면으로 감싸 열리므로, 그 안에 담긴 보낼 내용을 꺼내 본다.
    private fun nextSendIntent(): Intent {
        val chooser = shadowOf(application).nextStartedActivity.shouldNotBeNull()
        chooser.action shouldBe Intent.ACTION_CHOOSER

        return chooser.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java).shouldNotBeNull()
    }
}
