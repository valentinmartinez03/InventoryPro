package com.example.inventorystock.viewmodel

import androidx.lifecycle.ViewModel
import com.example.inventorystock.data.model.InventoryMovement
import com.example.inventorystock.data.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DashboardViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount

    private val _inStockCount = MutableStateFlow(0)
    val inStockCount: StateFlow<Int> = _inStockCount

    private val _criticalCount = MutableStateFlow(0)
    val criticalCount: StateFlow<Int> = _criticalCount

    private val _movements = MutableStateFlow<List<InventoryMovement>>(emptyList())
    val movements: StateFlow<List<InventoryMovement>> = _movements

    init {
        fetchData()
    }

    private fun fetchData() {
        db.collection("products").addSnapshotListener { value, _ ->
            value?.toObjects(Product::class.java)?.let { products ->
                _totalCount.value = products.size
                _inStockCount.value = products.count { it.stock > 0 }
                _criticalCount.value = products.count { it.stock == 0 }
            }
        }

        db.collection("movements")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(3)
            .addSnapshotListener { value, _ ->
                value?.toObjects(InventoryMovement::class.java)?.let {
                    _movements.value = it
                }
            }
    }
}
