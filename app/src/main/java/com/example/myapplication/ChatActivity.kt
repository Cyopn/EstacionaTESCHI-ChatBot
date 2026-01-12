package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class ChatActivity : AppCompatActivity() {

    private lateinit var chatContainer: LinearLayout
    private lateinit var etMessage: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var scrollView: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat)

        // 1. Inicializar vistas
        chatContainer = findViewById(R.id.chat_container)
        etMessage = findViewById(R.id.et_message)
        sendButton = findViewById(R.id.send_button)
        scrollView = findViewById(R.id.scroll_view_chat)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation_chat)
        bottomNav.selectedItemId = R.id.nav_chat

        // 2. Enviar mensaje inicial al cargar la vista
        enviarMensajeApi("hola")

        // 3. Evento del botón enviar
        sendButton.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                agregarMensajeUsuario(text)
                enviarMensajeApi(text)
                etMessage.text.clear()
            }
        }

        // Botón Back (Atrás)
        findViewById<ImageView>(R.id.btn_back_chat).setOnClickListener { onBackPressed() }

        // Navegación inferior
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chat -> true
                R.id.nav_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }

                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }

                else -> false
            }
        }
    }

    private fun agregarMensajeUsuario(mensaje: String) {
        val textView = TextView(this)
        textView.text = mensaje
        textView.setTextColor(ContextCompat.getColor(this, R.color.white))
        textView.setPadding(35, 25, 35, 25)
        textView.background = ContextCompat.getDrawable(this, R.drawable.bubble_user)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.gravity = Gravity.END
        params.setMargins(100, 10, 0, 15)
        textView.layoutParams = params

        chatContainer.addView(textView)
        scrollToBottom()
    }

    private fun agregarMensajeBot(mensaje: String) {
        // Inflar el diseño de la burbuja del bot (con imagen)
        val view = LayoutInflater.from(this).inflate(R.layout.item_message_bot, chatContainer, false)
        val tvBotMessage = view.findViewById<TextView>(R.id.tv_bot_message)
        tvBotMessage.text = mensaje
        
        chatContainer.addView(view)
        scrollToBottom()
    }

    private fun scrollToBottom() {
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    private fun enviarMensajeApi(mensaje: String) {
        // Obtener el ID de usuario desde SharedPreferences usando el SessionManager definido en LoginActivity
        val userId = LoginActivity.SessionManager.getUserId(this)

        lifecycleScope.launch(Dispatchers.IO) {
            val client = OkHttpClient()

            val jsonBody = JSONObject()
            jsonBody.put("message", mensaje)
            jsonBody.put("usuario_id", userId) // Pasar el ID de usuario

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = jsonBody.toString().toRequestBody(mediaType)

            // Usando el endpoint proporcionado
            val request = Request.Builder()
                .url("http://192.168.1.28:8000/api/chat/")
                .post(body)
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    val responseData = response.body?.string()
                    val isSuccessful = response.isSuccessful

                    withContext(Dispatchers.Main) {
                        if (isSuccessful && responseData != null) {
                            try {
                                val jsonResponse = JSONObject(responseData)
                                // Ajustado para usar la llave "texto" según el nuevo formato
                                val botReply = jsonResponse.optString("texto", "Sin respuesta")
                                agregarMensajeBot(botReply)
                            } catch (e: Exception) {
                                Log.e("chat_api", "Error parseando respuesta: ${e.message}")
                            }
                        } else {
                            Log.e("chat_api", "Error en respuesta: $responseData")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("chat_api", "Error de conexión: ${e.message}")
                }
            }
        }
    }

}
