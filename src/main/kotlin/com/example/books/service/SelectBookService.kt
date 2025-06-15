package com.example.books.service

import com.example.books.dto.AuthorResponse
import com.example.books.dto.BookInfo
import com.example.books.repository.AuthorsRepository
import com.example.books.repository.BooksRepository
import org.springframework.stereotype.Service

@Service
class SelectBookService (
    private val booksRepository: BooksRepository,
    private val authorsRepository: AuthorsRepository
) {

    // 著者の書籍情報取得
    fun selectBooks(name: String): AuthorResponse {
        // 名前検索
        val bookIdList: List<Int> = authorsRepository.selectIdByName(name)

        if (bookIdList.isEmpty()) {
            throw IllegalArgumentException("検索条件が存在しませんでした。")
        }

        val list = mutableListOf<BookInfo>()

        // 著者に紐づく本の情報を取得する
        for(bookId in bookIdList) {
            val book: BookInfo = booksRepository.selectByBookId(bookId)
            list.add(book)
        }

        return AuthorResponse(list)
    }
}
