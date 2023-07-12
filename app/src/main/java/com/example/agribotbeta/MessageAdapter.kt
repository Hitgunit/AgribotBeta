package com.example.agribotbeta

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter(private val messages: List<Mensaje>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    companion object {
        private const val VIEW_TYPE_BOT = 1
        private const val VIEW_TYPE_USER = 2
    }

    inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMessage: TextView? = view.findViewById(R.id.tv_message)
        val tvBotMessage: TextView? = view.findViewById(R.id.tv_bot_message)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = if (viewType == VIEW_TYPE_BOT) {
            LayoutInflater.from(parent.context)
                .inflate(R.layout.message_item_bot, parent, false)
        } else {
            LayoutInflater.from(parent.context)
                .inflate(R.layout.message_item_user, parent, false)
        }
        return MessageViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].esBot) {
            VIEW_TYPE_BOT
        } else {
            VIEW_TYPE_USER
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        if (message.esBot) {
            holder.tvBotMessage?.text = message.texto
        } else {
            holder.tvMessage?.text = message.texto
        }
    }
}

