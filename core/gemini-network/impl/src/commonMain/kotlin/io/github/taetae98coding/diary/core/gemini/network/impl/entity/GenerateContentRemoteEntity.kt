package io.github.taetae98coding.diary.core.gemini.network.impl.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
internal data class GenerateContentRequestRemoteEntity(
    @SerialName("contents") val contents: List<ContentRemoteEntity>,
    @SerialName("generationConfig") val generationConfig: GenerationConfigRemoteEntity,
    @SerialName("systemInstruction") val systemInstruction: ContentRemoteEntity? = null,
)

@Serializable
internal data class GenerateContentResponseRemoteEntity(
    @SerialName("candidates") val candidates: List<CandidateRemoteEntity> = emptyList(),
)

@Serializable
internal data class CandidateRemoteEntity(
    @SerialName("content") val content: ContentRemoteEntity? = null,
)

@Serializable
internal data class ContentRemoteEntity(
    @SerialName("parts") val parts: List<PartRemoteEntity> = emptyList(),
)

@Serializable
internal data class PartRemoteEntity(
    @SerialName("text") val text: String = "",
)

@Serializable
internal data class GenerationConfigRemoteEntity(
    @SerialName("responseMimeType") val responseMimeType: String,
    @SerialName("responseSchema") val responseSchema: JsonObject,
)
