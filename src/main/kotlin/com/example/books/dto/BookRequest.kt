package com.example.books.dto

import jakarta.validation.constraints.*
import java.time.LocalDate

// 書籍情報登録APIのリクエストボディ
data class RegisterBookRequest(
    @field:NotBlank(message = "タイトルは必須です")
    val title: String,

    @field:Min(0, message = "価格は0円以上である必要があります")
    val price: Long = 0,

    @field:NotNull(message = "出版状況は必須です")
    val isPublished: Boolean,

    @field:NotNull(message = "著者は1人以上である必要があります")
    @field:Size(min = 1, message = "著者は1人以上である必要があります")
    val authorIds: List<Int>
)

// 書籍更新APIのリクエストボディ
data class UpdateBookRequest(
    @field:NotBlank(message = "タイトルは必須です")
    val title: String,

    @field:Min(0, message = "価格は0円以上である必要があります")
    val price: Long = 0,

    @field:NotNull(message = "出版状況は必須です")
    @field:AssertTrue(message = "未出版に更新できません")
    val isPublished: Boolean
)

// 著者登録、更新APIのリクエストボディ
data class AuthorRequest(
    @field:NotBlank(message = "著者名は必須です")
    val name: String,

    @field:Past(message = "誕生日は過去の日付である必要があります")
    val birthday: LocalDate
)

// 書籍の著者更新APIのリクエストボディ
data class BookAuthorsRequest(
    @field:NotNull(message = "著者は1人以上である必要があります")
    @field:Size(min = 1, message = "著者は1人以上である必要があります")
    val authorIds: List<Int>
)