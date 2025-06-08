package com.example.books.service

import com.example.books.dto.BookRequest

interface BookService {
    fun registerBook(request: BookRequest): Int
}