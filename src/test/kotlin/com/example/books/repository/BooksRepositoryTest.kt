package com.example.books.repository

import com.example.books.dto.BookInfo
import com.example.books.dto.RegisterBookRequest
import com.example.books.dto.UpdateBookRequest
import com.example.jooq.generated.tables.Books.BOOKS
import com.example.jooq.generated.tables.BooksAuthors.BOOKS_AUTHORS
import org.jooq.DSLContext
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jooq.JooqTest
import org.springframework.test.context.jdbc.Sql
import java.time.LocalDateTime

@JooqTest
@Sql("/test-data.sql")
class BooksRepositoryTest {

    @Autowired
    lateinit var dsl: DSLContext

    lateinit var booksRepository: BooksRepository

    lateinit var authorsRepository: AuthorsRepository

    lateinit var booksAuthorsrepository: BooksAuthorsRepository

    @BeforeEach
    fun setup() {
        booksRepository = BooksRepository(dsl)
        authorsRepository = AuthorsRepository(dsl)
        booksAuthorsrepository = BooksAuthorsRepository(dsl)
    }

    @Test
    @DisplayName("リクエストの著者1人で1人登録されていて件数が返却されること")
    fun `selectCountForExists should return count 1 by 1 request`() {
        val title = "テストブック3"
        val price: Long = 3000
        val published = false
        val authorIds: List<Int> = listOf(4)
        val bookRequest = RegisterBookRequest(title, price, published, authorIds)

        val expected = 1
        val actual = booksRepository.selectCountForExists(bookRequest)

        assertEquals(expected, actual)
    }

    @Test
    @DisplayName("リクエストの著者2人で1人登録されていて件数が返却されること")
    fun `selectCountForExists should return count 1 by 2 request`() {
        val title = "テストブック"
        val price: Long = 1000
        val published = true
        val authorIds: List<Int> = listOf(1,2)
        val bookRequest = RegisterBookRequest(title, price, published, authorIds)

        val expected = 2
        val actual = booksRepository.selectCountForExists(bookRequest)

        assertEquals(expected, actual)
    }

    @Test
    @DisplayName("リクエストの著者2人でどなたもDB登録なしで件数が返却されること")
    fun `selectCountForExists should return count 0 by 2 request`() {
        val title = "テストブック"
        val price: Long = 1000
        val published = true
        val authorIds: List<Int> = listOf(3,4)
        val bookRequest = RegisterBookRequest(title, price, published, authorIds)

        val expected = 0
        val actual = booksRepository.selectCountForExists(bookRequest)

        assertEquals(expected, actual)
    }

    @Test
    @DisplayName("存在するbook_idを指定して1が返却されること")
    fun `selectCountById should return 1`() {
        val expected = 1
        val actual = booksRepository.selectCountById(2)

        assertEquals(expected, actual)
    }

    @Test
    @DisplayName("存在しないbook_idを指定して0が返却されること")
    fun `selectCountById should return 0`() {
        val expected = 0
        val actual = booksRepository.selectCountById(4)

        assertEquals(expected, actual)
    }

    @Test
    @DisplayName("存在するbook_idを指定して書籍情報が返却されること")
    fun `selectByBookId should return info`() {
        val actual = booksRepository.selectByBookId(1)
        val exTitle = "テストブック"
        val exPrice: Long = 1000
        val exIsPublished = true
        val exBookInfo = BookInfo(exTitle, exPrice, exIsPublished)
        assertEquals(exBookInfo, actual)
    }

    @Test
    @DisplayName("存在しないbook_idを指定して例外が返却されること")
    fun `selectByBookId should return error`() {
        val exception = assertThrows<IllegalArgumentException> {
            booksRepository.selectByBookId(4)
        }
        assertEquals("指定されたIDの本が見つかりません。", exception.message)
    }

    // 登録用テストのための書籍情報取得メソッド追加
    fun getBookByTitle(title: String): BookInfo? {
        return dsl.select(BOOKS.TITLE, BOOKS.PRICE, BOOKS.IS_PUBLISHED)
            .from(BOOKS)
            .where(BOOKS.TITLE.eq(title))
            .fetchOne { record ->
                BookInfo(
                    title = record[BOOKS.TITLE],
                    price = record[BOOKS.PRICE],
                    isPublished = record[BOOKS.IS_PUBLISHED]
                )
            }
    }

    // 登録用テストのための著者取得メソッド追加
    fun getCountAuthorsByBookTitle(title: String): Int? {
        return dsl.selectCount()
            .from(BOOKS_AUTHORS)
            .join(BOOKS).on(BOOKS.ID.eq(BOOKS_AUTHORS.BOOK_ID))
            .where(BOOKS.TITLE.eq(title))
            .fetchOneInto(Int::class.java) ?: 0
    }

    @Test
    @DisplayName("新しい書籍情報が登録されること")
    fun `should insert new book and associate with books_authors and new authors`() {
        val title = "テストブック4"
        val price: Long = 4000
        val published = false
        val authorIds: List<Int> = listOf(3,4)
        val bookRequest = RegisterBookRequest(title, price, published, authorIds)

        // 登録実行
        booksRepository.insert(bookRequest)

        // 登録後の確認
        val exBookInfo = BookInfo(title, price, published)
        assertEquals(exBookInfo, getBookByTitle(title))
        assertEquals(2, getCountAuthorsByBookTitle("テストブック4"))
    }

    @Test
    @DisplayName("DBに存在しない著者IDをリクエストしてエラーになること")
    fun `should not insert and return error if not exists authorIds`() {
        val title = "テストブック4"
        val price: Long = 4000
        val published = false
        val authorIds: List<Int> = listOf(3,5)
        val bookRequest = RegisterBookRequest(title, price, published, authorIds)

        // 登録実行
        val exception = assertThrows<Exception> {
            booksRepository.insert(bookRequest)
        }
        assertEquals("指定の著者IDが存在しません。", exception.message)
    }

    // 更新用テストのためのUPDATED_AT取得メソッド追加
    fun getUpdatedAt(bookId: Int): LocalDateTime? {
        return dsl.select(BOOKS.UPDATED_AT)
            .from(BOOKS)
            .where(BOOKS.ID.eq(bookId))
            .fetchOne(BOOKS.UPDATED_AT)
    }

    @Test
    @DisplayName("既存の書籍が正しく更新されること")
    fun `updateById should update existing book information correctly`() {
        val bookIdToUpdate = 2
        val newTitle = "テストブック更新"
        val newPrice: Long = 5000
        val newIsPublished = true
        val request = UpdateBookRequest(newTitle, newPrice, newIsPublished)

        // 更新前の確認
        val originalBook = booksRepository.selectByBookId(bookIdToUpdate)
        assertEquals("テストブック2", originalBook.title)
        assertEquals(2000, originalBook.price)
        assertEquals(false, originalBook.isPublished)
        val originalUpdatedAt = getUpdatedAt(bookIdToUpdate)

        // 更新実行
        booksRepository.updateById(bookIdToUpdate, request)

        // 更新後の確認
        val updatedAuthor = booksRepository.selectByBookId(bookIdToUpdate)
        assertNotNull(updatedAuthor)
        assertEquals(newTitle, updatedAuthor.title)
        assertEquals(newPrice, updatedAuthor.price)
        assertEquals(newIsPublished, updatedAuthor.isPublished)

        val updatedUpdatedAt = getUpdatedAt(bookIdToUpdate)
        assertTrue(updatedUpdatedAt!!.isAfter(originalUpdatedAt), "UPDATED_AT should be updated to a later time")
    }

    @Test
    @DisplayName("存在しないIDを指定した場合に何も更新されないこと")
    fun `updateById should not update anything if non-existent ID is specified`() {
        val bookIdToUpdate = 4
        val newTitle = "テストブック更新"
        val newPrice: Long = 5000
        val newIsPublished = true
        val request = UpdateBookRequest(newTitle, newPrice, newIsPublished)

        // 更新実行
        booksRepository.updateById(bookIdToUpdate, request)

        // 既存のレコードが変更されていないことを確認
        val originalBook1 = booksRepository.selectByBookId(1)
        assertEquals("テストブック", originalBook1.title)
        assertEquals(1000, originalBook1.price)
        assertEquals(true, originalBook1.isPublished)

        val originalBook2 = booksRepository.selectByBookId(2)
        assertEquals("テストブック2", originalBook2.title)
        assertEquals(2000, originalBook2.price)
        assertEquals(false, originalBook2.isPublished)

        val originalBook3 = booksRepository.selectByBookId(3)
        assertEquals("テストブック3", originalBook3.title)
        assertEquals(3000, originalBook3.price)
        assertEquals(false, originalBook3.isPublished)
    }

    @Test
    @DisplayName("更新対象のIDが0の場合に更新が行われないこと")
    fun `updateById should not update anything if bookId is 0`() {
        val bookIdToUpdate = 0
        val newTitle = "テストブック更新"
        val newPrice: Long = 5000
        val newIsPublished = true
        val request = UpdateBookRequest(newTitle, newPrice, newIsPublished)

        // 更新実行
        booksRepository.updateById(bookIdToUpdate, request)

        val exception = assertThrows<IllegalArgumentException> {
            booksRepository.selectByBookId(0)
        }
        assertEquals("指定されたIDの本が見つかりません。", exception.message)
    }
}
