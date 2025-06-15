package com.example.books.service

import com.example.books.dto.AuthorRequest
import com.example.books.dto.BookRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.time.LocalDate

// テスト対象のServiceクラス
class UpdateBookService (
    private val booksRepository: BooksRepository,
    private val authorsRepository: AuthorsRepository,
    private val booksAuthorsRepository: BooksAuthorsRepository
) {

    // 書籍情報更新(著者情報も合わせて)
    fun updateBook(bookId: Int, request: BookRequest) {
        // IDチェック
        if (booksRepository.selectCountById(bookId) < 1) {
            throw IllegalArgumentException("指定のIDが存在しません")
        }

        // booksへの更新
        booksRepository.updateById(bookId, request)

        // 中間テーブルのauthorIdをリストで取得
        val authorIds: List<Int> = booksAuthorsRepository.findAuthorIds(bookId)

        if (authorIds.isEmpty()) {
            throw IllegalArgumentException("author_idが存在しません")
        }

        // リクエスト値の著者リスト
        val reqAuthors: List<AuthorRequest> = request.authors
        // リクエスト値の著者IDリスト
        val reqAuthorIds: List<Int?> = reqAuthors.map { it.authorId }

        for(authorId in authorIds) {
            // 中間テーブルにあって、リクエストの著者IDになければ中間テーブルの著者を削除実行
            if(authorId !in reqAuthorIds){
                // 削除
                booksAuthorsRepository.deleteByBookIdAndAuthorId(bookId, authorId)
            }
        }

        // リクエストの著者を1人ずつ確認
        for(reqAuthor in reqAuthors) {
            // リクエストの著者IDなければ追加登録
            if(reqAuthor.authorId == null){
                authorsRepository.insert(bookId,reqAuthor.name, reqAuthor.birthday)
                continue
            }else{
                // リクエストに著者IDあれば更新
                authorsRepository.updateById(reqAuthor)
            }
        }
    }

    // 著者更新のみ
    fun updateAuthor(authorId: Int, request: AuthorRequest) {
        // IDチェック
        if (authorsRepository.selectCountById(authorId) < 1) {
            throw IllegalArgumentException("指定のIDが存在しません")
        }

        // 更新
        authorsRepository.updateById(authorId, request)
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
        private val sampleBookRequest = BookRequest(
            title = "テスト更新",
            price = 2000,
            isPublished = true,
            authors = emptyList() // シナリオに応じて変更
        )

        @Test
        @DisplayName("指定のbookIdが存在しない場合にIllegalArgumentExceptionをスローすること")
        fun `updateBook should throw IllegalArgumentException if bookId does not exist`() {
            // モックの挙動を設定: bookId が存在しない (selectCountById が 0 を返す)
            whenever(booksRepository.selectCountById(bookId)).thenReturn(0)

            // 例外がスローされることを検証
            val exception = assertThrows<IllegalArgumentException> {
                updateBookService.updateBook(bookId, sampleBookRequest)
            }

            // 例外メッセージが正しいことを検証
            assertEquals("指定のIDが存在しません", exception.message)

            // 呼び出し回数検証
            verify(booksRepository, times(1)).selectCountById(bookId)
            verify(booksRepository, never()).updateById(any(), any())
            verifyNoInteractions(authorsRepository)
            verifyNoInteractions(booksAuthorsRepository)
        }

        @Test
        @DisplayName("booksAuthorsRepository.findAuthorIdsが空のリストを返す場合にIllegalArgumentExceptionをスローすること")
        fun `updateBook should throw IllegalArgumentException if author_id is empty`() {
            // 前提条件: bookId は存在する
            whenever(booksRepository.selectCountById(bookId)).thenReturn(1)
            // booksRepository.updateById は呼び出される
            doNothing().whenever(booksRepository).updateById(eq(bookId), any())

            // モックの挙動を設定: findAuthorIds が空のリストを返す
            whenever(booksAuthorsRepository.findAuthorIds(bookId)).thenReturn(emptyList())

            // 例外がスローされることを検証
            val exception = assertThrows<IllegalArgumentException> {
                updateBookService.updateBook(bookId, sampleBookRequest)
            }

            // 例外メッセージが正しいことを検証
            assertEquals("author_idが存在しません", exception.message)

            // 呼び出し回数検証
            verify(booksRepository, times(1)).selectCountById(bookId)
            verify(booksRepository, times(1)).updateById(bookId, sampleBookRequest)
            verify(booksAuthorsRepository, times(1)).findAuthorIds(bookId)
            verifyNoInteractions(authorsRepository)
            verify(booksAuthorsRepository, never()).deleteByBookIdAndAuthorId(any(), any())
        }

        @Test
        @DisplayName("書籍情報と著者の更新のみが正しく行われること")
        fun `updateBook should handle just update book and authors correctly`() {
            // テストデータ
            val existingBookId = 1
            val existingAuthorIds = listOf(10, 11)

            // リクエストデータ:
            // - 既存のauthorId 10,11を更新
            val requestAuthors = listOf(
                AuthorRequest(authorId = 10, name = "テスト著者10", birthday = LocalDate.of(1985, 3, 10)),
                AuthorRequest(authorId = 11, name = "テスト著者11", birthday = LocalDate.of(2000, 7, 20))
            )
            val request = BookRequest(
                title = "テストブック更新",
                price = 3000,
                isPublished = true,
                authors = requestAuthors
            )

            // モックの挙動を設定
            whenever(booksRepository.selectCountById(existingBookId)).thenReturn(1)
            doNothing().whenever(booksRepository).updateById(eq(existingBookId), eq(request))
            whenever(booksAuthorsRepository.findAuthorIds(existingBookId)).thenReturn(existingAuthorIds)
            doNothing().whenever(authorsRepository).updateById(eq(requestAuthors[0]))
            doNothing().whenever(authorsRepository).updateById(eq(requestAuthors[1]))

            // メソッド実行
            updateBookService.updateBook(existingBookId, request)

            // 検証: 各メソッドが期待通りに呼び出されたか
            verify(booksRepository, times(1)).selectCountById(existingBookId)
            verify(booksRepository, times(1)).updateById(existingBookId, request)
            verify(booksAuthorsRepository, times(1)).findAuthorIds(existingBookId)

            // 更新の検証: 既存の10,11がリクエストにあるため更新される
            verify(authorsRepository, times(1)).updateById(requestAuthors[0])
            verify(authorsRepository, times(1)).updateById(requestAuthors[1])

            // 他のモックメソッドが余計に呼び出されていないことを検証
            verifyNoMoreInteractions(booksRepository, authorsRepository, booksAuthorsRepository)
        }

        @Test
        @DisplayName("書籍情報と著者の追加、更新、削除が正しく行われること")
        fun `updateBook should handle add, update, and delete authors correctly`() {
            // テストデータ
            val existingBookId = 1
            val existingAuthorIds = listOf(10, 11)

            // リクエストデータ:
            // - 既存のauthorId 10を更新
            // - 既存のauthorId 11を削除
            // - 新しい著者(ID=null)を追加
            val requestAuthors = listOf(
                AuthorRequest(authorId = 10, name = "テスト著者", birthday = LocalDate.of(1985, 3, 10)),
                AuthorRequest(authorId = null, name = "新著者", birthday = LocalDate.of(2000, 7, 20))
            )
            val request = BookRequest(
                title = "テストブック更新",
                price = 3000,
                isPublished = true,
                authors = requestAuthors
            )

            // モックの挙動を設定
            whenever(booksRepository.selectCountById(existingBookId)).thenReturn(1)
            doNothing().whenever(booksRepository).updateById(eq(existingBookId), eq(request))
            whenever(booksAuthorsRepository.findAuthorIds(existingBookId)).thenReturn(existingAuthorIds)
            doNothing().whenever(booksAuthorsRepository).deleteByBookIdAndAuthorId(eq(existingBookId), eq(11))
            doNothing().whenever(authorsRepository).updateById(eq(requestAuthors[0]))
            whenever(authorsRepository.insert(eq(existingBookId), eq("新著者"), eq(LocalDate.of(2000, 7, 20)))).thenReturn(12)

            // メソッド実行
            updateBookService.updateBook(existingBookId, request)

            // 検証: 各メソッドが期待通りに呼び出されたか
            verify(booksRepository, times(1)).selectCountById(existingBookId)
            verify(booksRepository, times(1)).updateById(existingBookId, request)
            verify(booksAuthorsRepository, times(1)).findAuthorIds(existingBookId)

            // 削除の検証: 既存の11がリクエストにないため削除される
            verify(booksAuthorsRepository, times(1)).deleteByBookIdAndAuthorId(existingBookId, 11)

            // 更新の検証: 既存の10がリクエストにあるため更新される
            verify(authorsRepository, times(1)).updateById(requestAuthors[0])

            // 追加の検証: IDがnullの新しい著者が追加される
            verify(authorsRepository, times(1)).insert(existingBookId, "新著者", LocalDate.of(2000, 7, 20))

            // 他のモックメソッドが余計に呼び出されていないことを検証
            verifyNoMoreInteractions(booksRepository, authorsRepository, booksAuthorsRepository)
        }

        @Test
        @DisplayName("書籍情報と著者の追加、更新が正しく行われ、削除は行われないこと")
        fun `updateBook should handle add, update, and not delete authors correctly`() {
            // テストデータ
            val existingBookId = 1
            val existingAuthorIds = listOf(10, 11)

            // リクエストデータ:
            // - 既存のauthorId 10,11を更新
            // - 新しい著者(ID=null)を追加
            val requestAuthors = listOf(
                AuthorRequest(authorId = 10, name = "テスト著者10", birthday = LocalDate.of(1985, 3, 10)),
                AuthorRequest(authorId = 11, name = "テスト著者11", birthday = LocalDate.of(2000, 7, 20)),
                AuthorRequest(authorId = null, name = "テスト著者12", birthday = LocalDate.of(1999, 12, 2))
            )
            val request = BookRequest(
                title = "テストブック更新",
                price = 3000,
                isPublished = true,
                authors = requestAuthors
            )

            // モックの挙動を設定
            whenever(booksRepository.selectCountById(existingBookId)).thenReturn(1)
            doNothing().whenever(booksRepository).updateById(eq(existingBookId), eq(request))
            whenever(booksAuthorsRepository.findAuthorIds(existingBookId)).thenReturn(existingAuthorIds)
            doNothing().whenever(authorsRepository).updateById(eq(requestAuthors[0]))
            doNothing().whenever(authorsRepository).updateById(eq(requestAuthors[1]))
            // 追加の挙動を設定
            whenever(authorsRepository.insert(eq(existingBookId), eq("テスト著者12"), eq(LocalDate.of(1999, 12, 2)))).thenReturn(12)

            // メソッド実行
            updateBookService.updateBook(existingBookId, request)

            // 検証: 各メソッドが期待通りに呼び出されたか
            verify(booksRepository, times(1)).selectCountById(existingBookId)
            verify(booksRepository, times(1)).updateById(existingBookId, request)
            verify(booksAuthorsRepository, times(1)).findAuthorIds(existingBookId)

            // 削除の検証: 既存の著者IDが全てリクエストに含まれるため、deleteByBookIdAndAuthorId は呼び出されない
            verify(booksAuthorsRepository, never()).deleteByBookIdAndAuthorId(any(), any())

            // 更新の検証
            verify(authorsRepository, times(1)).updateById(requestAuthors[0])
            verify(authorsRepository, times(1)).updateById(requestAuthors[1])

            // 追加の検証
            verify(authorsRepository, times(1)).insert(existingBookId, "テスト著者12", LocalDate.of(1999, 12, 2))
        }

        @Test
        @DisplayName("リクエストの著者リストが空でないが、AuthorRequestのauthorIdが全てnullの場合、全てinsertされること")
        fun `updateBook should insert all authors if all authorIds in request are null`() {
            // テストデータ
            val existingBookId = 1
            val existingAuthorIds = listOf(301)

            // リクエストデータ:
            // - 新しい著者(ID=null)を2人追加
            val requestAuthors = listOf(
                AuthorRequest(authorId = null, name = "テスト著者302", birthday = LocalDate.of(1990, 1, 1)),
                AuthorRequest(authorId = null, name = "テスト著者303", birthday = LocalDate.of(1995, 5, 5))
            )
            val request = BookRequest(
                title = "テストブック更新",
                price = 3000,
                isPublished = true,
                authors = requestAuthors
            )

            // モックの挙動を設定
            whenever(booksRepository.selectCountById(existingBookId)).thenReturn(1)
            doNothing().whenever(booksRepository).updateById(eq(existingBookId), eq(request))
            whenever(booksAuthorsRepository.findAuthorIds(existingBookId)).thenReturn(existingAuthorIds)
            doNothing().whenever(booksAuthorsRepository).deleteByBookIdAndAuthorId(eq(existingBookId), eq(301)) // 301は削除される
            whenever(authorsRepository.insert(eq(existingBookId), eq("テスト著者302"), eq(LocalDate.of(1990, 1, 1)))).thenReturn(401)
            whenever(authorsRepository.insert(eq(existingBookId), eq("テスト著者303"), eq(LocalDate.of(1995, 5, 5)))).thenReturn(402)

            // メソッド実行
            updateBookService.updateBook(existingBookId, request)

            // 検証: 削除、追加の検証
            verify(booksAuthorsRepository, times(1)).deleteByBookIdAndAuthorId(existingBookId, 301)
            verify(authorsRepository, times(1)).insert(existingBookId, "テスト著者302", LocalDate.of(1990, 1, 1))
            verify(authorsRepository, times(1)).insert(existingBookId, "テスト著者303", LocalDate.of(1995, 5, 5))

            // 検証: 更新は呼ばれない
            verify(authorsRepository, never()).updateById(any<AuthorRequest>())
        }
    }

    @Nested
    @DisplayName("updateAuthor メソッドのテスト")
    inner class UpdateAuthorTests {
        private val authorId = 1
        private val sampleAuthorRequest = AuthorRequest(
            authorId = 1,
            name = "テスト新著者",
            birthday = LocalDate.of(1980, 1, 1)
        )

        @Test
        @DisplayName("指定のauthorIdが存在しない場合にIllegalArgumentExceptionをスローすること")
        fun `updateAuthor should throw IllegalArgumentException if authorId does not exist`() {
            // モックの挙動を設定
            whenever(authorsRepository.selectCountById(authorId)).thenReturn(0)

            // 例外がスローされることを検証
            val exception = assertThrows<IllegalArgumentException> {
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
}

