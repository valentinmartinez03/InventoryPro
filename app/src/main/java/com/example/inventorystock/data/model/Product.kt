package com.example.inventorystock.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey var id: String = "",
    val name: String = "",
    val category: String = "",
    val stock: Int = 0,
    val price: Double = 0.0,
    val barcode: String = ""
)
