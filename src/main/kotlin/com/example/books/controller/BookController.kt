package com.example.books.controller

import com.example.books.dto.AuthorRequest
import com.example.books.dto.BookAuthorsRequest
import com.example.books.dto.RegisterBookRequest
import com.example.books.dto.UpdateBookRequest
import com.example.books.service.RegisterBookService
import com.example.books.service.SelectBookService
import com.example.books.service.UpdateBookService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/book")
class BookController(private val registerBookService: RegisterBookService,
                     private val updateBookService: UpdateBookService,
                     private val selectBookService: SelectBookService) {

    // 1.書籍情報登録API
    @PostMapping("/register")
    fun registerBook(@RequestBody @Valid request: RegisterBookRequest, bindingResult: BindingResult
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

    // 2.著者登録API
    @PostMapping("/author/register")
    fun registerAuthor(@RequestBody @Valid request: AuthorRequest, bindingResult: BindingResult
    ): ResponseEntity<Any> {
        // バリデーションエラー処理
        if (bindingResult.hasErrors()) {
            val errors = bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
            return ResponseEntity.badRequest().body(mapOf("errors" to errors))
        }

        return try {
            // 正常登録してidを返却する
            val id = registerBookService.registerAuthor(request)
            ResponseEntity.ok(mapOf("message" to "登録完了しました。", "ID" to id))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "登録に失敗しました。"))
        }
    }

    // 3.書籍更新API
    @PostMapping("/update/{id}")
    fun updateBook(@PathVariable("id") id: Int, @RequestBody @Valid request: UpdateBookRequest, bindingResult: BindingResult
    ): ResponseEntity<Any> {
        // バリデーションエラー処理
        if (bindingResult.hasErrors()) {
            val errors = bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
            return ResponseEntity.badRequest().body(mapOf("errors" to errors))
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

    // 4.著者更新API
    @PostMapping("/author/update/{id}")
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

    // 5.書籍の著者更新API
    @PostMapping("/author/update/{id}/sync")
    fun updateBookAuthor(@PathVariable("id") id: Int, @RequestBody @Valid request: BookAuthorsRequest, bindingResult: BindingResult
    ): ResponseEntity<Any> {
        // バリデーションエラー処理
        if (bindingResult.hasErrors()) {
            val errors = bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
            return ResponseEntity.badRequest().body(mapOf("errors" to errors))
        }

        return try {
            // 正常更新してidを返却する
            updateBookService.updateBookAuthors(id, request)
            ResponseEntity.ok(mapOf("message" to "更新完了しました。", "ID" to id))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "更新に失敗しました。"))
        }
    }

    // 6.著者の書籍参照API
    @GetMapping("/author/info")
    fun selectBooks(@RequestParam("name") name: String
    ): ResponseEntity<Any> {
        // バリデーションエラー処理
        if (name.isEmpty()) {
            return ResponseEntity.badRequest().body(mapOf("errors" to "検索条件がありません。"))
        }

        return try {
            // 書籍のリストを返却する
            ResponseEntity.ok(mapOf("message" to "参照完了しました。", "list" to selectBookService.selectBooks(name)))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "参照に失敗しました。"))
        }
    }
}