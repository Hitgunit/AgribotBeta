package com.example.agribotbeta

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import android.content.Context
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.github.chrisbanes.photoview.PhotoView

class MessageAdapter(private val messages: List<Mensaje>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    // Constantes para determinar el tipo de vista (bot o usuario)
    companion object {
        private const val VIEW_TYPE_BOT = 1
        private const val VIEW_TYPE_USER = 2
    }

    // ViewHolder interno para mantener las referencias a las vistas dentro de cada elemento de la lista
    inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMessage: TextView? = view.findViewById(R.id.tv_message) // Texto del usuario
        val tvBotMessage: TextView? = view.findViewById(R.id.tv_bot_message) // Texto del bot
        val ivBotImage: ImageView? = view.findViewById(R.id.iv_bot_image) // Imagen
    }

    // Crear un nuevo ViewHolder para inflar el diseño correspondiente según sea un mensaje del bot o del usuario
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = if (viewType == VIEW_TYPE_BOT) {
            // Si es un mensaje del bot, inflar el diseño del bot
            LayoutInflater.from(parent.context)
                .inflate(R.layout.message_item_bot, parent, false)
        } else {
            // Si es un mensaje del usuario, inflar el diseño del usuario
            LayoutInflater.from(parent.context)
                .inflate(R.layout.message_item_user, parent, false)
        }
        return MessageViewHolder(view)
    }


    // Determinar el tipo de vista en función de si el mensaje fue enviado por el bot o el usuario
    override fun getItemViewType(position: Int): Int {
        return if (messages[position].esBot) {
            // Si el mensaje en la posición dada fue enviado por el bot, retornar la constante para la vista del bot
            VIEW_TYPE_BOT
        } else {
            // Si el mensaje en la posición dada fue enviado por el usuario, retornar la constante para la vista del usuario
            VIEW_TYPE_USER
        }
    }


    // Devolver el tamaño de la lista de mensajes
    override fun getItemCount(): Int {
        return messages.size
    }

    // Vincular los datos del mensaje a las vistas dentro del ViewHolder
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        // Obtener el mensaje en la posición dada
        val message = messages[position]

        // Si el mensaje fue enviado por el bot
        if (message.esBot) {
            // Establecer el texto del mensaje en la vista de texto del bot
            holder.tvBotMessage?.text = message.texto

            // Si hay una imagen en el mensaje
            if (message.imagen != null) {
                // Utilizar la biblioteca Glide para cargar y ajustar la imagen en la vista de imagen del bot
                Glide.with(holder.ivBotImage?.context!!)
                    .load(message.imagen).fitCenter()
                    .into(holder.ivBotImage!!)
                // Hacer visible la vista de imagen
                holder.ivBotImage.visibility = View.VISIBLE

                // Configurar un oyente para mostrar un diálogo con la imagen completa cuando se hace clic en la miniatura
                holder.ivBotImage.setOnClickListener {
                    showImageDialog(holder.ivBotImage.context, message.imagen)
                }
            } else {
                // Ocultar la vista de imagen si no hay una imagen en el mensaje
                holder.ivBotImage?.visibility = View.GONE
            }
        } else {
            // Si el mensaje fue enviado por el usuario, establecer el texto del mensaje en la vista de texto del usuario
            holder.tvMessage?.text = message.texto
        }
    }


    // Función para mostrar un diálogo con una imagen ampliada cuando se hace clic en una miniatura de imagen
    private fun showImageDialog(context: Context, imageUrl: String) {
        // Crear un constructor de diálogo con el contexto actual
        val builder = AlertDialog.Builder(context)
        // Inflar la vista del diálogo desde el recurso XML correspondiente
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_image, null)
        // Encontrar la vista photo dentro del diseño inflado
        val photoView = dialogView.findViewById<PhotoView>(R.id.dialog_image_view)
        //
        val imageView = dialogView.findViewById<ImageView>(R.id.zoom_image)

        // Utilizar la biblioteca Glide para cargar la imagen desde la URL, ajustarla al centro y colocarla en la vista de la foto
        Glide.with(context)
            .load(imageUrl).fitCenter()
            .into(photoView)

        Glide.with(context)
            .asGif()
            .load(R.drawable._zoom)
            .into(imageView)


        // Configurar el cuadro de diálogo con la vista preparada y un botón "OK" para cerrarlo
        builder.setView(dialogView)
            .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

}

