package com.example.books.service

import com.example.books.dto.AuthorRequest
import com.example.books.dto.BookAuthorsRequest
import com.example.books.dto.UpdateBookRequest
import com.example.books.repository.AuthorsRepository
import com.example.books.repository.BooksAuthorsRepository
import com.example.books.repository.BooksRepository
import org.springframework.stereotype.Service

@Service
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
        for(authorId in authorIds) {
            // 中間テーブルにあって、リクエストの著者IDになければ中間テーブルの著者を削除実行
            if(authorId !in reqAuthorIds){
                // 削除
                booksAuthorsRepository.deleteByBookIdAndAuthorId(bookId, authorId)
            }
        }

        // リクエストの著者を1ずつ確認
        for(authorId in reqAuthorIds) {
            // リクエストの著者IDがテーブルになければ、存在しないIDは追加できないためエラー
            if(authorsRepository.selectCountById(authorId) < 1){
                throw Exception("著者ID: $authorId が存在しません。")
            }else{
                // リクエストの著者IDがauthorsテーブルに存在すれば、中間テーブルに登録(既に中間テーブルにある場合は、登録しない)
                booksAuthorsRepository.insert(bookId, authorId)
            }
        }
    }
}
