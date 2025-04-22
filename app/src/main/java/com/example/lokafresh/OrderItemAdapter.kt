package com.example.lokafresh
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OrderItemAdapter(
    private val dataSet: MutableList<ListItem>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<OrderItemAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    class ViewHolder(view: View, private val itemClickListener: OnItemClickListener) : RecyclerView.ViewHolder(view) {
        val imgIcon: ImageView = view.findViewById(R.id.img_icon)
        val tvTitle: TextView = view.findViewById(R.id.tv_title)
        val tvSubtitle: TextView = view.findViewById(R.id.tv_subtitle)
        val tvDescription: TextView = view.findViewById(R.id.tv_description)
        val imgCheck: ImageView = view.findViewById(R.id.img_check)

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    itemClickListener.onItemClick(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.list_item, viewGroup, false)
        return ViewHolder(view, itemClickListener)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val currentItem = dataSet[position]
        viewHolder.imgIcon.setImageResource(currentItem.icon)
        viewHolder.tvTitle.text = currentItem.title
        viewHolder.tvSubtitle.text = currentItem.subtitle
        viewHolder.tvDescription.text = currentItem.description
        viewHolder.imgCheck.visibility = if (currentItem.isChecked) View.VISIBLE else View.GONE
    }

    override fun getItemCount() = dataSet.size
}
