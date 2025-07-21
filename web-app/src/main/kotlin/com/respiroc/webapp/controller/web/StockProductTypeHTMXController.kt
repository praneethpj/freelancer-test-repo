package com.respiroc.webapp.controller.web


import com.respiroc.domain.chat.stock.dto.ProductStockPayload
import com.respiroc.domain.chat.stock.dto.UpdateProductStockPayload
import com.respiroc.domain.chat.stock.service.ProductStockService
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
@RequestMapping("/product-stock")
class StockProductTypeHTMXController(
    private val productStockService: ProductStockService,
    private val itemTypeService: ItemTypeService
) {

    @GetMapping("/list")
    fun listProductStocks(
        model: Model,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "5") size: Int,
        @RequestParam("keyword", required = false) keyword: String?,
        @AuthenticationPrincipal userDetails: UserDetails?
    ): String {
        if (userDetails == null) return "redirect:/auth/login"

        val productStockPage = productStockService.getProductStocksByPageAndKeyword(page, size, keyword)

        model.addAttribute("user", userDetails)
        model.addAttribute("productStocks", productStockPage.content)
        model.addAttribute("currentPage", page)
        model.addAttribute("totalPages", productStockPage.totalPages)
        model.addAttribute("keyword", keyword)

        return "stock/product-stock/list-product-stock"
    }

    @GetMapping("/new")
    fun showNewProductForm(model: Model, @AuthenticationPrincipal user: UserDetails?): String {
        if (user == null) return "redirect:/auth/login"

        model.addAttribute("user", user)
        model.addAttribute("productStockPayload", ProductStockPayload(0, "", "", 0))
        model.addAttribute("itemTypes", itemTypeService.getItemTypesByPageAndKeyword(0, 100, null).content)
        return "stock/product-stock/create-product-stock-form"
    }

    @PostMapping
    fun createProductStock(
        @ModelAttribute("productStockPayload") @Valid productStockPayload: ProductStockPayload,
        result: BindingResult,
        model: Model,
        @AuthenticationPrincipal userDetails: UserDetails?
    ): String {
        if (userDetails == null) return "redirect:/auth/login"


        if (result.hasErrors()) {
            model.addAttribute("error", "Validation errors")
            model.addAttribute("user", userDetails)
            model.addAttribute("itemTypes", itemTypeService.getItemTypesByPageAndKeyword(0, 100, null).content)
            return "stock/product-stock/save-or-update-product-stock-form"
        }

        return try {
            productStockService.createProductStock(productStockPayload)
            "redirect:/product-stock/list"
        } catch (e: Exception) {
            model.addAttribute("error", e.message)
            model.addAttribute("user", userDetails)
            model.addAttribute("itemTypes", itemTypeService.getItemTypesByPageAndKeyword(0, 100, null).content)
            return "stock/product-stock/save-or-update-product-stock-form"
        }
    }


    @GetMapping("/edit/{id}")
    fun showEditForm(
        @PathVariable id: Int,
        model: Model,
        @AuthenticationPrincipal userDetails: UserDetails?
    ): String {
        if (userDetails == null) return "redirect:/auth/login"

        val productStock = productStockService.getProductStockById(id)
        model.addAttribute("user", userDetails)
        model.addAttribute("editing", true)
        model.addAttribute(
            "productStockPayload",
            UpdateProductStockPayload(
                id=productStock.id,
                name = productStock.name,
                description = productStock.description,
                itemTypeId = productStock.itemType?.id ?: 0,
                count = productStock.count ?: 0
            )
        )
        model.addAttribute("itemTypes", itemTypeService.getItemTypesByPageAndKeyword(0, 100, null).content)

        return "stock/product-stock/save-or-update-product-stock-form"
    }

    @PostMapping("/update/{id}")
    fun updateProductStock(
        @PathVariable id: Int,
        @ModelAttribute("productStockPayload") @Valid productStockPayload: ProductStockPayload,
        result: BindingResult,
        model: Model,
        @AuthenticationPrincipal userDetails: UserDetails?
    ): String {
        if (userDetails == null) return "redirect:/auth/login"

        if (result.hasErrors()) {
            model.addAttribute("error", "Validation errors")
            model.addAttribute("user", userDetails)
            model.addAttribute("editing", true)
            model.addAttribute("itemTypes", itemTypeService.getItemTypesByPageAndKeyword(0, 100, null).content)
            return "stock/product-stock/save-or-update-product-stock-form"
        }

        return try {
            productStockService.updateProductStock(id, productStockPayload)
            "redirect:/product-stock/list"
        } catch (ex: Exception) {
            model.addAttribute("error", ex.message)
            model.addAttribute("user", userDetails)
            model.addAttribute("editing", true)
            model.addAttribute("itemTypes", itemTypeService.getItemTypesByPageAndKeyword(0, 100, null).content)
            return "stock/product-stock/save-or-update-product-stock-form"
        }
    }

    @PostMapping("/delete/{id}")
    fun deleteProductStock(@PathVariable id: Int): String {
        productStockService.deleteProductStock(id)
        return "redirect:/product-stock/list"
    }

    @PostMapping("/import")
    fun importCSV(file: MultipartFile, redirectAttributes: RedirectAttributes): String {

        if (file.isEmpty) {

            redirectAttributes.addFlashAttribute("error", "Please select a CSV file.")
            return "redirect:/product-stock/list"
        }

        try {

            productStockService.importProductStocksFromCsv(file)
            redirectAttributes.addFlashAttribute("success", "CSV import completed.")
        } catch (e: Exception) {
            println(e)
            redirectAttributes.addFlashAttribute("error", "Error during import: ${e.message}")
        }

        return "redirect:/product-stock/list"
    }

}
