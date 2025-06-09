package com.example.books.repository

import com.example.jooq.generated.tables.BooksAuthors.BOOKS_AUTHORS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class BooksAuthorsRepository(private val dsl: DSLContext) {

    // 著者IDの一覧取得
    fun findAuthorIds(bookId: Int): List<Int> {
        return this.dsl.select(BOOKS_AUTHORS.AUTHOR_ID)
            .from(BOOKS_AUTHORS)
            .where(BOOKS_AUTHORS.BOOK_ID.eq(bookId))
            .fetch(BOOKS_AUTHORS.AUTHOR_ID)
    }

    // 削除
    @Transactional
    fun deleteByBookIdAndAuthorId(bookId: Int, authorId: Int) {
        this.dsl.deleteFrom(BOOKS_AUTHORS)
            .where(BOOKS_AUTHORS.BOOK_ID.eq(bookId))
            .and(BOOKS_AUTHORS.AUTHOR_ID.eq(authorId))
            .execute()
    }

}