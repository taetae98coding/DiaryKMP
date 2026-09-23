package io.github.taetae98coding.diary.domain.playlist.link

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

private const val VIDEO_ID: String = "dQw4w9WgXcQ"
private const val THUMBNAIL: String = "https://i.ytimg.com/vi/$VIDEO_ID/hqdefault.jpg"

class YoutubeVideoThumbnailTest :
    FunSpec({
        listOf(
            "https://www.youtube.com/watch?v=$VIDEO_ID",
            "https://m.youtube.com/watch?v=$VIDEO_ID&list=PL123&t=42s",
            "https://www.youtube.com/watch?list=PL123&v=$VIDEO_ID",
            "https://music.youtube.com/watch?v=$VIDEO_ID",
            "https://youtu.be/$VIDEO_ID",
            "https://youtu.be/$VIDEO_ID?t=42",
            "https://www.youtube.com/shorts/$VIDEO_ID",
            "https://www.youtube.com/embed/$VIDEO_ID",
            "https://www.youtube.com/live/$VIDEO_ID",
            "https://www.youtube.com/v/$VIDEO_ID",
            "  https://youtu.be/$VIDEO_ID  ",
            "HTTPS://WWW.YOUTUBE.COM/watch?v=$VIDEO_ID",
            "http://www.youtube.com/watch?v=$VIDEO_ID#fragment",
        ).forEach { link ->
            test("TC-MUSIC-ADD-DOMAIN-017 영상 ID를 얻을 수 있는 링크는 그 영상의 썸네일이 정해진다: '$link'") {
                link.toYoutubeVideoThumbnailOrNull() shouldBe THUMBNAIL
            }
        }

        listOf(
            "",
            "   ",
            "https://www.youtube.com/",
            "https://www.youtube.com/@channel",
            "https://www.youtube.com/playlist?list=PL123",
            "https://www.youtube.com/watch",
            "https://www.youtube.com/watch?v=short",
            "https://www.youtube.com/watch?v=dQw4w9WgXc!",
            "https://www.youtube.com/watch?v=dQw4w9WgXcQextra",
            "https://youtu.be/",
            "https://www.youtube.com/shorts/",
            "https://www.youtube.com/shorts/$VIDEO_ID/extra",
            "https://www.youtube.com/channel/$VIDEO_ID",
            "https://example.com/watch?v=$VIDEO_ID",
            "https://notyoutube.com/watch?v=$VIDEO_ID",
            "https://youtube.com.attacker.example/watch?v=$VIDEO_ID",
            "www.youtube.com/watch?v=$VIDEO_ID",
        ).forEach { link ->
            test("TC-MUSIC-ADD-DOMAIN-018 영상 ID를 얻을 수 없는 링크는 썸네일이 없다: '$link'") {
                link.toYoutubeVideoThumbnailOrNull().shouldBeNull()
            }
        }
    })
