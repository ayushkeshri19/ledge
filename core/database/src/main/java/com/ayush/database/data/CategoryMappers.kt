package com.ayush.database.data

import com.ayush.common.models.Category

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    icon = icon,
    colorHex = colorHex,
    isDefault = isDefault
)
