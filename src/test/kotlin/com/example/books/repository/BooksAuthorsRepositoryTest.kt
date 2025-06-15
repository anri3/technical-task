package com.example.books.repository

import org.jooq.DSLContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jooq.JooqTest
import org.springframework.test.context.jdbc.Sql

@JooqTest
@Sql("/test-data.sql")
class BooksAuthorsRepositoryTest {

    @Autowired
    lateinit var dsl: DSLContext

    lateinit var repository: BooksAuthorsRepository

    @BeforeEach
    fun setup() {
        repository = BooksAuthorsRepository(dsl)
    }

    @Test
    @DisplayName("指定のauthor_idのリストが返却されること")
    fun `findAuthorIds should return author ids`() {
        val expected = listOf(1, 2)
        val actual = repository.findAuthorIds(1)

        assertEquals(expected, actual)
    }

    @Test
    @DisplayName("存在しないbook_idでemptyチェック")
    fun `findAuthorIds return empty list`() {
        val actual = repository.findAuthorIds(4)

        assertEquals(emptyList<Int>(), actual)
    }

    @Test
    @DisplayName("指定したbook_idとauthor_idのレコードが削除")
    fun `deleteByBookIdAndAuthorId should delete`() {
        repository.deleteByBookIdAndAuthorId(2,3)

        val expected = listOf(2)
        val actual = repository.findAuthorIds(2)

        assertEquals(expected, actual)
    }

    @Test
    @DisplayName("存在しないbook_idとauthor_idで削除されないこと")
    fun `deleteByBookIdAndAuthorId should not delete`() {
        repository.deleteByBookIdAndAuthorId(4,5)

        val actual = repository.findAuthorIds(4)

        assertEquals(emptyList<Int>(), actual)
    }

    @Test
    @DisplayName("存在しないbook_idと存在するauthor_idで削除されないこと")
    fun `deleteByBookIdAndAuthorId should not delete if book_id is null`() {
        repository.deleteByBookIdAndAuthorId(4,4)

        val expected = listOf(4)
        val actual = repository.findAuthorIds(3)

        assertEquals(expected, actual)
    }

    @Test
    @DisplayName("存在するbook_idと存在しないauthor_idで削除されないこと")
    fun `deleteByBookIdAndAuthorId should not delete if author_id is null`() {
        repository.deleteByBookIdAndAuthorId(3,5)

        val expected = listOf(4)
        val actual = repository.findAuthorIds(3)

        assertEquals(expected, actual)
    }
}
