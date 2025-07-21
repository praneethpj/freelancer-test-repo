package com.respiroc.webapp.controller.web

import com.respiroc.domain.chat.stock.dto.ItemTypePayload

import com.respiroc.domain.chat.stock.dto.UpdateItemPayload
import com.respiroc.domain.chat.stock.service.ItemTypeService
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/item-type")
class StockItemTypeHTMXController(
    private val itemTypeService: ItemTypeService
) {

    @GetMapping("/list")
    fun listItemTypes(
        model: Model,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "5") size: Int,
        @RequestParam("keyword", required = false) keyword: String?,
        @AuthenticationPrincipal userDetails: UserDetails?
    ): String {

        if (userDetails == null) return "redirect:/auth/login"

        val itemTypePage = itemTypeService.getItemTypesByPageAndKeyword(page, size, keyword)

        model.addAttribute("user", userDetails)
        model.addAttribute("itemTypes", itemTypePage.content)
        model.addAttribute("currentPage", page)
        model.addAttribute("totalPages", itemTypePage.totalPages)
        model.addAttribute("keyword", keyword)

        return "stock/item-type/list-item-type"
    }


    @GetMapping("/new")
    fun showNewItemTypeForm(model: Model, @AuthenticationPrincipal user: UserDetails?): String {
        if (user == null) return "redirect:/auth/login"

        model.addAttribute("user", user)
        model.addAttribute("itemTypePayload", ItemTypePayload())
        return "stock/item-type/create-item-type-form"
    }

    @PostMapping
    fun createItemType(
        @ModelAttribute("itemTypePayload") @Valid itemTypePayload: ItemTypePayload,
        result: BindingResult,
        model: Model,
        @AuthenticationPrincipal userDetails: UserDetails?
    ): String {
        if (userDetails == null) return "redirect:/auth/login"

        if (result.hasErrors()) {
            model.addAttribute("error", "Validation errors")
            model.addAttribute("user", userDetails)
            return "stock/item-type/save-or-update-itemtype-form"
        }

        return try {
            itemTypeService.createItemType(itemTypePayload)
            "redirect:/item-type/list"
        } catch (e: IllegalArgumentException) {
            model.addAttribute("error", e.message)
            model.addAttribute("user", userDetails)
            "stock/item-type/save-or-update-itemtype-form"
        }
    }

    @GetMapping("/edit/{id}")
    fun showEditForm(
        @PathVariable id: Int,
        model: Model,
        @AuthenticationPrincipal userDetails: UserDetails?
    ): String {
        if (userDetails == null) return "redirect:/auth/login"

        val itemType = itemTypeService.getItemTypeById(id) ?: return "redirect:/item-type/list"

        model.addAttribute("user", userDetails)
        model.addAttribute(
            "itemTypePayload",
            UpdateItemPayload(
                name = itemType.name,
                itemTypeId = itemType.id,
                activate = itemType.activate,
                type = itemType.type ?: "Normal"
            )
        )
        model.addAttribute("editing", true)


        return "stock/item-type/save-or-update-itemtype-form"
    }

    @PostMapping("/update/{id}")
    fun updateItemType(
        @PathVariable id: Int,
        @ModelAttribute("itemTypePayload") @Valid itemTypePayload: ItemTypePayload,
        result: BindingResult,
        model: Model,
        @AuthenticationPrincipal userDetails: UserDetails?
    ): String {
        if (userDetails == null) return "redirect:/auth/login"

        if (result.hasErrors()) {
            model.addAttribute("error", "Validation errors")
            model.addAttribute("user", userDetails)
            model.addAttribute("editing", true)
            return "stock/item-type/save-or-update-itemtype-form"
        }

        return try {
            itemTypeService.updateItemType(id, itemTypePayload)
            "redirect:/item-type/list"
        } catch (ex: Exception) {
            model.addAttribute("error", ex.message)
            model.addAttribute("user", userDetails)
            model.addAttribute("editing", true)
            return "stock/save-or-update-itemtype-form"
        }
    }

    @PostMapping("/toggle/{id}")
    fun toggleStatus(@PathVariable id: Int): String {
        itemTypeService.toggleStatus(id)
        return "redirect:/item-type/list"
    }


    @PostMapping("/import")
    fun importCSV(file: MultipartFile, redirectAttributes: RedirectAttributes): String {
        if (file.isEmpty) {
            redirectAttributes.addFlashAttribute("error", "Please select a CSV file.")
            return "redirect:/item-types/list"
        }

        try {
            itemTypeService.importFromCsv(file)
            redirectAttributes.addFlashAttribute("success", "CSV import completed.")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", "Error during import: ${e.message}")
        }

        return "redirect:/item-type/list"
    }

}
