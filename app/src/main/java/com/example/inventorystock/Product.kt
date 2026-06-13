package com.example.inventorystock

data class Product(
    var id: String = "",
    val name: String = "",
    val category: String = "",
    val stock: Int = 0,
    val price: Double = 0.0,
    val barcode: String = ""
)
