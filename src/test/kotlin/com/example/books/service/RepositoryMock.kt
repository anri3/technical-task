package com.example.books.service

import com.example.books.dto.AuthorRequest
import com.example.books.dto.BookInfo
import com.example.books.dto.RegisterBookRequest
import com.example.books.dto.UpdateBookRequest

// テストではモックで代替するので、ここではダミー実装

// AuthorsRepository モック
class AuthorsRepository {
    fun insert(request: AuthorRequest): Int { throw UnsupportedOperationException("Not implemented for mock") }
    fun updateById(authorId: Int, request: AuthorRequest) { throw UnsupportedOperationException("Not implemented for mock") }
    fun selectCountById(authorId: Int): Int { throw UnsupportedOperationException("Not implemented for mock") }
    fun selectIdByName(name: String): List<Int> { return emptyList() }
}

// BooksRepository モック
class BooksRepository {
    fun selectCountForExists(request: RegisterBookRequest): Int { throw UnsupportedOperationException("Not implemented for mock") }
    fun selectCountById(bookId: Int): Int { throw UnsupportedOperationException("Not implemented for mock") }
    fun selectByBookId(bookId: Int): BookInfo { throw UnsupportedOperationException("Not implemented for mock") }
    fun insert(request: RegisterBookRequest): Int { throw UnsupportedOperationException("Not implemented for mock") }
    fun updateById(bookId: Int, request: UpdateBookRequest) { throw UnsupportedOperationException("Not implemented for mock") }
}

// BooksAuthorsRepository モック
class BooksAuthorsRepository {
    fun findAuthorIds(bookId: Int): List<Int> { throw UnsupportedOperationException("Not implemented for mock") }
    fun insert(bookId: Int, authorId: Int): Int { throw UnsupportedOperationException("Not implemented for mock") }
    fun deleteByBookIdAndAuthorId(bookId: Int, authorId: Int) { throw UnsupportedOperationException("Not implemented for mock") }
}