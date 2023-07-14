package com.example.agribotbeta

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    //Variable de conexion firebase
    private val db = FirebaseFirestore.getInstance()

    // Esta es la lista de mensajes que inicialmente está vacía
    val mensajes = mutableListOf<Mensaje>()

    // Aquí está tu adaptador RecyclerView inicialmente nulo
    private var adaptadorRecyclerView: MessageAdapter? = null

    // Inicializamos el RecyclerView aquí
    private lateinit var rvMessages: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ahora inicializamos el adaptadorRecyclerView
        adaptadorRecyclerView = MessageAdapter(mensajes)

        rvMessages = findViewById<RecyclerView>(R.id.rv_messages)
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
        generarRespuestaBot(texto)
        adaptadorRecyclerView?.notifyDataSetChanged()
        // Agregamos la función de desplazamiento al último mensaje después de cada envío
        rvMessages.scrollToPosition(mensajes.size - 1)
    }


    private fun generarRespuestaBot(texto: String) {
        db.collection("Problemas")
            .get()
            .addOnSuccessListener { result ->
                var foundKeyword = false // Variable para controlar si encontramos una palabra clave
                loop@ for (document in result) {
                    val palabrasClave = document.get("palabras clave") as List<String>
                    for (palabraClave in palabrasClave) {
                        if (texto.contains(palabraClave)) {
                            foundKeyword = true // Encontramos una palabra clave
                            val soluciones = (document.get("soluciones") as List<Map<String, Any>>).sortedBy { (it["orden"] as String).toInt() }
                            for (solucion in soluciones) {
                                // Aquí tienes el texto de la solución
                                val solucionTexto = solucion["texto"] as String
                                // Agrega la solución como un mensaje del bot
                                mensajes.add(Mensaje(solucionTexto, true))
                                adaptadorRecyclerView?.notifyDataSetChanged()
                                rvMessages.scrollToPosition(mensajes.size - 1)
                            }
                            // Saliendo del loop una vez que se ha encontrado la palabra clave en el texto
                            break@loop
                        }
                    }
                }
                // Si no encontramos ninguna palabra clave, agregamos un mensaje predeterminado
                if (!foundKeyword) {
                    mensajes.add(Mensaje("Lo siento, no puedo ayudarte con eso.", true))
                    adaptadorRecyclerView?.notifyDataSetChanged()
                    rvMessages.scrollToPosition(mensajes.size - 1)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }





}

