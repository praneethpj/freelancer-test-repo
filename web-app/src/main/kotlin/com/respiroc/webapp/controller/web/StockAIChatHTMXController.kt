package com.respiroc.webapp.controller.web

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.respiroc.domain.chat.chat.ChatAgentService
import com.respiroc.domain.chat.chat.dto.ProductStockDTO
import com.respiroc.domain.chat.chat.dto.ItemTypeDTO
import com.respiroc.domain.chat.chat.dto.toDTO
import com.respiroc.domain.chat.stock.service.ItemTypeService
import com.respiroc.domain.chat.stock.service.ProductStockService
import com.respiroc.webapp.controller.BaseController
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/chat-module")
class StockAIChatHTMXController(
    private val chatAgentService: ChatAgentService,
    private val productStockService: ProductStockService,
    private val itemTypeService: ItemTypeService
) : BaseController() {

    @GetMapping("/open-chat-ui")
    fun openChatUI(
        model: Model,
        @AuthenticationPrincipal userDetails: UserDetails?
    ): String {
        if (userDetails == null) {
            return "redirect:/auth/login"
        }
        model.addAttribute("user", userDetails)
        return "stock/chat-ui"
    }

    @PostMapping("/ask")
    @ResponseBody
    fun askChat(@RequestBody request: Map<String, String>): ResponseEntity<out Any> {
        val message = request["message"]
            ?: return ResponseEntity.badRequest().body(mapOf("reply" to "Missing 'message' parameter"))

        val trimmedMessage = message.trim()

        // Handle /help explicitly
        if (trimmedMessage.equals("/help", ignoreCase = true)) {
            val helpText = """
                Supported chat commands:
                - low_stock_analysis (threshold: Int, optional)
                - get_inactive_item_types
                - get_active_item_types
                - total_stock_count
                - search_by_type (type: String)
                - search_by_product_name (name: String)
                - search_by_item_type_name (typeName: String)
            """.trimIndent()
            return ResponseEntity.ok(mapOf("reply" to helpText))
        }

        // Build AI prompt
        val safeMessage = trimmedMessage.replace("\"", "\\\"")
        val instructionPrompt = """
            You are an inventory system assistant.
            Interpret the user message and return a JSON with:
            {
              "action": "<action_name>",
              // additional fields if needed
            }
            
            Supported actions:
            - low_stock_analysis (optional field "threshold" Int)
            - get_inactive_item_types
            - get_active_item_types
            - total_stock_count
            - search_by_type (field "type" String)
            - search_by_product_name (field "name" String)
            - search_by_item_type_name (field "typeName" String)
            
            User message: "$safeMessage"
        """.trimIndent()

        val aiResponse = chatAgentService.askAgent(instructionPrompt)

        if (!aiResponse.statusCode.is2xxSuccessful || aiResponse.body.isNullOrBlank()) {
            return ResponseEntity.status(500).body(mapOf("reply" to "Failed to get response from AI service"))
        }

        val json = try {
            jacksonObjectMapper().readTree(aiResponse.body)
        } catch (ex: Exception) {
            return ResponseEntity.status(500).body(mapOf("reply" to "Failed to parse AI response JSON: ${ex.message}"))
        }

        return when (val action = json["action"]?.asText()) {
            "low_stock_analysis" -> {
                val threshold = json["threshold"]?.asInt() ?: 10
                val result = productStockService.findByCountLessThan(threshold).map { it.toDTO() }
                ResponseEntity.ok(result)
            }

            "get_inactive_item_types" -> {
                val result = itemTypeService.findInactive().map { it.toDTO() }
                ResponseEntity.ok(result)
            }

            "get_active_item_types" -> {
                val result = itemTypeService.findByActivate().map { it.toDTO() }
                ResponseEntity.ok(result)
            }

            "total_stock_count" -> {
                val totalCount = productStockService.getTotalStockCount()
                ResponseEntity.ok(mapOf("totalStockCount" to totalCount))
            }

            "search_by_type" -> {
                val type = json["type"]?.asText() ?: return ResponseEntity.badRequest().body(mapOf("reply" to "Missing 'type' field"))
                val result = itemTypeService.findByType(type).map { it.toDTO() }
                ResponseEntity.ok(result)
            }

            "search_by_product_name" -> {
                val name = json["name"]?.asText() ?: return ResponseEntity.badRequest().body(mapOf("reply" to "Missing 'name' field"))
                val result = productStockService.findByNameContaining(name).map { it.toDTO() }
                ResponseEntity.ok(result)
            }

            "search_by_item_type_name" -> {
                val typeName = json["typeName"]?.asText() ?: return ResponseEntity.badRequest().body(mapOf("reply" to "Missing 'typeName' field"))
                val result = productStockService.findByItemTypeName(typeName).map { it.toDTO() }
                ResponseEntity.ok(result)
            }

            else -> {
                ResponseEntity.ok(mapOf("reply" to "Sorry, I didn't understand your request. Try /help for available commands."))
            }
        }
    }
}
