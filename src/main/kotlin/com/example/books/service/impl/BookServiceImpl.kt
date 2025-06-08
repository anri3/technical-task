package com.example.books.service.impl

import com.example.books.dto.BookRequest
import com.example.books.repository.BooksRepository
import com.example.books.service.BookService
import org.springframework.stereotype.Service

@Service
class BookServiceImpl(
    private val booksRepository: BooksRepository
) : BookService {

    override fun registerBook(request: BookRequest): Int {
        // 件数チェック(1件でもあれば登録しない)
        if (booksRepository.selectCountForExists(request) > 0) {
            throw IllegalArgumentException("データが既に存在します。")
        }

        // 登録
        return booksRepository.insert(request)
    }
}