package com.example.books.dto

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

    // 指定されたリクエストオブジェクトをバリデーションし、BindingResult に結果を設定するヘルパー関数。
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

    @Test
    @DisplayName("RegisterBookRequest: 全てのバリデーションルールを満たす場合、BindingResultにエラーがないこと")
    fun `RegisterBookRequest should have no errors in BindingResult when valid`() {
        val validRequest = RegisterBookRequest(
            title = "テスト",
            price = 1000,
            isPublished = true,
            authorIds = listOf(1,2)
            )
        val bindingResult = validateAndGetBindingResult(validRequest)
        assertFalse(bindingResult.hasErrors(), "Valid BookRequest should have no errors")
        assertTrue(bindingResult.allErrors.isEmpty(), "Valid BookRequest should have no errors list")
    }

    @Test
    @DisplayName("UpdateBookRequest: 全てのバリデーションルールを満たす場合、BindingResultにエラーがないこと")
    fun `UpdateBookRequest should have no errors in BindingResult when valid`() {
        val validRequest = UpdateBookRequest(
            title = "テスト",
            price = 1000,
            isPublished = true
        )
        val bindingResult = validateAndGetBindingResult(validRequest)
        assertFalse(bindingResult.hasErrors(), "Valid BookRequest should have no errors")
        assertTrue(bindingResult.allErrors.isEmpty(), "Valid BookRequest should have no errors list")
    }

    @Test
    @DisplayName("AuthorRequest: 全てのバリデーションルールを満たす場合、BindingResultにエラーがないこと")
    fun `AuthorRequest should have no errors in BindingResult when valid`() {
        val validRequest = AuthorRequest(
            name = "テスト",
            birthday = LocalDate.of(2025, 6, 12)
        )
        val bindingResult = validateAndGetBindingResult(validRequest)
        assertFalse(bindingResult.hasErrors(), "Valid BookRequest should have no errors")
        assertTrue(bindingResult.allErrors.isEmpty(), "Valid BookRequest should have no errors list")
    }

    @Test
    @DisplayName("BookAuthorsRequest: 全てのバリデーションルールを満たす場合、BindingResultにエラーがないこと")
    fun `BookAuthorsRequest should have no errors in BindingResult when valid`() {
        val validRequest = BookAuthorsRequest(
            authorIds = listOf(1,2)
        )
        val bindingResult = validateAndGetBindingResult(validRequest)
        assertFalse(bindingResult.hasErrors(), "Valid BookRequest should have no errors")
        assertTrue(bindingResult.allErrors.isEmpty(), "Valid BookRequest should have no errors list")
    }

    @Test
    @DisplayName("RegisterBookRequest: title が空の場合、BindingResultにNotBlankエラーがあること")
    fun `RegisterBookRequest should have NotBlank error in BindingResult for empty title`() {
        val invalidRequest = RegisterBookRequest(
            title = "",
            price = 1000,
            isPublished = true,
            authorIds = listOf(1,2)
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
    @DisplayName("UpdateBookRequest: title が空の場合、BindingResultにNotBlankエラーがあること")
    fun `UpdateBookRequest should have NotBlank error in BindingResult for empty title`() {
        val invalidRequest = UpdateBookRequest(
            title = "",
            price = 1000,
            isPublished = true
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
    @DisplayName("AuthorRequest: name が空の場合、BindingResultにNotBlankエラーがあること")
    fun `AuthorRequest should have NotBlank error in BindingResult for empty name`() {
        val invalidRequest = AuthorRequest(
            name = "",
            birthday = LocalDate.of(2025, 6, 12)
        )
        val bindingResult = validateAndGetBindingResult(invalidRequest)
        assertTrue(bindingResult.hasErrors(), "BindingResult should have errors for empty name")
        assertEquals(1, bindingResult.fieldErrors.size, "Should have 1 field error")
        val fieldError = bindingResult.getFieldError("name")
        assertNotNull(fieldError)
        assertEquals("NotBlank", fieldError?.code)
        assertEquals("著者名は必須です", fieldError?.defaultMessage)
    }

    @Test
    @DisplayName("RegisterBookRequest: price が負の値の場合、BindingResultにMinエラーがあること")
    fun `RegisterBookRequest should have Min error in BindingResult for negative price`() {
        val invalidRequest = RegisterBookRequest(
            title = "タイトル",
            price = -1000,
            isPublished = true,
            authorIds = listOf(1,2)
        )
        val bindingResult = validateAndGetBindingResult(invalidRequest)
        assertTrue(bindingResult.hasErrors())
        val fieldError = bindingResult.getFieldError("price")
        assertNotNull(fieldError)
        assertEquals("Min", fieldError?.code)
        assertEquals("価格は0円以上である必要があります", fieldError?.defaultMessage)
    }

    @Test
    @DisplayName("UpdateBookRequest: price が負の値の場合、BindingResultにMinエラーがあること")
    fun `UpdateBookRequest should have Min error in BindingResult for negative price`() {
        val invalidRequest = UpdateBookRequest(
            title = "タイトル",
            price = -1000,
            isPublished = true
        )
        val bindingResult = validateAndGetBindingResult(invalidRequest)
        assertTrue(bindingResult.hasErrors())
        val fieldError = bindingResult.getFieldError("price")
        assertNotNull(fieldError)
        assertEquals("Min", fieldError?.code)
        assertEquals("価格は0円以上である必要があります", fieldError?.defaultMessage)
    }


    @Test
    @DisplayName("UpdateBookRequest: isPublished が false の場合、BindingResultにAssertTrueエラーがあること")
    fun `UpdateBookRequest should have AssertTrue error in BindingResult for false isPublished`() {
        val invalidRequest = UpdateBookRequest(
            title = "タイトル",
            price = -1000,
            isPublished = false
        )
        val bindingResult = validateAndGetBindingResult(invalidRequest)
        assertTrue(bindingResult.hasErrors())
        val fieldError = bindingResult.getFieldError("isPublished")
        assertNotNull(fieldError)
        assertEquals("AssertTrue", fieldError?.code)
        assertEquals("未出版に更新できません", fieldError?.defaultMessage)
    }

    @Test
    @DisplayName("RegisterBookRequest: authorIds リストが空の場合、BindingResultにSizeエラーがあること")
    fun `RegisterBookRequest should have Size error in BindingResult for empty authors list`() {
        val invalidRequest = RegisterBookRequest(
            title = "タイトル",
            price = 1000,
            isPublished = true,
            authorIds = listOf()
        )
        val bindingResult = validateAndGetBindingResult(invalidRequest)
        assertTrue(bindingResult.hasErrors())
        val fieldError = bindingResult.getFieldError("authorIds")
        assertNotNull(fieldError)
        assertEquals("Size", fieldError?.code)
        assertEquals("著者は1人以上である必要があります", fieldError?.defaultMessage)
    }

    @Test
    @DisplayName("AuthorRequest: birthdayが現在日の場合、ネストされたBindingResultにエラーがあること")
    fun `AuthorRequest should have nested Past error in BindingResult for birthday is today `() {
        val invalidRequest = AuthorRequest(
            name = "テスト",
            birthday = LocalDate.of(2025, 6, 16)
        )
        val bindingResult = validateAndGetBindingResult(invalidRequest)
        assertTrue(bindingResult.hasErrors())
        val fieldError = bindingResult.getFieldError("birthday")
        assertNotNull(fieldError)
        assertEquals("Past", fieldError?.code)
        assertEquals("誕生日は過去の日付である必要があります", fieldError?.defaultMessage)
    }

    @Test
    @DisplayName("BookAuthorsRequest: authorIds リストが空の場合、BindingResultにSizeエラーがあること")
    fun `BookAuthorsRequest should have Size error in BindingResult for empty authors list`() {
        val invalidRequest = BookAuthorsRequest(
            authorIds = listOf()
        )
        val bindingResult = validateAndGetBindingResult(invalidRequest)
        assertTrue(bindingResult.hasErrors())
        val fieldError = bindingResult.getFieldError("authorIds")
        assertNotNull(fieldError)
        assertEquals("Size", fieldError?.code)
        assertEquals("著者は1人以上である必要があります", fieldError?.defaultMessage)
    }
}