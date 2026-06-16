package com.example.inventorystock

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class InventoryMovement(
    val id: String = "",
    val type: String = "", // "new", "update", "delete"
    val productName: String = "",
    @ServerTimestamp val timestamp: Timestamp? = null
)
