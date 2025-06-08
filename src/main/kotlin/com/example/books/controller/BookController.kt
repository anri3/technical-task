package com.example.books.controller

import com.example.books.dto.BookRequest
import com.example.books.service.BookService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.validation.BindingResult

@RestController
@RequestMapping("/book")
class BookController(private val bookService: BookService) {

    @PostMapping("/register")
    fun registerBook(@RequestBody @Valid request: BookRequest, bindingResult: BindingResult
    ): ResponseEntity<Any> {
        // バリデーションエラー処理
        if (bindingResult.hasErrors()) {
            val errors = bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
            return ResponseEntity.badRequest().body(mapOf("errors" to errors))
        }

        return try {
            // 正常登録してidを返却する
            val id = bookService.registerBook(request)
            ResponseEntity.ok(mapOf("message" to "登録完了しました。", "ID" to id))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "登録に失敗しました。"))
        }
    }
}