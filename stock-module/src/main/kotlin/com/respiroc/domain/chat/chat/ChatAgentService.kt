package com.respiroc.domain.chat.chat

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.respiroc.domain.chat.config.AIConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
@Service
class ChatAgentService {

    private val restTemplate = RestTemplate()
    private val objectMapper = jacksonObjectMapper()
    private val apiUrl =  AIConfig.APIURL


    val apiKey = AIConfig.OPENROUTER_API_KEY

    fun askAgent(userInput: String): ResponseEntity<String> {
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            set("Authorization", "Bearer $apiKey")
        }

        val chatRequest = mapOf(
            "model" to "openai/gpt-3.5-turbo",
            "messages" to listOf(
                mapOf("role" to "system", "content" to "You are a helpful assistant."),
                mapOf("role" to "user", "content" to userInput)
            )
        )

        val entity = HttpEntity(objectMapper.writeValueAsString(chatRequest), headers)

        return try {
            val response = restTemplate.exchange<String>(apiUrl, HttpMethod.POST, entity)
            println("OpenRouter API response status: ${response.statusCode}")
            println("OpenRouter API response body: ${response.body}")

            if (!response.statusCode.is2xxSuccessful) {
                return ResponseEntity.status(response.statusCode).body("Error from AI API: ${response.body}")
            }
            if (response.body.isNullOrBlank()) {
                return ResponseEntity.status(500).body("AI API returned empty response body")
            }

            val json: Map<String, Any> = objectMapper.readValue(
                response.body,
                object : TypeReference<Map<String, Any>>() {}
            )

            val replyText = (((json["choices"] as? List<*>)?.get(0) as? Map<*, *>)?.get("message") as? Map<*, *>)?.get("content")?.toString()
                ?: "No reply"

            ResponseEntity.ok(replyText)

        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.status(500).body("Error: ${e.message}")
        }
    }
}
