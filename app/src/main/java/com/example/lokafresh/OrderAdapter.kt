package com.example.lokafresh

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lokafresh.response.DoData

import java.text.SimpleDateFormat
import java.util.*

class OrderAdapter(
    private var orders: List<DoData>,
    private val onDeleteClick: (DoData) -> Unit,
    private val onUpdateClick: (DoData) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderNumber = itemView.findViewById<TextView>(R.id.tvOrderNumber)
        val destination = itemView.findViewById<TextView>(R.id.tvDestination)
        val username = itemView.findViewById<TextView>(R.id.tvUsername)
        val btnUpdate = itemView.findViewById<ImageButton>(R.id.btnUpdate)
        val btnDelete = itemView.findViewById<ImageButton>(R.id.btnDelete)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.orderNumber.text = "Order: ${order.order_number}"
        holder.destination.text = "Destination: ${order.destination}"
        holder.username.text = "Username: ${order.username}"


        holder.btnDelete.setOnClickListener { onDeleteClick(order) }
        holder.btnUpdate.setOnClickListener { onUpdateClick(order) }
    }

    override fun getItemCount() = orders.size

    fun updateData(newOrders: List<DoData>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}


