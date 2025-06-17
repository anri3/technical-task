package com.example.books.repository

import com.example.books.dto.AuthorRequest
import com.example.jooq.generated.Tables.AUTHORS
import org.jooq.DSLContext
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jooq.JooqTest
import org.springframework.test.context.jdbc.Sql
import java.time.DateTimeException
import java.time.LocalDate
import java.time.LocalDateTime

@JooqTest
@Sql("/test-data.sql")
class AuthorsRepositoryTest {

    @Autowired
    lateinit var dsl: DSLContext

    lateinit var repository: AuthorsRepository

    lateinit var booksAuthorsrepository: BooksAuthorsRepository

    @BeforeEach
    fun setup() {
        repository = AuthorsRepository(dsl)
        booksAuthorsrepository = BooksAuthorsRepository(dsl)
    }

    @Test
    @DisplayName("指定のauthor_idの件数が返却されること")
    fun `selectCountById should return count`() {
        val expected = 1
        val actual = repository.selectCountById(1)

        assertEquals(expected, actual)
    }

    @Test
    @DisplayName("存在しないauthor_idを指定して0が返却されること")
    fun `selectCountById should return 0`() {
        val expected = 0
        val actual = repository.selectCountById(5)

        assertEquals(expected, actual)
    }

    @Test
    @DisplayName("書籍IDのリストが返却されること")
    fun `selectIdByName should return books ids`() {
        val expected = listOf(1, 2)
        val actual = repository.selectIdByName("テスト著者2")

        assertEquals(expected, actual)
    }

    @Test
    @DisplayName("存在しない著者名でemptyチェック")
    fun `selectIdByName should return empty list`() {
        val actual = repository.selectIdByName("テスト")

        assertEquals(emptyList<Int>(), actual)
    }

    @Test
    @DisplayName("新しい著者が登録されること")
    fun `should insert new author and associate with books_authors`() {
        // 登録前の確認
        assertEquals(emptyList<Int>(), repository.selectIdByName("Test"))

        // 登録実行
        val number = repository.insert(AuthorRequest("Test", LocalDate.of(1988, 1, 1)))

        // 登録後の確認
        val afterExpected = 1
        assertEquals(afterExpected, repository.selectCountById(number))
    }

    @Test
    @DisplayName("登録失敗でスローされること")
    fun `insert should return throw if birthday is wrong value`() {
        // 登録実行
        val exception = assertThrows<DateTimeException> {
            repository.insert(AuthorRequest("Test", LocalDate.of(1988, 1, 50)))
        }
        assertEquals("Invalid value for DayOfMonth (valid values 1 - 28/31): 50", exception.message)
    }

    // 更新用テストのための著者取得メソッド追加
    fun getAuthorById(authorId: Int): AuthorRequest? {
        return dsl.select(AUTHORS.ID, AUTHORS.NAME, AUTHORS.BIRTHDAY)
            .from(AUTHORS)
            .where(AUTHORS.ID.eq(authorId))
            .fetchOne { record ->
                AuthorRequest(
                    name = record[AUTHORS.NAME],
                    birthday = record[AUTHORS.BIRTHDAY]
                )
            }
    }

    // 更新用テストのためのUPDATED_AT取得メソッド追加
    fun getUpdatedAt(authorId: Int): LocalDateTime? {
        return dsl.select(AUTHORS.UPDATED_AT)
            .from(AUTHORS)
            .where(AUTHORS.ID.eq(authorId))
            .fetchOne(AUTHORS.UPDATED_AT)
    }

    @Test
    @DisplayName("既存の著者が正しく更新されること")
    fun `updateById(id, request) should update existing author information correctly`() {
        val authorIdToUpdate = 1
        val newName = "テスト著者 更新"
        val newBirthday = LocalDate.of(1995, 2, 15)
        val request = AuthorRequest(newName, newBirthday)

        // 更新前の確認
        val originalAuthor = getAuthorById(authorIdToUpdate)
        assertNotNull(originalAuthor)
        assertEquals("テスト著者", originalAuthor?.name)
        assertEquals(LocalDate.of(1995, 1, 1), originalAuthor?.birthday)
        val originalUpdatedAt = getUpdatedAt(authorIdToUpdate)

        // 更新実行
        repository.updateById(authorIdToUpdate, request)

        // 更新後の確認
        val updatedAuthor = getAuthorById(authorIdToUpdate)
        assertNotNull(updatedAuthor)
        assertEquals(newName, updatedAuthor?.name)
        assertEquals(newBirthday, updatedAuthor?.birthday)

        val updatedUpdatedAt = getUpdatedAt(authorIdToUpdate)
        assertTrue(updatedUpdatedAt!!.isAfter(originalUpdatedAt), "UPDATED_AT should be updated to a later time")
    }

    @Test
    @DisplayName("存在しないIDを指定した場合に何も更新されないこと")
    fun `updateById(id, request) should not update anything if non-existent ID is specified`() {
        val id = 999
        val newName = "テスト"
        val newBirthday = LocalDate.of(2000, 1, 1)
        val request = AuthorRequest(newName, newBirthday)

        // 更新実行
        repository.updateById(id, request)

        // 存在しないIDの著者情報が取得できないこと
        assertNull(getAuthorById(id))

        // 既存のレコードが変更されていないことを確認
        val originalAuthor1 = getAuthorById(1)
        assertNotNull(originalAuthor1)
        assertEquals("テスト著者", originalAuthor1?.name)
        assertEquals(LocalDate.of(1995, 1, 1), originalAuthor1?.birthday)

        val originalAuthor2 = getAuthorById(2)
        assertNotNull(originalAuthor2)
        assertEquals("テスト著者2", originalAuthor2?.name)
        assertEquals(LocalDate.of(1995, 2, 1), originalAuthor2?.birthday)

        val originalAuthor3 = getAuthorById(3)
        assertNotNull(originalAuthor3)
        assertEquals("テスト著者3", originalAuthor3?.name)
        assertEquals(LocalDate.of(1995, 3, 1), originalAuthor3?.birthday)

        val originalAuthor4 = getAuthorById(4)
        assertNotNull(originalAuthor4)
        assertEquals("テスト著者4", originalAuthor4?.name)
        assertEquals(LocalDate.of(1995, 4, 1), originalAuthor4?.birthday)
    }

    @Test
    @DisplayName("更新対象のIDが0の場合に更新が行われないこと")
    fun `updateById(id, request) should not update anything if authorId is 0`() {
        val id = 0
        val newName = "テスト"
        val newBirthday = LocalDate.of(2000, 1, 1)
        val request = AuthorRequest(newName, newBirthday)

        repository.updateById(id, request)

        assertNull(getAuthorById(id))
    }
}
