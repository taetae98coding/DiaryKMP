package io.github.taetae98coding.diary.core.network.impl.entitytag.datasource

import io.github.taetae98coding.diary.core.network.api.entitytag.entity.EntityTagKindRemoteEntity
import io.github.taetae98coding.diary.core.network.api.entitytag.entity.EntityTagPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.entitytag.entity.EntityTagRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.TestException
import io.github.taetae98coding.diary.core.network.impl.sync.entity.PullRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.testing.entitytag.entityTagRemoteEntity
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

// 종류마다 서버 함수와 JSON 키가 다르므로 종류와 그 계약, 문서의 케이스를 묶는다.
private data class EntityTagRemoteCase(
    val label: String,
    val kind: EntityTagKindRemoteEntity,
    val keyPrefix: String,
    val functionName: String,
    val pushTcId: String,
    val pullTcId: String,
)

private val entityTagRemoteCaseList: List<EntityTagRemoteCase> =
    listOf(
        EntityTagRemoteCase(
            label = "웹 항목",
            kind = EntityTagKindRemoteEntity.WEB,
            keyPrefix = "web",
            functionName = "web-tag",
            pushTcId = "TC-fbebf0be-a83a-43c9-8c7b-0c47f25e1e00 TC-fb383a98-cf0e-4086-af3d-4e557b8edd75",
            pullTcId = "TC-3de3f780-f097-4009-b2ff-da214d0d3328",
        ),
        EntityTagRemoteCase(
            label = "장소",
            kind = EntityTagKindRemoteEntity.PLACE,
            keyPrefix = "place",
            functionName = "place-tag",
            pushTcId = "TC-7304e71b-b36f-423d-9efa-52d3d75bcdd9 TC-a7ac9999-59d1-445b-bc5b-312a87bad444",
            pullTcId = "TC-96b8e102-0188-48ac-83f6-9f2651aaee11",
        ),
        EntityTagRemoteCase(
            label = "연락처",
            kind = EntityTagKindRemoteEntity.CONTACT,
            keyPrefix = "contact",
            functionName = "contact-tag",
            pushTcId = "TC-7116fdba-725f-490e-ab90-4d6772791a88 TC-f5d7255d-3752-4f7d-8c5e-882b71fa516e",
            pullTcId = "TC-bf88c8cb-9494-4bf7-a207-bc4d48a1fdb7",
        ),
        EntityTagRemoteCase(
            label = "체크리스트",
            kind = EntityTagKindRemoteEntity.CHECKLIST,
            keyPrefix = "checklist",
            functionName = "checklist-tag",
            pushTcId = "TC-88450497-a69a-4738-b91b-558fa1d00cda TC-c2ec35b0-3f18-47d6-89cb-685728ff2fb6",
            pullTcId = "TC-a6fd5fa8-6296-445d-b8ac-7d06615f38db",
        ),
        EntityTagRemoteCase(
            label = "곡",
            kind = EntityTagKindRemoteEntity.MUSIC,
            keyPrefix = "music",
            functionName = "music-tag",
            pushTcId = "TC-f0ac2072-ec83-435d-a33e-63afe03d825f TC-ffb0dac8-27ef-4c74-b9d6-78f632e07694",
            pullTcId = "TC-08270649-c0fd-4fa9-beff-db394a7c6f30",
        ),
        EntityTagRemoteCase(
            label = "QR",
            kind = EntityTagKindRemoteEntity.QR,
            keyPrefix = "qr",
            functionName = "qr-tag",
            pushTcId = "TC-f7ef8c7b-8bd9-4627-a510-fb09b2099345 TC-7480fbc5-d840-49ae-af4a-918f9ce3ccd2",
            pullTcId = "TC-e6e6a0ab-99b7-446a-b6d5-0d03d0372fe1",
        ),
        EntityTagRemoteCase(
            label = "디데이",
            kind = EntityTagKindRemoteEntity.DDAY,
            keyPrefix = "dday",
            functionName = "dday-tag",
            pushTcId = "TC-625e9cc2-5758-408e-b2cc-3848aced64e9 TC-e3c79a10-0f67-404b-9a01-c528f91912af",
            pullTcId = "TC-5540aae3-5d58-4bcb-a8cf-7f74047a738d",
        ),
    )

class EntityTagRemoteDataSourceImplTest :
    FunSpec({
        test("모든 종류의 서버 계약을 검증한다") {
            entityTagRemoteCaseList.map { case -> case.kind }.toSet() shouldBe EntityTagKindRemoteEntity.entries.toSet()
        }

        entityTagRemoteCaseList.forEach { case ->
            val pushFunction = "v1-sync-push-${case.functionName}"
            val pullFunction = "v1-sync-pull-${case.functionName}"

            test("${case.pushTcId} ${case.label}과 태그의 연결의 전체 상태를 $pushFunction 함수에 종류별 키로 전달한다") {
                val entityTagList = listOf(entityTag(), entityTag(isDeleted = true))
                val response = mockk<HttpResponse>()
                every { response.status } returns HttpStatusCode.OK
                val requestSlot = slot<JsonObject>()
                val supabaseFunction = mockk<SupabaseFunction>()
                coEvery {
                    supabaseFunction(
                        function = pushFunction,
                        body = capture(requestSlot),
                        typeInfo = any(),
                        headers = any(),
                    )
                } returns response
                val dataSource = EntityTagRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

                dataSource.push(kind = case.kind, entityTagList = entityTagList)

                requestSlot.captured shouldBe
                    buildJsonObject {
                        put(
                            "${case.keyPrefix}TagList",
                            buildJsonArray { entityTagList.forEach { entityTag -> add(entityTag.toJson(case = case)) } },
                        )
                    }
                coVerify(exactly = 1) {
                    supabaseFunction(
                        function = pushFunction,
                        body = any<JsonObject>(),
                        typeInfo = any(),
                        headers = any(),
                    )
                }
            }

            test("${case.pullTcId} ${case.label}과 태그의 연결의 최신 전체 상태와 서버 변경 순번을 응답한다") {
                val usn = Arb.long().next()
                val pullList = List(2) { EntityTagPullRemoteEntity(entityTag = entityTag(), usn = Arb.long().next()) }
                val requestSlot = slot<PullRequestRemoteEntity>()
                val supabaseFunction = mockk<SupabaseFunction>()
                coEvery {
                    supabaseFunction(
                        function = pullFunction,
                        body = capture(requestSlot),
                        typeInfo = any(),
                        headers = any(),
                    )
                } returns
                    httpResponse(
                        body =
                            buildJsonObject {
                                put(
                                    "${case.keyPrefix}TagList",
                                    buildJsonArray {
                                        pullList.forEach { pull ->
                                            add(
                                                buildJsonObject {
                                                    put("${case.keyPrefix}Tag", pull.entityTag.toJson(case = case))
                                                    put("usn", JsonPrimitive(pull.usn))
                                                },
                                            )
                                        }
                                    },
                                )
                            }.toString(),
                    )
                val dataSource = EntityTagRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

                val actual = dataSource.pull(kind = case.kind, usn = usn)

                requestSlot.captured.usn shouldBe usn
                actual shouldBe pullList
            }

            test("${case.label}과 태그의 연결 pull 함수가 실패하면 오류를 그대로 전달한다") {
                val failure = TestException(Arb.string().next())
                val supabaseFunction = mockk<SupabaseFunction>()
                coEvery {
                    supabaseFunction(
                        function = pullFunction,
                        body = any<PullRequestRemoteEntity>(),
                        typeInfo = any(),
                        headers = any(),
                    )
                } throws failure
                val dataSource = EntityTagRemoteDataSourceImpl(supabaseFunction = supabaseFunction)

                shouldThrow<TestException> {
                    dataSource.pull(kind = case.kind, usn = Arb.long().next())
                }
            }
        }
    }) {
    public companion object {
        private fun entityTag(isDeleted: Boolean = false): EntityTagRemoteEntity =
            Arb
                .entityTagRemoteEntity()
                .next()
                .copy(
                    isDeleted = isDeleted,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                )

        private fun EntityTagRemoteEntity.toJson(case: EntityTagRemoteCase): JsonObject =
            buildJsonObject {
                put("${case.keyPrefix}Id", JsonPrimitive(entityId.toString()))
                put("tagId", JsonPrimitive(tagId.toString()))
                put("isDeleted", JsonPrimitive(isDeleted))
                put("updatedAt", JsonPrimitive(updatedAt.toString()))
                put("createdAt", JsonPrimitive(createdAt.toString()))
            }

        private suspend fun httpResponse(
            body: String,
            status: HttpStatusCode = HttpStatusCode.OK,
        ): HttpResponse {
            val client =
                HttpClient(
                    MockEngine {
                        respond(
                            content = body,
                            status = status,
                            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                        )
                    },
                ) {
                    install(ContentNegotiation) {
                        json(Json)
                    }
                }

            return client.get("/")
        }
    }
}
