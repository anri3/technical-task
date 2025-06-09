package com.example.books.service

import com.example.books.dto.BookRequest
import com.example.books.repository.BooksRepository
import org.springframework.stereotype.Service

@Service
class RegisterBookService(
    private val booksRepository: BooksRepository
) {
    fun registerBook(request: BookRequest): Int {
        // 件数チェック(1件でもあれば登録しない)
        if (booksRepository.selectCountForExists(request) > 0) {
            throw IllegalArgumentException("データが既に存在します。")
        }

        // 登録
        return booksRepository.insert(request)
    }
}