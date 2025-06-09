package com.example.books.dto

import com.example.books.controller.validation.UpdateValidation
import jakarta.validation.constraints.*
import java.time.LocalDate

// 各API共通のリクエストボディ
data class BookRequest(
    @field:NotBlank(message = "タイトルは必須です")
    val title: String,

    @field:Min(0, message = "価格は0円以上である必要があります")
    val price: Long = 0,

    @field:NotNull(message = "出版状況は必須です")
    @field:AssertTrue(message = "未出版に更新できません", groups = [UpdateValidation::class])
    val isPublished: Boolean?,

    @field:Size(min = 1, max = 100, message = "著者は1人以上100人以下である必要があります")
    val authors: List<AuthorRequest>
)

data class AuthorRequest(
    val authorId: Int?,

    @field:NotBlank(message = "著者名は必須です")
    val name: String,

    @field:Past(message = "誕生日は過去の日付である必要があります")
    val birthday: LocalDate
)