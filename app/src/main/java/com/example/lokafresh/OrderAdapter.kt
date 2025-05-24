import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lokafresh.R
import com.example.lokafresh.response.DoData
import com.example.lokafresh.response.StoreData

class OrderAdapter(
    private var orders: List<DoData>,
    private var storeList: List<StoreData>,
    private val onDeleteClick: (DoData) -> Unit,
    private val onUpdateClick: (DoData) -> Unit,
    private val onItemClick: (DoData) -> Unit  // tambahan parameter callback klik item
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
        val storeName = storeList.find { it.id.toString() == order.destination }?.nama ?: "Unknown Store"

        holder.orderNumber.text = "Order: ${order.order_number}"
        holder.destination.text = "Destination: $storeName"
        holder.username.text = "Username: ${order.username}"

        holder.btnDelete.setOnClickListener { onDeleteClick(order) }
        holder.btnUpdate.setOnClickListener { onUpdateClick(order) }

        // Pasang klik listener pada root item view
        holder.itemView.setOnClickListener {
            onItemClick(order)
        }
    }

    override fun getItemCount() = orders.size

    fun updateData(newOrders: List<DoData>, newStoreList: List<StoreData>) {
        orders = newOrders
        storeList = newStoreList
        notifyDataSetChanged()
    }
}
