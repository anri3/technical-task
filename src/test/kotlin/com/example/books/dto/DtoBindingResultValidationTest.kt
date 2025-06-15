package com.example.books.dto

import com.example.books.controller.validation.UpdateValidation
import jakarta.validation.Validation
import jakarta.validation.Validator
import jakarta.validation.ValidatorFactory
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.BindingResult
import java.time.LocalDate

class DtoBindingResultValidationTest {

    private companion object {
        lateinit var validator: Validator

        @BeforeAll
        @JvmStatic
        fun setupValidator() {
            val factory: ValidatorFactory = Validation.buildDefaultValidatorFactory()
            validator = factory.validator
        }
    }

    /**
     * 指定されたリクエストオブジェクトをバリデーションし、BindingResult に結果を設定するヘルパー関数。
     */
    private fun <T : Any> validateAndGetBindingResult(
        request: T,
        vararg groups: Class<*>
    ): BindingResult {
        // BindingResult の実装として BeanPropertyBindingResult を使用
        val bindingResult = BeanPropertyBindingResult(request, request::class.simpleName!!)

        // Jakarta Bean Validation の Validator を使ってバリデーションを実行
        val violations = validator.validate(request, *groups)

        // 発生した違反を BindingResult に追加
        violations.forEach { violation ->
            // FieldError を生成し、BindingResult に追加
            violation.constraintDescriptor.annotation.annotationClass.simpleName?.let {
                bindingResult.rejectValue(
                    violation.propertyPath.toString(), // フィールド名 (例: "title", "authors[0].name")
                    it, // エラーコード (例: "NotBlank", "Min")
                    violation.message // エラーメッセージ
                )
            }
        }
        return bindingResult
    }

    // --- BookRequest のバリデーションテスト (BindingResult 使用) ---

    @Test
    @DisplayName("BookRequest: 全てのバリデーションルールを満たす場合、BindingResultにエラーがないこと")
    fun `BookRequest should have no errors in BindingResult when valid`() {
        val validRequest = BookRequest(
            title = "テスト",
            price = 1000,
            isPublished = true,
            authors = listOf(
                AuthorRequest(1, "テスト著者", LocalDate.of(1980, 1, 1))
            )
        )
        val bindingResult = validateAndGetBindingResult(validRequest)
        assertFalse(bindingResult.hasErrors(), "Valid BookRequest should have no errors")
        assertTrue(bindingResult.allErrors.isEmpty(), "Valid BookRequest should have no errors list")
    }

    @Test
    @DisplayName("BookRequest: title が空の場合、BindingResultにNotBlankエラーがあること")
    fun `BookRequest should have NotBlank error in BindingResult for empty title`() {
        val invalidRequest = BookRequest(
            title = "",
            price = 100,
            isPublished = true,
            authors = listOf(AuthorRequest(1, "テスト著者", LocalDate.of(1980, 1, 1)))
        )
        val bindingResult = validateAndGetBindingResult(invalidRequest)
        assertTrue(bindingResult.hasErrors(), "BindingResult should have errors for empty title")
        assertEquals(1, bindingResult.fieldErrors.size, "Should have 1 field error")
        val fieldError = bindingResult.getFieldError("title")
        assertNotNull(fieldError)
        assertEquals("NotBlank", fieldError?.code)
        assertEquals("タイトルは必須です", fieldError?.defaultMessage)
    }

    @Test
    @DisplayName("BookRequest: price が負の値の場合、BindingResultにMinエラーがあること")
    fun `BookRequest should have Min error in BindingResult for negative price`() {
        val invalidRequest = BookRequest(
            title = "テスト",
            price = -100,
            isPublished = true,
            authors = listOf(AuthorRequest(1, "テスト著者", LocalDate.of(1980, 1, 1)))
        )
        val bindingResult = validateAndGetBindingResult(invalidRequest)
        assertTrue(bindingResult.hasErrors())
        val fieldError = bindingResult.getFieldError("price")
        assertNotNull(fieldError)
        assertEquals("Min", fieldError?.code)
        assertEquals("価格は0円以上である必要があります", fieldError?.defaultMessage)
    }

    @Test
    @DisplayName("BookRequest: isPublished が null の場合、BindingResultにNotNullエラーがあること")
    fun `BookRequest should have NotNull error in BindingResult for null isPublished`() {
        val invalidRequest = BookRequest(
            title = "テスト",
            price = 100,
            isPublished = null,
            authors = listOf(AuthorRequest(1, "テスト著者", LocalDate.of(1980, 1, 1)))
        )
        val bindingResult = validateAndGetBindingResult(invalidRequest)
        assertTrue(bindingResult.hasErrors())
        val fieldError = bindingResult.getFieldError("isPublished")
        assertNotNull(fieldError)
        assertEquals("NotNull", fieldError?.code)
        assertEquals("出版状況は必須です", fieldError?.defaultMessage)
    }

    @Test
    @DisplayName("BookRequest: isPublished が false の場合 (UpdateValidationグループ)、BindingResultにAssertTrueエラーがあること")
    fun `BookRequest should have AssertTrue error in BindingResult for false isPublished in UpdateValidation group`() {
        val invalidRequest = BookRequest(
            title = "テスト",
            price = 100,
            isPublished = false,
            authors = listOf(AuthorRequest(1, "テスト著者", LocalDate.of(1980, 1, 1)))
        )
        val bindingResult = validateAndGetBindingResult(invalidRequest, UpdateValidation::class.java)
        assertTrue(bindingResult.hasErrors())
        val fieldError = bindingResult.getFieldError("isPublished")
        assertNotNull(fieldError)
        assertEquals("AssertTrue", fieldError?.code)
        assertEquals("未出版に更新できません", fieldError?.defaultMessage)
    }

    @Test
    @DisplayName("BookRequest: authors リストが空の場合、BindingResultにSizeエラーがあること")
    fun `BookRequest should have Size error in BindingResult for empty authors list`() {
        val invalidRequest = BookRequest(
            title = "テスト",
            price = 100,
            isPublished = true,
            authors = emptyList()
        )
        val bindingResult = validateAndGetBindingResult(invalidRequest)
        assertTrue(bindingResult.hasErrors())
        val fieldError = bindingResult.getFieldError("authors")
        assertNotNull(fieldError)
        assertEquals("Size", fieldError?.code)
        assertEquals("著者は1人以上である必要があります", fieldError?.defaultMessage)
    }

    @Test
    @DisplayName("BookRequest: authors リストのAuthorRequestでnameが空の場合、ネストされたBindingResultにエラーがあること")
    fun `BookRequest should have nested NotBlank error in BindingResult for empty author name`() {
        val invalidRequest = BookRequest(
            title = "テスト",
            price = 100,
            isPublished = true,
            authors = listOf(AuthorRequest(null, "", LocalDate.of(1980, 1, 1)))
        )
        val bindingResult = validateAndGetBindingResult(invalidRequest)
        assertTrue(bindingResult.hasErrors())
        assertEquals(1, bindingResult.fieldErrors.size, "Should have 1 field error")
        val fieldError = bindingResult.getFieldError("authors[0].name")
        assertNotNull(fieldError)
        assertEquals("NotBlank", fieldError?.code)
        assertEquals("著者名は必須です", fieldError?.defaultMessage)
    }

    @Test
    @DisplayName("BookRequest: authors リストのAuthorRequestでbirthdayが現在日の場合、ネストされたBindingResultにエラーがあること")
    fun `BookRequest should have nested Past error in BindingResult for birthday is today `() {
        val invalidRequest = BookRequest(
            title = "テスト",
            price = 100,
            isPublished = true,
            authors = listOf(AuthorRequest(null, "テスト著者", LocalDate.of(2025, 6, 12)))
        )
        val bindingResult = validateAndGetBindingResult(invalidRequest)
        assertTrue(bindingResult.hasErrors())
        val fieldError = bindingResult.getFieldError("authors[0].birthday")
        assertNotNull(fieldError)
        assertEquals("Past", fieldError?.code)
        assertEquals("誕生日は過去の日付である必要があります", fieldError?.defaultMessage)
    }
}