package com.example.books.service

import com.example.books.dto.AuthorRequest
import com.example.books.dto.RegisterBookRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.time.LocalDate

// テスト対象のServiceクラス
class RegisterBookService(
    private val booksRepository: BooksRepository,
    private val authorsRepository: AuthorsRepository
) {
    fun registerBook(request: RegisterBookRequest): Int {
        // 件数チェック(1件でもあれば登録しない)
        if (booksRepository.selectCountForExists(request) > 0) {
            throw IllegalArgumentException("データが既に存在します。")
        }
        // 登録
        return booksRepository.insert(request)
    }

    fun registerAuthor(request: AuthorRequest): Int {
        // 登録 同姓同名、同じ誕生日は許容
        return authorsRepository.insert(request)
    }
}

class RegisterBookServiceTest {
    // モックオブジェクトの宣言
    private lateinit var booksRepository: BooksRepository
    private lateinit var authorsRepository: AuthorsRepository
    private lateinit var registerBookService: RegisterBookService // テスト対象

    @BeforeEach
    fun setUp() {
        // 各テストメソッドの前にモックオブジェクトとテスト対象を初期化
        booksRepository = mock()
        authorsRepository = mock()
        registerBookService = RegisterBookService(booksRepository, authorsRepository)
    }

    @Test
    @DisplayName("registerBook: データが既に存在する場合にIllegalArgumentExceptionをスローすること")
    fun `registerBook should throw IllegalArgumentException when data already exists`() {
        val existingBookRequest = RegisterBookRequest(
            title = "テストブック",
            price = 1000,
            isPublished = true,
            authorIds = listOf(1,2)
        )

        // モックの挙動を設定: selectCountForExists が 1 を返すように
        whenever(booksRepository.selectCountForExists(existingBookRequest)).thenReturn(1)

        // 例外がスローされることを検証
        val exception = assertThrows<IllegalArgumentException> {
            registerBookService.registerBook(existingBookRequest)
        }

        // 例外メッセージが正しいことを検証
        assertEquals("データが既に存在します。", exception.message)

        // selectCountForExists が一度だけ呼び出されたことを検証
        verify(booksRepository, times(1)).selectCountForExists(existingBookRequest)
        // insert メソッドは呼び出されないことを検証
        verify(booksRepository, never()).insert(any())
    }

    @Test
    @DisplayName("registerBook: データが存在しない場合に正しく登録を行い、生成されたIDを返すこと")
    fun `registerBook should register book correctly when data does not exist`() {
        val newBookRequest = RegisterBookRequest(
            title = "テスト",
            price = 2000,
            isPublished = false,
            authorIds = listOf(3,4)
        )
        val generatedBookId = 5 // insert メソッドが返すであろうID

        // モックの挙動を設定:
        // selectCountForExists が 0 を返すように
        whenever(booksRepository.selectCountForExists(newBookRequest)).thenReturn(0)
        // insert が指定のIDを返すように
        whenever(booksRepository.insert(newBookRequest)).thenReturn(generatedBookId)

        // メソッド実行
        val returnedId = registerBookService.registerBook(newBookRequest)
        assertEquals(generatedBookId, returnedId)

        // selectCountForExists が一度だけ呼び出されたことを検証
        verify(booksRepository, times(1)).selectCountForExists(newBookRequest)
        // insert メソッドが一度だけ呼び出されたことを検証
        verify(booksRepository, times(1)).insert(newBookRequest)
    }

    @Test
    @DisplayName("registerBook: selectCountForExistsが2を返す場合でもIllegalArgumentExceptionをスローすること")
    fun `registerBook should throw IllegalArgumentException when selectCountForExists returns more than 0`() {
        val request = RegisterBookRequest(
            title = "テスト",
            price = 500,
            isPublished = true,
            authorIds = emptyList()
        )

        // selectCountForExists が 2 を返すように設定
        whenever(booksRepository.selectCountForExists(request)).thenReturn(2)

        val exception = assertThrows<IllegalArgumentException> {
            registerBookService.registerBook(request)
        }

        assertEquals("データが既に存在します。", exception.message)
        verify(booksRepository, times(1)).selectCountForExists(request)
        verify(booksRepository, never()).insert(any())
    }

    @Test
    @DisplayName("registerAuthor: 正しく登録を行い、生成されたIDを返すこと")
    fun `registerAuthor should register author correctly`() {
        val newAuthorRequest = AuthorRequest(
            name = "テスト著者",
            birthday = LocalDate.of(1985, 3, 10)
        )
        val generatedBookId = 5 // insert メソッドが返すであろうID

        // モックの挙動を設定:
        // insert が指定のIDを返すように
        whenever(authorsRepository.insert(newAuthorRequest)).thenReturn(generatedBookId)

        // メソッド実行
        val returnedId = registerBookService.registerAuthor(newAuthorRequest)
        assertEquals(generatedBookId, returnedId)

        // insert メソッドが一度だけ呼び出されたことを検証
        verify(authorsRepository, times(1)).insert(newAuthorRequest)
    }
}