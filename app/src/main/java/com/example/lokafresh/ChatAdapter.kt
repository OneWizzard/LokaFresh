package com.example.lokafresh

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val messageList: List<Message>) :
    RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    companion object {
        const val VIEW_TYPE_USER = 1
        const val VIEW_TYPE_BOT = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (messageList[position].isUser) VIEW_TYPE_USER else VIEW_TYPE_BOT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_USER) {
            val view = layoutInflater.inflate(R.layout.item_user_message, parent, false)
            UserMessageViewHolder(view)
        } else {
            val view = layoutInflater.inflate(R.layout.item_bot_message, parent, false)
            BotMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messageList[position]
        if (holder is UserMessageViewHolder) {
            holder.messageTextView.text = message.text
        } else if (holder is BotMessageViewHolder) {
            holder.messageTextView.text = message.text
        }
    }

    override fun getItemCount() = messageList.size

    abstract class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTextView: TextView = itemView.findViewById(android.R.id.text1) // Gunakan ID standar text1 atau buat ID sendiri di layout item
    }

    class UserMessageViewHolder(itemView: View) : MessageViewHolder(itemView)
    class BotMessageViewHolder(itemView: View) : MessageViewHolder(itemView)
}