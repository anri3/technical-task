package com.example.books.repository

import com.example.books.dto.AuthorRequest
import com.example.jooq.generated.tables.Authors.AUTHORS
import com.example.jooq.generated.tables.Books.BOOKS
import com.example.jooq.generated.tables.BooksAuthors.BOOKS_AUTHORS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Repository
class AuthorsRepository(private val dsl: DSLContext) {
    // 参照
    fun selectCountById(authorId: Int): Int {
        // authors テーブルへの参照
        return this.dsl
            .selectCount()
            .from(AUTHORS)
            .where(AUTHORS.ID.eq(authorId))
            .fetchOneInto(Int::class.java) ?: 0
    }

    // 名前からIDを取得
    fun selectIdByName(name: String): List<Int> {
        // authors テーブルと中間テーブルへの参照
        return this.dsl.select(BOOKS_AUTHORS.BOOK_ID)
            .from(BOOKS_AUTHORS)
            .join(AUTHORS).on(BOOKS_AUTHORS.AUTHOR_ID.eq(AUTHORS.ID))
            .where(AUTHORS.NAME.eq(name))
            .fetch(BOOKS_AUTHORS.BOOK_ID)
    }

    // 登録
    @Transactional
    fun insert(request: AuthorRequest): Int {
        // authors テーブルへ登録
        return this.dsl.insertInto(AUTHORS)
            .set(AUTHORS.NAME, request.name)
            .set(AUTHORS.BIRTHDAY, request.birthday)
            .returning(AUTHORS.ID)
            .fetchOne()
            ?.id ?: throw IllegalStateException("authorsテーブルへの登録が失敗しました。")
    }

    // 更新(id別)
    @Transactional
    fun updateById(authorId: Int, request: AuthorRequest) {
        // authors テーブルへ UPDATE
        this.dsl.update(AUTHORS)
            .set(AUTHORS.NAME, request.name)
            .set(AUTHORS.BIRTHDAY, request.birthday)
            .set(BOOKS.UPDATED_AT, LocalDateTime.now())
            .where(AUTHORS.ID.eq(authorId))
            .execute()
    }
}