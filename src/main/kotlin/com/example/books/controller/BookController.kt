package com.example.books.controller

import com.example.books.controller.validation.UpdateValidation
import com.example.books.dto.AuthorRequest
import com.example.books.dto.AuthorResponse
import com.example.books.dto.BookRequest
import com.example.books.service.RegisterBookService
import com.example.books.service.SelectBookService
import com.example.books.service.UpdateBookService
import jakarta.validation.Valid
import jakarta.validation.Validator
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/book")
class BookController(private val registerBookService: RegisterBookService,
                     private val updateBookService: UpdateBookService,
                     private val selectBookService: SelectBookService,
                     private val validator: Validator) {

    // 書籍情報登録API
    @PostMapping("/info")
    fun registerBook(@RequestBody @Valid request: BookRequest, bindingResult: BindingResult
    ): ResponseEntity<Any> {
        // バリデーションエラー処理
        if (bindingResult.hasErrors()) {
            val errors = bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
            return ResponseEntity.badRequest().body(mapOf("errors" to errors))
        }

        return try {
            // 正常登録してidを返却する
            val id = registerBookService.registerBook(request)
            ResponseEntity.ok(mapOf("message" to "登録完了しました。", "ID" to id))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "登録に失敗しました。"))
        }
    }

    // 書籍情報更新API
    @PostMapping("/info/{id}")
    fun updateBook(@PathVariable("id") id: Int, @RequestBody @Valid request: BookRequest, bindingResult: BindingResult
    ): ResponseEntity<Any> {
        // バリデーションエラー処理
        if (bindingResult.hasErrors()) {
            val errors = bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
            return ResponseEntity.badRequest().body(mapOf("errors" to errors))
        }

        // 更新時のバリデーションチェック
        val violations = validator.validate(request, UpdateValidation::class.java)
        if (violations.isNotEmpty()) {
            return ResponseEntity.badRequest().body(mapOf("error" to violations.first().message))
        }

        return try {
            // 正常更新してidを返却する
            updateBookService.updateBook(id, request)
            ResponseEntity.ok(mapOf("message" to "更新完了しました。", "ID" to id))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "更新に失敗しました。"))
        }
    }

    // 著者更新API
    @PostMapping("/author/{id}")
    fun updateAuthor(@PathVariable("id") id: Int, @RequestBody @Valid request: AuthorRequest, bindingResult: BindingResult
    ): ResponseEntity<Any> {
        // バリデーションエラー処理
        if (bindingResult.hasErrors()) {
            val errors = bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
            return ResponseEntity.badRequest().body(mapOf("errors" to errors))
        }

        return try {
            // 正常更新してidを返却する
            updateBookService.updateAuthor(id, request)
            ResponseEntity.ok(mapOf("message" to "更新完了しました。", "AUTHOR_ID" to id))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "更新に失敗しました。"))
        }
    }

    // 著者情報参照API
    @PostMapping("/author/info")
    fun selectBooks(@RequestBody request: Map<String, String>
    ): ResponseEntity<Any> {
        val name = request["name"]
        // バリデーションエラー処理
        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest().body(mapOf("errors" to "検索条件がありません。"))
        }

        return try {
            // 書籍のリストを返却する
            val list: AuthorResponse = selectBookService.selectBooks(name)
            ResponseEntity.ok(mapOf("message" to "参照完了しました。", "LIST" to list))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "参照に失敗しました。"))
        }
    }
}