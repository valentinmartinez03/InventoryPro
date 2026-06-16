package com.example.inventorystock.ui.components

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.inventorystock.R
import com.example.inventorystock.activities.AddProductActivity
import com.example.inventorystock.data.model.Product

class ProductAdapter(
    private val products: List<Product>,
    private val onUpdateStock: (Product, Int) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tv_product_name)
        val category: TextView = view.findViewById(R.id.tv_product_category)
        val stock: TextView = view.findViewById(R.id.tv_product_stock)
        val price: TextView = view.findViewById(R.id.tv_product_price)
        val icon: ImageView = view.findViewById(R.id.iv_product_icon)
        val btnAdd: ImageButton = view.findViewById(R.id.btn_add_stock)
        val btnRemove: ImageButton = view.findViewById(R.id.btn_remove_stock)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.name.text = product.name
        holder.category.text = product.category
        holder.stock.text = "Stock: ${product.stock}"
        holder.price.text = "$${product.price}"

        holder.btnAdd.setOnClickListener {
            onUpdateStock(product, product.stock + 1)
        }

        holder.btnRemove.setOnClickListener {
            if (product.stock > 0) {
                onUpdateStock(product, product.stock - 1)
            }
        }

        // Click corto para EDITAR
        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, AddProductActivity::class.java).apply {
                putExtra("PRODUCT_ID", product.id)
                putExtra("PRODUCT_NAME", product.name)
                putExtra("PRODUCT_CATEGORY", product.category)
                putExtra("PRODUCT_STOCK", product.stock)
                putExtra("PRODUCT_PRICE", product.price)
                putExtra("PRODUCT_BARCODE", product.barcode)
            }
            it.context.startActivity(intent)
        }

        // Click largo para BORRAR
        holder.itemView.setOnLongClickListener {
            onDeleteClick(product)
            true
        }
    }

    override fun getItemCount() = products.size
}
