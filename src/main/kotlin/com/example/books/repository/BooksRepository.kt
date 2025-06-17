package com.example.books.repository

import com.example.books.dto.BookInfo
import com.example.books.dto.RegisterBookRequest
import com.example.books.dto.UpdateBookRequest
import com.example.jooq.generated.tables.Authors.AUTHORS
import com.example.jooq.generated.tables.Books.BOOKS
import com.example.jooq.generated.tables.BooksAuthors.BOOKS_AUTHORS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Repository
class BooksRepository(private val dsl: DSLContext) {
    // 著者と本のリクエスト値を元に検索して件数を返却する
    fun selectCountForExists(request: RegisterBookRequest): Int {
        var count = 0
        request.authorIds.forEach { authorId ->
            count += this.dsl
                .selectCount()
                .from(BOOKS)
                .join(BOOKS_AUTHORS).on(BOOKS.ID.eq(BOOKS_AUTHORS.BOOK_ID))
                .join(AUTHORS)
                .on(AUTHORS.ID.eq(BOOKS_AUTHORS.AUTHOR_ID))
                .and(AUTHORS.ID.eq(authorId))
                .where(BOOKS.TITLE.eq(request.title))
                .fetchOneInto(Int::class.java) ?: 0
        }
        return count
    }

    // 書籍idを元に検索して件数を返却する
    fun selectCountById(bookId: Int): Int {
        return this.dsl
            .selectCount()
            .from(BOOKS)
            .where(BOOKS.ID.eq(bookId))
            .fetchOneInto(Int::class.java) ?: 0
    }

    // 書籍idを元に検索して返却する
    fun selectByBookId(bookId: Int): BookInfo {
        val data =
            this.dsl
                .select(BOOKS.TITLE, BOOKS.PRICE, BOOKS.IS_PUBLISHED)
                .from(BOOKS)
                .where(BOOKS.ID.eq(bookId))
                .fetchOne()

        // nullチェック
        data ?: throw IllegalArgumentException("指定されたIDの本が見つかりません。")

        return BookInfo(
            title = data[BOOKS.TITLE]!!,
            price = data[BOOKS.PRICE]!!,
            isPublished = data[BOOKS.IS_PUBLISHED]!!
        )
    }

    // 登録
    @Transactional
    fun insert(request: RegisterBookRequest): Int {
        // books テーブルへ INSERT
        val bookId = dsl.insertInto(BOOKS)
            .set(BOOKS.TITLE, request.title)
            .set(BOOKS.PRICE, request.price)
            .set(BOOKS.IS_PUBLISHED, request.isPublished)
            .returning(BOOKS.ID)
            .fetchOne()
            ?.id ?: throw Exception("booksテーブルへの登録が失敗しました。")

        // authors テーブルへ INSERT
        request.authorIds.forEach { authorId ->
            // 既に登録されている著者の検索
            val author = dsl.selectFrom(AUTHORS)
                .where(AUTHORS.ID.eq(authorId))
                .fetchOne()

            // 著者がいなければエラー
           if (author == null) {
                throw Exception("指定の著者IDが存在しません。")
            } else {
                // books_authors テーブルへ INSERT
                this.dsl.insertInto(BOOKS_AUTHORS)
                    .set(BOOKS_AUTHORS.BOOK_ID, bookId)
                    .set(BOOKS_AUTHORS.AUTHOR_ID, authorId)
                    .execute()
            }
        }
        return bookId
    }

    // 更新
    @Transactional
    fun updateById(bookId: Int, request: UpdateBookRequest) {
        // books テーブルへ UPDATE
        this.dsl.update(BOOKS)
            .set(BOOKS.TITLE, request.title)
            .set(BOOKS.PRICE, request.price)
            .set(BOOKS.IS_PUBLISHED, request.isPublished)
            .set(BOOKS.UPDATED_AT, LocalDateTime.now())
            .where(BOOKS.ID.eq(bookId))
            .execute()
    }
}