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
    @SerialName("promptFeedback") val promptFeedback: PromptFeedbackRemoteEntity? = null,
)

@Serializable
internal data class CandidateRemoteEntity(
    @SerialName("content") val content: ContentRemoteEntity? = null,
    @SerialName("finishReason") val finishReason: String = "",
)

@Serializable
internal data class PromptFeedbackRemoteEntity(
    @SerialName("blockReason") val blockReason: String = "",
)

@Serializable
internal data class ContentRemoteEntity(
    @SerialName("parts") val parts: List<PartRemoteEntity> = emptyList(),
)

@Serializable
internal data class PartRemoteEntity(
    @SerialName("text") val text: String = "",
    @SerialName("thought") val isThought: Boolean = false,
)

@Serializable
internal data class GenerationConfigRemoteEntity(
    @SerialName("responseMimeType") val responseMimeType: String,
    @SerialName("responseJsonSchema") val responseJsonSchema: JsonObject,
)
