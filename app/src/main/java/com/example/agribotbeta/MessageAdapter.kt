package com.example.agribotbeta

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter(private val messages: List<Mensaje>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMessage: TextView = view.findViewById(R.id.tv_message)
        val tvBotMessage: TextView = view.findViewById(R.id.tv_bot_message)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.message_item, parent, false)

        return MessageViewHolder(view)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        if (message.esBot) {
            holder.tvMessage.visibility = View.GONE
            holder.tvBotMessage.visibility = View.VISIBLE
            holder.tvBotMessage.text = message.texto
        } else {
            holder.tvMessage.visibility = View.VISIBLE
            holder.tvBotMessage.visibility = View.GONE
            holder.tvMessage.text = message.texto
        }
    }
}
