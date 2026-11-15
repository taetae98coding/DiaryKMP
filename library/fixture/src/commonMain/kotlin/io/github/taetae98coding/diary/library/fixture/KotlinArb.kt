package io.github.taetae98coding.diary.library.fixture

import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.alphanumeric
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.byte
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import kotlin.time.Instant
import kotlin.uuid.Uuid

/** 모델이 담는 목록·집합·맵의 최대 크기다. 테스트가 크기 자체를 다루지 않으면 몇 개로 충분하고, 크면 중첩된 모델의 생성이 느려진다. */
public const val MAX_COLLECTION_SIZE: Int = 3

// 1970-01-01부터 2100-01-01까지. 저장과 직렬화를 거쳐도 뜻이 남는 범위다.
private const val MIN_EPOCH_SECONDS = 0L
private const val MAX_EPOCH_SECONDS = 4_102_444_800L

/**
 * Kotest의 [Arb.Companion.uuid]는 플랫폼마다 다른 `PlatformUuid`(JVM에서는 `java.util.UUID`)를 만들어 공통 코드에서 [Uuid]로 쓸 수 없으므로
 * [Uuid.random]으로 만든다.
 */
public fun Arb.Companion.kotlinUuid(): Arb<Uuid> = arbitrary { Uuid.random() }

/**
 * 저장소는 [Instant]를 에포크 밀리초로, 일부 원격 계약은 에포크 초로 다루므로 그보다 정밀한 값은 왕복하면 달라진다.
 * 초 정밀도는 두 경로 모두에서 값이 보존되는 가장 정밀한 단위다.
 */
public fun Arb.Companion.secondPrecisionInstant(): Arb<Instant> = Arb.long(MIN_EPOCH_SECONDS..MAX_EPOCH_SECONDS).map(Instant::fromEpochSeconds)

/**
 * [Arb.Companion.string]은 약 1%가 빈 문자열이라, 빈 문자열이 나오면 기본값이 빈 문자열인 모델과 같아지거나 빈 문자열을
 * 초깃값으로 두는 `StateFlow`의 방출이 합쳐져 판정이 뒤집힌다. 그런 테스트는 이 Arb를 쓰고, 빈 문자열 자체가 목적이면
 * 직접 지정한다.
 */
public fun Arb.Companion.nonBlankString(): Arb<String> = Arb.string(minSize = 1).filter { value -> value.isNotBlank() }

/**
 * 공백·줄바꿈·기호가 섞이면 화면 표시와 노드 탐색, 경로 해석이 값에 따라 달라지므로 화면에서 글자로 찾거나 경로·식별자에
 * 이어 붙이는 문자열은 이 Arb를 쓴다. 비어 있을 수 있으므로 어느 값인지 알아보는 접두사를 받는 쪽을 주로 쓴다.
 */
public fun Arb.Companion.alphanumericString(): Arb<String> = Arb.string(codepoints = Codepoint.alphanumeric())

public fun Arb.Companion.alphanumericString(prefix: String): Arb<String> = Arb.alphanumericString().map { suffix -> prefix + suffix }

public fun Arb.Companion.randomByteArray(): Arb<ByteArray> = Arb.list(Arb.byte()).map { bytes -> bytes.toByteArray() }
