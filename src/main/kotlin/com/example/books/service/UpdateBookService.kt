package com.example.books.service

import com.example.books.dto.AuthorRequest
import com.example.books.dto.BookRequest
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
        for(reqAuthors in reqAuthors) {
            // リクエストに著者IDなければ追加登録
            if(reqAuthors.authorId == null){
                authorsRepository.insert(bookId,reqAuthors)
                continue
            }else{
                // リクエストに著者IDあれば更新
                authorsRepository.updateById(reqAuthors)
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
