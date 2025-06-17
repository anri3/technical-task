package com.example.books.service

import com.example.books.dto.AuthorResponse
import com.example.books.dto.BookInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*

// テスト対象のServiceクラス
class BookService(
    private val authorsRepository: AuthorsRepository,
    private val booksRepository: BooksRepository
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

class SelectBookServiceTest {
    // モックオブジェクトの宣言
    private lateinit var authorsRepository: AuthorsRepository
    private lateinit var booksRepository: BooksRepository
    private lateinit var bookService: BookService // テスト対象

    @BeforeEach
    fun setUp() {
        // 各テストメソッドの前にモックオブジェクトとテスト対象を初期化
        authorsRepository = mock()
        booksRepository = mock()
        bookService = BookService(authorsRepository, booksRepository)
    }

    @Test
    @DisplayName("selectBooks: authorsRepository.selectIdByNameが空のリストを返す場合にIllegalArgumentExceptionをスローすること")
    fun `selectBooks should throw IllegalArgumentException when bookIdList is empty`() {
        val searchName = "テスト"

        // モックの挙動を設定: authorsRepository.selectIdByName が空のリストを返すように
        whenever(authorsRepository.selectIdByName(searchName)).thenReturn(emptyList())

        // 例外がスローされることを検証
        val exception = assertThrows<IllegalArgumentException> {
            bookService.selectBooks(searchName)
        }

        // 例外メッセージが正しいことを検証
        assertEquals("検索条件が存在しませんでした。", exception.message)

        // authorsRepository.selectIdByName が一度だけ呼び出されたことを検証
        verify(authorsRepository, times(1)).selectIdByName(searchName)
        // booksRepository のメソッドは呼び出されないことを検証
        verifyNoInteractions(booksRepository)
    }

    @Test
    @DisplayName("selectBooks: authorsRepository.selectIdByNameがIDリストを返す場合に正しいAuthorResponseを返すこと")
    fun `selectBooks should return correct AuthorResponse when bookIdList is not empty`() {
        val searchName = "テスト著者2"
        val authorBookIds = listOf(1, 2) // authorsRepository が返すであろう Book ID のリスト

        // authorsRepository のモックの挙動を設定
        whenever(authorsRepository.selectIdByName(searchName)).thenReturn(authorBookIds)

        // booksRepository のモックの挙動を設定
        val bookInfo1 = BookInfo("テストブック", 1000, true)
        val bookInfo2 = BookInfo("テストブック2", 2000, false)

        whenever(booksRepository.selectByBookId(1)).thenReturn(bookInfo1)
        whenever(booksRepository.selectByBookId(2)).thenReturn(bookInfo2)

        // メソッド実行
        val response = bookService.selectBooks(searchName)

        // 結果のAuthorResponseが期待通りであることを検証
        assertNotNull(response)
        assertEquals(2, response.books.size)
        assertEquals(bookInfo1, response.books[0])
        assertEquals(bookInfo2, response.books[1])

        // authorsRepository.selectIdByName が一度だけ呼び出されたことを検証
        verify(authorsRepository, times(1)).selectIdByName(searchName)
        // booksRepository.selectByBookId が各 ID に対して一度ずつ呼び出されたことを検証
        verify(booksRepository, times(1)).selectByBookId(1)
        verify(booksRepository, times(1)).selectByBookId(2)
    }

    @Test
    @DisplayName("selectBooks: authorsRepository.selectIdByNameが空ではないがbooksRepositoryがnullを返す場合 (想定外のケース)")
    fun `selectBooks should handle null bookInfo from booksRepository (if applicable)`() {
        val searchName = "テストブック"
        val authorBookIds = listOf(201)

        // booksRepository.selectByBookId が null を返すようにモックする
        // 返却のBookInfoがnullを許容しない前提で例外発生を確認する
        whenever(authorsRepository.selectIdByName(searchName)).thenReturn(authorBookIds)
        whenever(booksRepository.selectByBookId(201)).thenThrow(IllegalStateException("Book not found for ID"))

        val exception = assertThrows<IllegalStateException> {
            bookService.selectBooks(searchName)
        }
        assertEquals("Book not found for ID", exception.message)

        verify(authorsRepository).selectIdByName(searchName)
        verify(booksRepository).selectByBookId(201)
    }
}