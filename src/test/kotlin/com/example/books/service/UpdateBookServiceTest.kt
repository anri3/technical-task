package com.example.books.service

import com.example.books.dto.AuthorRequest
import com.example.books.dto.BookAuthorsRequest
import com.example.books.dto.UpdateBookRequest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.mockito.kotlin.*
import java.time.LocalDate

// テスト対象のServiceクラス
class UpdateBookService (
    private val booksRepository: BooksRepository,
    private val authorsRepository: AuthorsRepository,
    private val booksAuthorsRepository: BooksAuthorsRepository
) {
    // 書籍情報更新
    fun updateBook(bookId: Int, request: UpdateBookRequest) {
        // IDチェック
        if (booksRepository.selectCountById(bookId) < 1) {
            throw Exception("指定のIDが存在しません")
        }

        // booksへの更新
        booksRepository.updateById(bookId, request)
    }

    // 著者更新
    fun updateAuthor(authorId: Int, request: AuthorRequest) {
        // IDチェック
        if (authorsRepository.selectCountById(authorId) < 1) {
            throw Exception("指定のIDが存在しません")
        }
        // 更新
        authorsRepository.updateById(authorId, request)
    }

    // 書籍の著者更新
    fun updateBookAuthors(bookId: Int, request: BookAuthorsRequest) {
        // bookID存在チェック
        if (booksRepository.selectCountById(bookId) < 1) {
            throw Exception("指定の書籍IDが存在しません")
        }

        // 中間テーブルのauthorIdをリストで取得
        val authorIds: List<Int> = booksAuthorsRepository.findAuthorIds(bookId)

        if (authorIds.isEmpty()) {
            throw Exception("指定の書籍IDに紐づく著者が存在しません")
        }

        // リクエスト値の著者IDリスト
        val reqAuthorIds: List<Int> = request.authorIds

        // 中間テーブルのIDを1ずつ確認
        for (authorId in authorIds) {
            // 中間テーブルにあって、リクエストの著者IDになければ中間テーブルの著者を削除実行
            if (authorId !in reqAuthorIds) {
                // 削除
                booksAuthorsRepository.deleteByBookIdAndAuthorId(bookId, authorId)
            }
        }

        // リクエストの著者を1ずつ確認
        for (authorId in reqAuthorIds) {
            // リクエストの著者IDがテーブルになければ、存在しないIDは追加できないためエラー
            if (authorsRepository.selectCountById(authorId) < 1) {
                throw Exception("著者ID: $authorId が存在しません。")
            } else {
                // リクエストの著者IDがauthorsテーブルに存在すれば、中間テーブルに登録(既に中間テーブルにある場合は、登録しない)
                booksAuthorsRepository.insert(bookId, authorId)
            }
        }
    }
}

class UpdateBookServiceTest {
    // モックオブジェクトの宣言
    private lateinit var booksRepository: BooksRepository
    private lateinit var authorsRepository: AuthorsRepository
    private lateinit var booksAuthorsRepository: BooksAuthorsRepository
    private lateinit var updateBookService: UpdateBookService

    @BeforeEach
    fun setUp() {
        // 各テストメソッドの前にモックオブジェクトとテスト対象を初期化
        booksRepository = mock()
        authorsRepository = mock()
        booksAuthorsRepository = mock()
        updateBookService = UpdateBookService(booksRepository, authorsRepository, booksAuthorsRepository)
    }

    @Nested
    @DisplayName("updateBook メソッドのテスト")
    inner class UpdateBookTests {

        private val bookId = 1
        private val sampleBookRequest = UpdateBookRequest(
            title = "テスト更新",
            price = 2000,
            isPublished = true
        )

        @Test
        @DisplayName("指定のbookIdが存在しない場合にExceptionをスローすること")
        fun `updateBook should throw Exception if bookId does not exist`() {
            // モックの挙動を設定
            whenever(booksRepository.selectCountById(bookId)).thenReturn(0)

            // 例外がスローされることを検証
            val exception = assertThrows<Exception> {
                updateBookService.updateBook(bookId, sampleBookRequest)
            }

            assertEquals("指定のIDが存在しません", exception.message)

            // 期待されるメソッドが呼び出されたことを検証
            verify(booksRepository, times(1)).selectCountById(bookId)
            verify(booksRepository, never()).updateById(eq(bookId), any())
        }

        @Test
        @DisplayName("指定bookIdが存在する場合にbooksRepository.updateByIdが呼び出されること")
        fun `updateBook should call updateById when authorId exists`() {
            // モックの挙動を設定
            whenever(booksRepository.selectCountById(bookId)).thenReturn(1)
            doNothing().whenever(booksRepository).updateById(eq(bookId), eq(sampleBookRequest))

            // メソッド実行
            updateBookService.updateBook(bookId, sampleBookRequest)

            // 期待されるメソッドが呼び出されたことを検証
            verify(booksRepository, times(1)).selectCountById(bookId)
            verify(booksRepository, times(1)).updateById(bookId, sampleBookRequest)
        }
    }

    @Nested
    @DisplayName("updateAuthor メソッドのテスト")
    inner class UpdateAuthorTests {
        private val authorId = 1
        private val sampleAuthorRequest = AuthorRequest(
            name = "テスト新著者",
            birthday = LocalDate.of(1980, 1, 1)
        )

        @Test
        @DisplayName("指定のauthorIdが存在しない場合にExceptionをスローすること")
        fun `updateAuthor should throw Exception if authorId does not exist`() {
            // モックの挙動を設定
            whenever(authorsRepository.selectCountById(authorId)).thenReturn(0)

            // 例外がスローされることを検証
            val exception = assertThrows<Exception> {
                updateBookService.updateAuthor(authorId, sampleAuthorRequest)
            }

            assertEquals("指定のIDが存在しません", exception.message)

            verify(authorsRepository, times(1)).selectCountById(authorId)
            verify(authorsRepository, never()).updateById(eq(authorId), any())
        }

        @Test
        @DisplayName("指定のauthorIdが存在する場合にauthorsRepository.updateByIdが呼び出されること")
        fun `updateAuthor should call updateById when authorId exists`() {
            // モックの挙動を設定
            whenever(authorsRepository.selectCountById(authorId)).thenReturn(1)
            doNothing().whenever(authorsRepository).updateById(eq(authorId), eq(sampleAuthorRequest))

            // メソッド実行
            updateBookService.updateAuthor(authorId, sampleAuthorRequest)

            // 期待されるメソッドが呼び出されたことを検証
            verify(authorsRepository, times(1)).selectCountById(authorId)
            verify(authorsRepository, times(1)).updateById(authorId, sampleAuthorRequest)
        }
    }

    @Nested
    @DisplayName("updateBookAuthors メソッドのテスト")
    inner class UpdateBookAuthorsTests {

        private val bookId = 1
        private val sampleBookRequest = BookAuthorsRequest(
            authorIds = emptyList()
        )

        @Test
        @DisplayName("指定のbookIdが存在しない場合にExceptionをスローすること")
        fun `updateBookAuthors should throw Exception if bookId does not exist`() {
            // モックの挙動を設定: bookId が存在しない (selectCountById が 0 を返す)
            whenever(booksRepository.selectCountById(bookId)).thenReturn(0)

            // 例外がスローされることを検証
            val exception = assertThrows<Exception> {
                updateBookService.updateBookAuthors(bookId, sampleBookRequest)
            }

            // 例外メッセージが正しいことを検証
            assertEquals("指定の書籍IDが存在しません", exception.message)

            // 呼び出し回数検証
            verify(booksRepository, times(1)).selectCountById(bookId)
            verify(booksAuthorsRepository, never()).findAuthorIds(any())
            verifyNoInteractions(authorsRepository)
        }

        @Test
        @DisplayName("booksAuthorsRepository.findAuthorIdsが空のリストを返す場合にExceptionをスローすること")
        fun `updateBookAuthors should throw Exception if author_id is empty`() {
            // 前提条件: bookId は存在する
            whenever(booksRepository.selectCountById(bookId)).thenReturn(1)

            // モックの挙動を設定: findAuthorIds が空のリストを返す
            whenever(booksAuthorsRepository.findAuthorIds(bookId)).thenReturn(emptyList())

            // 例外がスローされることを検証
            val exception = assertThrows<Exception> {
                updateBookService.updateBookAuthors(bookId, sampleBookRequest)
            }

            // 例外メッセージが正しいことを検証
            assertEquals("指定の書籍IDに紐づく著者が存在しません", exception.message)

            // 呼び出し回数検証
            verify(booksRepository, times(1)).selectCountById(bookId)
            verify(booksAuthorsRepository, times(1)).findAuthorIds(bookId)
            verifyNoInteractions(authorsRepository)
            verify(booksAuthorsRepository, never()).deleteByBookIdAndAuthorId(any(), any())
        }

        @Test
        @DisplayName("書籍の著者の追加が正しく行われること")
        fun `updateBookAuthors should handle just update add authors of book correctly`() {
            // テストデータ
            val existingBookId = 1
            val existingAuthorIds = listOf(10, 11)

            // リクエストデータ:
            // - authorId 12を追加
            val requestAuthors = listOf(10,11,12)
            val request = BookAuthorsRequest(
                authorIds = requestAuthors
            )

            // モックの挙動を設定
            whenever(booksRepository.selectCountById(existingBookId)).thenReturn(1)
            whenever(booksAuthorsRepository.findAuthorIds(existingBookId)).thenReturn(existingAuthorIds)
            whenever(authorsRepository.selectCountById(10)).thenReturn(1)
            whenever(authorsRepository.selectCountById(11)).thenReturn(1)
            whenever(authorsRepository.selectCountById(12)).thenReturn(1)

            // メソッド実行
            updateBookService.updateBookAuthors(existingBookId, request)

            // 検証: 各メソッドが期待通りに呼び出されたか
            verify(booksRepository, times(1)).selectCountById(existingBookId)
            verify(booksAuthorsRepository, times(1)).findAuthorIds(existingBookId)
            verify(authorsRepository, times(1)).selectCountById(10)
            verify(authorsRepository, times(1)).selectCountById(11)
            verify(authorsRepository, times(1)).selectCountById(12)

            // 更新の検証: 既存の10,11、新規の12がリクエストにあるため更新される
            verify(booksAuthorsRepository, times(1)).insert(existingBookId, 10)
            verify(booksAuthorsRepository, times(1)).insert(existingBookId, 11)
            verify(booksAuthorsRepository, times(1)).insert(existingBookId, 12)

            // 他のモックメソッドが余計に呼び出されていないことを検証
            verify(booksAuthorsRepository, never()).deleteByBookIdAndAuthorId(any(), any())
        }

        @Test
        @DisplayName("書籍の著者の削除と追加が正しく行われること")
        fun `updateBookAuthors should handle add, and delete authors correctly`() {
            // テストデータ
            val existingBookId = 1
            val existingAuthorIds = listOf(10, 11)

            // リクエストデータ:
            // - authorId 11削除で12を追加
            val requestAuthors = listOf(10,12)
            val request = BookAuthorsRequest(
                authorIds = requestAuthors
            )

            // モックの挙動を設定
            whenever(booksRepository.selectCountById(existingBookId)).thenReturn(1)
            whenever(booksAuthorsRepository.findAuthorIds(existingBookId)).thenReturn(existingAuthorIds)
            doNothing().whenever(booksAuthorsRepository).deleteByBookIdAndAuthorId(eq(existingBookId), eq(11))
            whenever(authorsRepository.selectCountById(10)).thenReturn(1)
            whenever(authorsRepository.selectCountById(12)).thenReturn(1)

            // メソッド実行
            updateBookService.updateBookAuthors(existingBookId, request)

            // 検証: 各メソッドが期待通りに呼び出されたか
            verify(booksRepository, times(1)).selectCountById(existingBookId)
            verify(booksAuthorsRepository, times(1)).findAuthorIds(existingBookId)
            verify(authorsRepository, times(1)).selectCountById(10)
            verify(authorsRepository, times(1)).selectCountById(12)

            // 削除の検証: 既存の11がリクエストにないため削除される
            verify(booksAuthorsRepository, times(1)).deleteByBookIdAndAuthorId(existingBookId, 11)

            // 更新の検証: 既存の10、新規の12がリクエストにあるため更新される
            verify(booksAuthorsRepository, times(1)).insert(existingBookId, 10)
            verify(booksAuthorsRepository, times(1)).insert(existingBookId, 12)
        }

        @Test
        @DisplayName("書籍の著者の削除をしてリクエストの著者IDがデータベースに存在せずエラーになること")
        fun `updateBookAuthors should throw Exception and can delete if DB author_id is not exists`() {
            // テストデータ
            val existingBookId = 1
            val existingAuthorIds = listOf(10, 11)

            // リクエストデータ:
            // - authorId 11削除で12を追加(12はDBに存在しない)
            val requestAuthors = listOf(10,12)
            val request = BookAuthorsRequest(
                authorIds = requestAuthors
            )

            // モックの挙動を設定
            whenever(booksRepository.selectCountById(existingBookId)).thenReturn(1)
            whenever(booksAuthorsRepository.findAuthorIds(existingBookId)).thenReturn(existingAuthorIds)
            doNothing().whenever(booksAuthorsRepository).deleteByBookIdAndAuthorId(eq(existingBookId), eq(11))
            whenever(authorsRepository.selectCountById(10)).thenReturn(1)

            // メソッド実行
            // 例外がスローされることを検証
            val exception = assertThrows<Exception> {
                updateBookService.updateBookAuthors(existingBookId, request)
            }
            // 例外メッセージが正しいことを検証
            assertEquals("著者ID: 12 が存在しません。", exception.message)

            // 検証: 各メソッドが期待通りに呼び出されたか
            verify(booksRepository, times(1)).selectCountById(existingBookId)
            verify(booksAuthorsRepository, times(1)).findAuthorIds(existingBookId)
            verify(authorsRepository, times(1)).selectCountById(10)
            verify(authorsRepository, times(1)).selectCountById(12)

            // 削除の検証: 既存の11がリクエストにないため削除される
            verify(booksAuthorsRepository, times(1)).deleteByBookIdAndAuthorId(existingBookId, 11)

            // 更新の検証: 既存の10がリクエストにあるため更新される
            verify(booksAuthorsRepository, times(1)).insert(existingBookId, 10)
        }

        @Test
        @DisplayName("リクエストの著者IDがデータベースに存在せずエラーになること")
        fun `updateBookAuthors should throw Exception if DB author_id is not exists`() {
            // テストデータ
            val existingBookId = 1
            val existingAuthorIds = listOf(12)

            // リクエストデータ:
            // - authorId 10を追加(10はDBに存在しない)
            val requestAuthors = listOf(10,12)
            val request = BookAuthorsRequest(
                authorIds = requestAuthors
            )

            // モックの挙動を設定
            whenever(booksRepository.selectCountById(existingBookId)).thenReturn(1)
            whenever(booksAuthorsRepository.findAuthorIds(existingBookId)).thenReturn(existingAuthorIds)
            whenever(authorsRepository.selectCountById(12)).thenReturn(1)

            // メソッド実行
            // 例外がスローされることを検証
            val exception = assertThrows<Exception> {
                updateBookService.updateBookAuthors(existingBookId, request)
            }
            // 例外メッセージが正しいことを検証
            assertEquals("著者ID: 10 が存在しません。", exception.message)

            // 検証: 各メソッドが期待通りに呼び出されたか
            verify(booksRepository, times(1)).selectCountById(existingBookId)
            verify(booksAuthorsRepository, times(1)).findAuthorIds(existingBookId)
            verify(authorsRepository, times(1)).selectCountById(10)

            // 他のモックメソッドが余計に呼び出されていないことを検証
            verify(booksAuthorsRepository, never()).insert(any(), any())
            verify(booksAuthorsRepository, never()).deleteByBookIdAndAuthorId(any(), any())
            verify(authorsRepository, never()).selectCountById(12)
        }
    }
}