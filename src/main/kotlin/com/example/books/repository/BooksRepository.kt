package com.example.books.repository

import com.example.books.dto.BookRequest
import com.example.jooq.generated.tables.Authors.AUTHORS
import com.example.jooq.generated.tables.Books.BOOKS
import com.example.jooq.generated.tables.BooksAuthors.BOOKS_AUTHORS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class BooksRepository(private val dsl: DSLContext) {

    // 著者と本のリクエスト値を元に検索して件数を返却する
    fun selectCountForExists(request: BookRequest): Int {
        var count = 0
        request.authors.forEach { author ->
            count = this.dsl
                .selectCount()
                .from(BOOKS)
                .join(BOOKS_AUTHORS).on(BOOKS.ID.eq(BOOKS_AUTHORS.BOOK_ID))
                .join(AUTHORS)
                .on(AUTHORS.ID.eq(BOOKS_AUTHORS.AUTHOR_ID))
                .and(AUTHORS.NAME.eq(author.name))
                .and(AUTHORS.BIRTHDAY.eq(author.birthday))
                .where(BOOKS.TITLE.eq(request.title))
                .fetchOneInto(Int::class.java) ?: 0
        }
        return count
    }

    @Transactional
    fun insert(request: BookRequest): Int {
        // books テーブルへ INSERT
        val bookId = dsl.insertInto(BOOKS)
            .set(BOOKS.TITLE, request.title)
            .set(BOOKS.PRICE, request.price)
            .set(BOOKS.IS_PUBLISHED, request.isPublished)
            .returning(BOOKS.ID)
            .fetchOne()
            ?.id ?: throw IllegalStateException("booksテーブルへの登録が失敗しました。")

        // authors テーブルへ INSERT
        request.authors.forEach { authorRequest ->
            // 既に登録されている著者の検索
            val author = dsl.selectFrom(AUTHORS)
                .where(AUTHORS.NAME.eq(authorRequest.name))
                .and(AUTHORS.BIRTHDAY.eq(authorRequest.birthday))
                .fetchOne()

            // 著者がいれば
            val authorId = if (author != null) {
                author.id
            } else {
                dsl.insertInto(AUTHORS)
                    .set(AUTHORS.NAME, authorRequest.name)
                    .set(AUTHORS.BIRTHDAY, authorRequest.birthday)
                    .returning(AUTHORS.ID)
                    .fetchOne()
                    ?.id ?: throw IllegalStateException("authorsテーブルへの登録が失敗しました。")
            }

            // books_authors テーブルへ INSERT
            dsl.insertInto(BOOKS_AUTHORS)
                .set(BOOKS_AUTHORS.BOOK_ID, bookId)
                .set(BOOKS_AUTHORS.AUTHOR_ID, authorId)
                .execute()
        }
        return bookId
    }
}