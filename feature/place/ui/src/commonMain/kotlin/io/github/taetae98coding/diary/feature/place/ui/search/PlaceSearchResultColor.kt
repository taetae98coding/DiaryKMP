package io.github.taetae98coding.diary.feature.place.ui.search

import androidx.compose.ui.graphics.Color
import io.github.taetae98coding.diary.core.model.place.SearchedPlace
import io.github.taetae98coding.diary.library.compose.ui.color.randomColor
import kotlin.random.Random

// 식별 값은 검색할 때마다 새로 만들어지므로, 다시 검색해도 같은 컬러가 나오도록 제공자가 돌려준 이름과 좌표로 무작위 값을 고정한다.
internal val SearchedPlace.resultColor: Color
    get() = randomColor(random = Random(seed = (name to coordinate).hashCode()))
