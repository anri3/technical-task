package com.example.books.service

import com.example.books.dto.AuthorRequest
import com.example.books.dto.RegisterBookRequest
import com.example.books.repository.AuthorsRepository
import com.example.books.repository.BooksRepository
import org.springframework.stereotype.Service

@Service
class RegisterBookService(
    private val booksRepository: BooksRepository,
    private val authorsRepository: AuthorsRepository
) {
    fun registerBook(request: RegisterBookRequest): Int {
        // 件数チェック(1件でもあれば登録しない)
        if (booksRepository.selectCountForExists(request) > 0) {
            throw IllegalArgumentException("データが既に存在します。")
        }
        // 登録
        return booksRepository.insert(request)
    }

    fun registerAuthor(request: AuthorRequest): Int {
        // 登録 同姓同名、同じ誕生日は許容
        return authorsRepository.insert(request)
    }
}