package com.example.books.repository

import org.jooq.DSLContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jooq.JooqTest
import org.springframework.test.context.jdbc.Sql

@JooqTest
@Sql("/test-data-books-authors-repository.sql")
class BooksAuthorsRepositoryTest {

    @Autowired
    lateinit var dsl: DSLContext

    lateinit var repository: BooksAuthorsRepository

    @BeforeEach
    fun setup() {
        repository = BooksAuthorsRepository(dsl)
    }

    @Test
    fun `findAuthorIds should return author ids`() {
        val expected = listOf(1, 2)
        val actual = repository.findAuthorIds(1)

        assertEquals(expected, actual)
    }

    @Test
    fun `findAuthorIds return empty list`() {
        val actual = repository.findAuthorIds(3)

        assertEquals(emptyList<Int>(), actual)
    }
}
