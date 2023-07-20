package com.example.agribotbeta

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import android.content.Context
import androidx.appcompat.app.AlertDialog

class MessageAdapter(private val messages: List<Mensaje>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    companion object {
        private const val VIEW_TYPE_BOT = 1
        private const val VIEW_TYPE_USER = 2
    }

    inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMessage: TextView? = view.findViewById(R.id.tv_message)
        val tvBotMessage: TextView? = view.findViewById(R.id.tv_bot_message)
        val ivBotImage: ImageView? = view.findViewById(R.id.iv_bot_image) //Imagen
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
            if (message.imagen != null) {
                Glide.with(holder.ivBotImage?.context!!)
                    .load(message.imagen).fitCenter()
                    .into(holder.ivBotImage!!)
                holder.ivBotImage.visibility = View.VISIBLE
                holder.ivBotImage.setOnClickListener {
                    showImageDialog(holder.ivBotImage.context, message.imagen)
                }
            } else {
                holder.ivBotImage?.visibility = View.GONE
            }
        } else {
            holder.tvMessage?.text = message.texto
        }
    }

    private fun showImageDialog(context: Context, imageUrl: String) {
        val builder = AlertDialog.Builder(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_image, null)
        val imageView = dialogView.findViewById<ImageView>(R.id.dialog_image_view)

        Glide.with(context)
            .load(imageUrl).fitCenter()
            .into(imageView)

        builder.setView(dialogView)
            .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }
}
