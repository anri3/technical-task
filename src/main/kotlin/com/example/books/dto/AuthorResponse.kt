package com.example.books.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

// 著者情報参照APIのレスポンスボディ
@JsonInclude(JsonInclude.Include.NON_NULL)
data class AuthorResponse(
    @JsonProperty
    val books: List<BookInfo>
)

data class BookInfo(
    @JsonProperty
    val title: String,

    @JsonProperty
    val price: Long = 0,

    @JsonProperty
    val isPublished: Boolean?
)