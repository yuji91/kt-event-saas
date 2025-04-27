package com.example.kteventsaas.domain.tenant.converter

import com.example.kteventsaas.domain.tenant.valueobject.TenantName
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = false)
class TenantNameConverter : AttributeConverter<TenantName, String> {

    override fun convertToDatabaseColumn(attribute: TenantName): String? {
        return attribute.value
    }

    override fun convertToEntityAttribute(dbData: String): TenantName {
        return TenantName(dbData)
    }
}
