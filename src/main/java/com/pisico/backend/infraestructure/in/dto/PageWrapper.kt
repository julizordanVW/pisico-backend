package com.pisico.backend.infraestructure.`in`.dto

data class PageWrapper<T>(
    val content: List<T>,
    val hasNext: Boolean,
    val pageNumber: Int
)