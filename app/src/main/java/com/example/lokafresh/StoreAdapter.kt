package com.example.lokafresh

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lokafresh.response.StoreData

class StoreAdapter(
    private val stores: List<StoreData>,
    private val onUpdateClick: (StoreData) -> Unit,
    private val onDeleteClick: (StoreData) -> Unit
) : RecyclerView.Adapter<StoreAdapter.StoreViewHolder>() {

    inner class StoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvStoreId: TextView = itemView.findViewById(R.id.tvStoreId)
        val tvStoreName: TextView = itemView.findViewById(R.id.tvStoreName)
        val btnUpdate: ImageButton = itemView.findViewById(R.id.btnUpdate)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_store, parent, false)
        return StoreViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoreViewHolder, position: Int) {
        val store = stores[position]
        holder.tvStoreId.text = "ID: ${store.id}"
        holder.tvStoreName.text = "Nama: ${store.nama}"

        holder.btnUpdate.setOnClickListener { onUpdateClick(store) }
        holder.btnDelete.setOnClickListener { onDeleteClick(store) }
    }

    override fun getItemCount(): Int = stores.size
}
