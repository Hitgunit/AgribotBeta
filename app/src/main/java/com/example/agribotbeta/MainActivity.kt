package com.example.agribotbeta

import android.content.ContentValues.TAG
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.SetOptions
import java.text.Normalizer

class MainActivity : AppCompatActivity() {

    // Variable opcional que guarda el problema actual (puede ser null)
    var problemaActual: String? = null

    // Mapeo de problemas a sus respectivas soluciones (cada solución es un mapa de String a Any)
    val solucionesPorProblema = mutableMapOf<String, List<Map<String, Any>>>()

    // Mapeo de problemas a los índices de sus próximas soluciones
    val siguienteSolucionPorProblema = mutableMapOf<String, Int>()

    // Variable para gestionar la conexión a Firebase (Firestore)
    private val db = FirebaseFirestore.getInstance()

    // Esta es la lista de mensajes
    val mensajes = mutableListOf<Mensaje>()

    // Aquí está el adaptador RecyclerView
    private var adaptadorRecyclerView: MessageAdapter? = null

    // Inicializamos el RecyclerView aquí
    private lateinit var rvMessages: RecyclerView

    //Se inicializa la configuracion para que el chache almacene la base de datos sin internet
    init {
        val settings = FirebaseFirestoreSettings.Builder()
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
        db.firestoreSettings = settings
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicialización del adaptadorRecyclerView
        adaptadorRecyclerView = MessageAdapter(mensajes)

        // Configuración del RecyclerView
        rvMessages = findViewById<RecyclerView>(R.id.rv_messages)
        rvMessages.adapter = adaptadorRecyclerView
        rvMessages.layoutManager = LinearLayoutManager(this)

        // Inicialización de los botones y campos de texto
        val btnSend = findViewById<Button>(R.id.btn_send)
        val etMessage = findViewById<EditText>(R.id.et_message)

        // Configuración del botón de enviar mensaje
        btnSend.setOnClickListener {
            val messageText = etMessage.text.toString() // Obtener el texto del mensaje
            etMessage.text.clear() // Limpiar el campo de texto
            enviarMensaje(messageText) // Llamar a la función para enviar el mensaje
        }

        // Agregar un mensaje inicial del bot desde Firebase
        db.collection("Soporte")
            .document("Bienvenida")
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val texto = document.getString("texto")?.replace("\\n", "\n") // Obtener y procesar el texto del mensaje
                    if (texto != null) {
                        mensajes.add(Mensaje(texto, true)) // Agregar el mensaje a la lista
                        adaptadorRecyclerView?.notifyDataSetChanged() // Notificar al adaptador sobre los cambios
                        rvMessages.scrollToPosition(mensajes.size - 1) // Desplazar el RecyclerView a la última posición
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting document.", exception) // Registrar error en caso de falla
            }
    }



    // Se normaliza el codigo para que el texto ingresado sea indiferente de errores otograficos
    fun String.normalize(): String {
        val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
        return Regex("[^\\p{ASCII}]").replace(temp, "")
    }

    // Función para enviar un mensaje desde el usuario
    private fun enviarMensaje(texto: String) {
        // Añadimos el mensaje enviado por el usuario a la lista de mensajes
        mensajes.add(Mensaje(texto, false))

        // Generamos una respuesta del bot basada en el texto del mensaje del usuario
        generarRespuestaBot(texto)

        // Notificamos al adaptador del RecyclerView que los datos han cambiado para que se pueda actualizar la interfaz
        adaptadorRecyclerView?.notifyDataSetChanged()

        // Nos aseguramos de que el RecyclerView se desplace hasta el último mensaje después de cada envío
        // para que el usuario pueda ver su propio mensaje y la respuesta del bot
        rvMessages.scrollToPosition(mensajes.size - 1)
    }



    private fun generarRespuestaBot(texto: String) {
        //Se crea la variable para poder retrasar el mensaje
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            if (problemaActual != null) {
                // Hay un problema actual, así que interpretamos la entrada como una respuesta a la pregunta de si la solución funcionó
                if (texto.normalize().equals("no", ignoreCase = true)) {
                    // La solución no funcionó, así que sugerimos la próxima
                    val nextSolutionIndex = siguienteSolucionPorProblema.getOrDefault(
                        problemaActual!!,
                        0
                    ) // Índice de la próxima solución
                    val soluciones = solucionesPorProblema[problemaActual!!]!!
                    if (nextSolutionIndex < soluciones.size) {
                        // Si hay una próxima solución, la sugerimos
                        val solucion = soluciones[nextSolutionIndex]
                        val solucionTexto = (solucion["texto"] as String)?.replace("\\n", "\n")
                        val solucionImagen = solucion["imagen"] as String?
                        //Validacion de solucion
                        db.collection("Soporte")
                            .document("Validacion")
                            .get()
                            .addOnSuccessListener { document ->
                                if (document != null) {
                                    val texto = document.getString("texto")?.replace("\\n", "\n")
                                    if (texto != null) {
                                        mensajes.add(
                                            Mensaje(
                                                "$solucionTexto \n" + texto,
                                                true,
                                                solucionImagen
                                            )
                                        )
                                        adaptadorRecyclerView?.notifyDataSetChanged()
                                        rvMessages.scrollToPosition(mensajes.size - 1)
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.w(TAG, "Error getting document.", exception)
                            }
                        // Actualizamos el índice de la próxima solución
                        siguienteSolucionPorProblema[problemaActual!!] = nextSolutionIndex + 1
                    } else {
                        // Si no hay más soluciones, agregamos un mensaje indicando esto
                        //Validacion de solucion
                        db.collection("Soporte")
                            .document("ValidacionFinal")
                            .get()
                            .addOnSuccessListener { document ->
                                if (document != null) {
                                    val texto = document.getString("texto")?.replace("\\n", "\n")
                                    if (texto != null) {
                                        mensajes.add(Mensaje(texto, true))
                                        adaptadorRecyclerView?.notifyDataSetChanged()
                                        rvMessages.scrollToPosition(mensajes.size - 1)
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.w(TAG, "Error getting document.", exception)
                            }
                        problemaActual = null // Reseteamos el problema actual
                    }
                } else if (texto.normalize().equals("si", ignoreCase = true)) {
                    // La solución funcionó, por lo que podemos terminar la interacción con respecto a este problema
                    //Validacion de solucion
                    db.collection("Soporte")
                        .document("ValidacionCorrecta")
                        .get()
                        .addOnSuccessListener { document ->
                            if (document != null) {
                                val texto = document.getString("texto")?.replace("\\n", "\n")
                                if (texto != null) {
                                    mensajes.add(Mensaje(texto, true))
                                    adaptadorRecyclerView?.notifyDataSetChanged()
                                    rvMessages.scrollToPosition(mensajes.size - 1)
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.w(TAG, "Error getting document.", exception)
                        }
                    problemaActual = null // Reseteamos el problema actual
                } else {
                    // La entrada no fue ni "sí" ni "no", así que le pedimos al usuario que responda correctamente
                    //Validacion de solucion
                    db.collection("Soporte")
                        .document("ValidacionErronea")
                        .get()
                        .addOnSuccessListener { document ->
                            if (document != null) {
                                val texto = document.getString("texto")?.replace("\\n", "\n")
                                if (texto != null) {
                                    mensajes.add(Mensaje(texto, true))
                                    adaptadorRecyclerView?.notifyDataSetChanged()
                                    rvMessages.scrollToPosition(mensajes.size - 1)
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.w(TAG, "Error getting document.", exception)
                        }
                }
            } else {
                // No hay un problema actual, así que interpretamos la entrada como una nueva consulta
                if (texto.normalize().equals("ayuda", ignoreCase = true)) {
                    // El usuario ha solicitado ayuda, así que buscamos el documento de ayuda y mostramos su contenido
                    db.collection("Soporte")
                        .document("Ayuda")
                        .get()
                        .addOnSuccessListener { document ->
                            if (document != null) {
                                val texto = document.getString("texto")?.replace("\\n", "\n")
                                if (texto != null) {
                                    mensajes.add(Mensaje(texto, true))
                                    adaptadorRecyclerView?.notifyDataSetChanged()
                                    rvMessages.scrollToPosition(mensajes.size - 1)
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.w(TAG, "Error getting document.", exception)
                        }
                } else {
                    // No hay un problema actual, así que interpretamos la entrada como una nueva consulta
                    db.collection("Problemas")
                        .get()
                        .addOnSuccessListener { result ->
                            var foundKeyword =
                                false // Variable para controlar si encontramos una palabra clave
                            loop@ for (document in result) {
                                val palabrasClave = document.get("palabras clave") as List<String>
                                for (palabraClave in palabrasClave) {
                                    if (texto.normalize()
                                            .contains(palabraClave.normalize(), ignoreCase = true)
                                    ) {
                                        foundKeyword = true // Encontramos una palabra clave
                                        val soluciones =
                                            (document.get("soluciones") as List<Map<String, Any>>).sortedBy { (it["orden"] as String).toInt() }
                                        problemaActual = document.id // El nombre del problema
                                        solucionesPorProblema[problemaActual!!] = soluciones
                                        siguienteSolucionPorProblema[problemaActual!!] = 0
                                        // Sugerimos la primera solución
                                        val solucionTexto = (soluciones[0]["texto"] as String)?.replace("\\n", "\n")
                                        val solucionImagen = soluciones[0]["imagen"] as String?
                                        //Validacion de solucion
                                        db.collection("Soporte")
                                            .document("Validacion")
                                            .get()
                                            .addOnSuccessListener { document ->
                                                if (document != null) {
                                                    val texto = document.getString("texto")?.replace("\\n", "\n")
                                                    if (texto != null) {
                                                        mensajes.add(
                                                            Mensaje(
                                                                "$solucionTexto\n" + texto,
                                                                true,
                                                                solucionImagen
                                                            )
                                                        )
                                                        adaptadorRecyclerView?.notifyDataSetChanged()
                                                        rvMessages.scrollToPosition(mensajes.size - 1)
                                                    }
                                                }
                                            }
                                            .addOnFailureListener { exception ->
                                                Log.w(TAG, "Error getting document.", exception)
                                            }
                                        // Actualizamos el índice de la próxima solución
                                        siguienteSolucionPorProblema[problemaActual!!] = 1
                                        // Saliendo del loop una vez que se ha encontrado la palabra clave en el texto
                                        break@loop
                                    }
                                }
                            }
                            // Si no encontramos ninguna palabra clave, agregamos un mensaje predeterminado
                            if (!foundKeyword) {

                                db.collection("Soporte")
                                    .document("Error")
                                    .get()
                                    .addOnSuccessListener { document ->
                                        if (document != null) {
                                            val texto = document.getString("texto")?.replace("\\n", "\n")
                                            if (texto != null) {
                                                mensajes.add(Mensaje(texto, true))
                                                adaptadorRecyclerView?.notifyDataSetChanged()
                                                rvMessages.scrollToPosition(mensajes.size - 1)
                                            }
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.w(TAG, "Error getting document.", exception)
                                    }
                                val data = hashMapOf("texto" to texto.toString())
                                db.collection("Diccionario")
                                    .add(data)
                                    .addOnSuccessListener { documentReference ->
                                        Log.d(
                                            TAG,
                                            "DocumentSnapshot written with ID: ${documentReference.id}"
                                        )
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w(TAG, "Error adding document", e)
                                    }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.w(TAG, "Error getting documents.", exception)
                        }

                }


            }
        }, 500)

    }


}
