package com.base.armsupportservice.dto.organization

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class OrganizationRequest(
    @field:NotBlank(message = "Название организации обязательно")
    @field:Size(max = 512)
    val name: String,
    @field:NotBlank(message = "ИНН обязателен")
    @field:Pattern(regexp = "^\\d{10}(\\d{2})?$", message = "ИНН должен содержать 10 или 12 цифр")
    val inn: String,
    @field:Pattern(regexp = "^\\d{9}$", message = "КПП должен содержать 9 цифр")
    val kpp: String? = null,
    @field:Pattern(regexp = "^\\d{13}(\\d{2})?$", message = "ОГРН должен содержать 13 или 15 цифр")
    val ogrn: String? = null,
    val legalAddress: String? = null,
    val contactEmail: String? = null,
    @field:Size(max = 32)
    val contactPhone: String? = null,
    val description: String? = null,
)
