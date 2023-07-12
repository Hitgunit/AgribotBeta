package com.example.agribotbeta

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    // Esta es la lista de mensajes que inicialmente está vacía
    val mensajes = mutableListOf<Mensaje>()

    // Aquí está tu adaptador RecyclerView inicialmente nulo
    private var adaptadorRecyclerView: MessageAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ahora inicializamos el adaptadorRecyclerView
        adaptadorRecyclerView = MessageAdapter(mensajes)

        val rvMessages = findViewById<RecyclerView>(R.id.rv_messages)
        rvMessages.adapter = adaptadorRecyclerView
        rvMessages.layoutManager = LinearLayoutManager(this)

        val btnSend = findViewById<Button>(R.id.btn_send)
        val etMessage = findViewById<EditText>(R.id.et_message)

        btnSend.setOnClickListener {
            val messageText = etMessage.text.toString()
            etMessage.text.clear()
            enviarMensaje(messageText)
        }
    }

    private fun enviarMensaje(texto: String) {
        mensajes.add(Mensaje(texto, false))
        val respuestaBot = generarRespuestaBot(texto)
        mensajes.add(Mensaje(respuestaBot, true))
        adaptadorRecyclerView?.notifyDataSetChanged()
    }

    private fun generarRespuestaBot(texto: String): String {
        return when {
            texto.contains("Hola") -> "Hello"
            texto.contains("Quien es?") -> "Un bot bebe"
            else -> "Lo siento, no entiendo tu problema. Por favor proporciona más detalles."
        }
    }
}
