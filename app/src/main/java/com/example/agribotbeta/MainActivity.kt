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
import com.google.firebase.firestore.FirebaseFirestoreSettings
import java.text.Normalizer

class MainActivity : AppCompatActivity() {

    var problemaActual: String? = null
    val solucionesPorProblema = mutableMapOf<String, List<Map<String, Any>>>()

    // Mapeo de problemas a los índices de sus próximas soluciones
    val siguienteSolucionPorProblema = mutableMapOf<String, Int>()

    //Variable de conexion firebase
    private val db = FirebaseFirestore.getInstance()

    //Se inicializa la configuracion para que el chache almacene la base de datos sin internet
    init {
        val settings = FirebaseFirestoreSettings.Builder()
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
        db.firestoreSettings = settings
    }

    // Esta es la lista de mensajes
    val mensajes = mutableListOf<Mensaje>()

    // Aquí está el adaptador RecyclerView
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

        // Agregar un mensaje inicial del bot
        mensajes.add(Mensaje("Hola, soy tu asistente. Puedo ayudarte a solucionar problemas. Por favor, describe tu problema.", true))
        adaptadorRecyclerView?.notifyDataSetChanged()
    }

    // Se normaliza el codigo para que el texto ingresado sea indiferente de errores otograficos
    fun String.normalize(): String {
        val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
        return Regex("[^\\p{ASCII}]").replace(temp, "")
    }
    private fun enviarMensaje(texto: String) {
        mensajes.add(Mensaje(texto, false))
        generarRespuestaBot(texto)
        adaptadorRecyclerView?.notifyDataSetChanged()
        // Agregamos la función de desplazamiento al último mensaje después de cada envío
        rvMessages.scrollToPosition(mensajes.size - 1)
    }


    private fun generarRespuestaBot(texto: String) {
        if (problemaActual != null) {
            // Hay un problema actual, así que interpretamos la entrada como una respuesta a la pregunta de si la solución funcionó
            if (texto.normalize().equals("no", ignoreCase = true)) {
                // La solución no funcionó, así que sugerimos la próxima
                val nextSolutionIndex = siguienteSolucionPorProblema.getOrDefault(problemaActual!!, 0) // Índice de la próxima solución
                val soluciones = solucionesPorProblema[problemaActual!!]!!
                if (nextSolutionIndex < soluciones.size) {
                    // Si hay una próxima solución, la sugerimos
                    val solucion = soluciones[nextSolutionIndex]
                    val solucionTexto = solucion["texto"] as String
                    mensajes.add(Mensaje("$solucionTexto\n¿Se solucionó tu problema? Responde con 'sí' o 'no'.", true))
                    adaptadorRecyclerView?.notifyDataSetChanged()
                    rvMessages.scrollToPosition(mensajes.size - 1)
                    // Actualizamos el índice de la próxima solución
                    siguienteSolucionPorProblema[problemaActual!!] = nextSolutionIndex + 1
                } else {
                    // Si no hay más soluciones, agregamos un mensaje indicando esto
                    mensajes.add(Mensaje("Lo siento, no tengo más sugerencias para este problema. Porfavor comunicate con el departamento de sistemas.", true))
                    adaptadorRecyclerView?.notifyDataSetChanged()
                    rvMessages.scrollToPosition(mensajes.size - 1)
                }
            } else if (texto.normalize().equals("si", ignoreCase = true)) {
                // La solución funcionó, por lo que podemos terminar la interacción con respecto a este problema
                mensajes.add(Mensaje("¡Genial! Me alegra haber podido ayudar. Si tienes algún otro problema, no dudes en preguntar.", true))
                adaptadorRecyclerView?.notifyDataSetChanged()
                rvMessages.scrollToPosition(mensajes.size - 1)
                problemaActual = null // Reseteamos el problema actual
            } else {
                // La entrada no fue ni "sí" ni "no", así que le pedimos al usuario que responda correctamente
                mensajes.add(Mensaje("Por favor, responde con 'sí' o 'no'. ¿Se solucionó tu problema?", true))
                adaptadorRecyclerView?.notifyDataSetChanged()
                rvMessages.scrollToPosition(mensajes.size - 1)
            }
        } else {
            // No hay un problema actual, así que interpretamos la entrada como una nueva consulta
            db.collection("Problemas")
                .get()
                .addOnSuccessListener { result ->
                    var foundKeyword = false // Variable para controlar si encontramos una palabra clave
                    loop@ for (document in result) {
                        val palabrasClave = document.get("palabras clave") as List<String>
                        for (palabraClave in palabrasClave) {
                            if (texto.normalize().contains(palabraClave.normalize(), ignoreCase = true)) {
                                foundKeyword = true // Encontramos una palabra clave
                                val soluciones = (document.get("soluciones") as List<Map<String, Any>>).sortedBy { (it["orden"] as String).toInt() }
                                problemaActual = document.id // El nombre del problema
                                solucionesPorProblema[problemaActual!!] = soluciones
                                siguienteSolucionPorProblema[problemaActual!!] = 0
                                // Sugerimos la primera solución
                                val solucionTexto = soluciones[0]["texto"] as String
                                mensajes.add(Mensaje("$solucionTexto\n¿Se solucionó tu problema? Responde con 'sí' o 'no'.", true))
                                adaptadorRecyclerView?.notifyDataSetChanged()
                                rvMessages.scrollToPosition(mensajes.size - 1)
                                // Actualizamos el índice de la próxima solución
                                siguienteSolucionPorProblema[problemaActual!!] = 1
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



}
