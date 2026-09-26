package io.github.taetae98coding.diary.core.testing.browser

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.browser.ChromeProfile

// 생성 문자열은 빈 값이 섞여 두 프로필의 폴더나 이름이 같아질 수 있으므로, 순번을 붙여 목록 안에서 서로 구분되게 한다.
public fun FixtureMonkey.chromeProfileList(size: Int): List<ChromeProfile> =
    List(size) { index ->
        ChromeProfile(
            directory = "profile-$index-${giveMeOne<String>()}",
            name = "name-$index-${giveMeOne<String>()}",
        )
    }
