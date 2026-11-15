package io.github.taetae98coding.diary.feature.playlist.ui.detail

import io.github.taetae98coding.diary.compose.core.share.ShareContent
import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import io.github.taetae98coding.diary.library.fixture.alphanumericString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class MusicShareContentTest :
    FunSpec({
        test("TC-59a5cc39-fab4-4ff6-afff-56ec90517f7d 공유하는 글은 제목 - 가수 줄과 링크 줄이다") {
            val storedTitle = Arb.alphanumericString(prefix = "StoredTitle").next()
            val title = Arb.alphanumericString(prefix = "Title").next()
            val artist = Arb.alphanumericString(prefix = "Artist").next()

            listOf(
                MusicDetail(title = title, artist = artist, link = LINK) to text(text = "$title - $artist\n$LINK", title = title),
                MusicDetail(title = title, artist = "", link = LINK) to text(text = "$title\n$LINK", title = title),
                MusicDetail(title = title, artist = "   ", link = LINK) to text(text = "$title\n$LINK", title = title),
                MusicDetail(title = " $title\n", artist = "\t$artist ", link = "  $LINK\n") to text(text = "$title - $artist\n$LINK", title = title),
                MusicDetail(title = "", artist = artist, link = LINK) to text(text = "$storedTitle - $artist\n$LINK", title = storedTitle),
                MusicDetail(title = "   ", artist = "", link = LINK) to text(text = "$storedTitle\n$LINK", title = storedTitle),
                MusicDetail(title = title, artist = artist, link = "") to MusicShareContent.LinkBlank,
                MusicDetail(title = title, artist = artist, link = "   ") to MusicShareContent.LinkBlank,
                MusicDetail(title = title, artist = artist, link = NOT_YOUTUBE_LINK) to MusicShareContent.LinkNotYoutube,
            ).forEach { (detail, expected) ->
                musicShareContentOf(detail = detail, storedTitle = storedTitle) shouldBe expected
            }
        }
    }) {
    private companion object {
        private const val LINK = "https://youtu.be/11cta61wi0g"
        private const val NOT_YOUTUBE_LINK = "https://example.com"

        private fun text(
            text: String,
            title: String,
        ): MusicShareContent = MusicShareContent.Text(content = ShareContent.Text(text = text, title = title))
    }
}
