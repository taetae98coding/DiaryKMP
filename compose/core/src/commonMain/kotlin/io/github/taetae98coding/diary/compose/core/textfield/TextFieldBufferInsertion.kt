package io.github.taetae98coding.diary.compose.core.textfield

import androidx.compose.foundation.text.input.TextFieldBuffer

// 넘친 값을 뒤에서 자르면 중간에 넣은 문자가 원래 있던 문자를 밀어내므로, 넣은 문자를 하나씩 받아 그때의 값이 isValid를 지킬 때만 남긴다.
public fun TextFieldBuffer.keepInsertionWhile(isValid: (CharSequence) -> Boolean) {
    val original = originalText
    val current = asCharSequence()
    val prefixLength = current.commonPrefixWith(original).length
    val suffixLength =
        current
            .commonSuffixWith(original)
            .length
            .coerceAtMost(minOf(current.length, original.length) - prefixLength)
    val head = original.subSequence(0, prefixLength)
    val tail = original.subSequence(original.length - suffixLength, original.length)
    val inserted = current.subSequence(prefixLength, current.length - suffixLength)
    val accepted = StringBuilder()

    for (char in inserted) {
        if (isValid("$head$accepted$char$tail")) accepted.append(char)
    }

    when {
        accepted.length == inserted.length -> Unit

        accepted.isEmpty() -> revertAllChanges()

        else -> {
            replace(0, length, "$head$accepted$tail")
            placeCursorBeforeCharAt(prefixLength + accepted.length)
        }
    }
}
