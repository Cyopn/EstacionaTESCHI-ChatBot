package com.example.myapplication

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history)

        container = findViewById(R.id.notifications_container)

        // 1. Cargar notificaciones desde la API
        obtenerHistorial()

        // 2. Navegación inferior
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation_history)
        bottomNav.selectedItemId = R.id.nav_history
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chat -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_history -> true
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

    private fun obtenerHistorial() {
        val userId = LoginActivity.SessionManager.getUserId(this)

        lifecycleScope.launch(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("http://192.168.1.28:8000/api/notifications/?usuario_id=$userId")
                .get()
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    val responseData = response.body?.string()
                    if (response.isSuccessful && responseData != null) {
                        val json = JSONObject(responseData)
                        val array = json.getJSONArray("notifications")

                        withContext(Dispatchers.Main) {
                            container.removeAllViews() // Limpiar "loading" o estáticos
                            for (i in 0 until array.length()) {
                                agregarNotificacionAVista(array.getJSONObject(i))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("HistoryAPI", "Error: ${e.message}")
            }
        }
    }

    private fun agregarNotificacionAVista(notif: JSONObject) {
        val view = LayoutInflater.from(this).inflate(R.layout.item_notification, container, false)

        val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        val tvBody = view.findViewById<TextView>(R.id.tv_body)
        val tvDate = view.findViewById<TextView>(R.id.tv_date)
        val cvIconBg = view.findViewById<CardView>(R.id.cv_icon_bg)
        val ivIcon = view.findViewById<ImageView>(R.id.iv_notification_icon)

        val tipo = notif.getString("tipo")
        tvTitle.text = notif.getString("cuerpo")
        tvBody.text = notif.getString("descripcion")

        // Formatear fecha simple
        val fechaOriginal = notif.getString("fecha_creacion")
        tvDate.text = formatearFecha(fechaOriginal)

        // Configurar estilo según el tipo
        when (tipo) {
            "SALIDA" -> {
                cvIconBg.setCardBackgroundColor(Color.parseColor("#2ECC71"))
                ivIcon.setImageResource(android.R.drawable.ic_lock_power_off)
            }
            "ACCESO_AUTORIZADO" -> {
                cvIconBg.setCardBackgroundColor(Color.parseColor("#F1C40F"))
                ivIcon.setImageResource(android.R.drawable.ic_dialog_map)
            }
            else -> { // OTRO
                cvIconBg.setCardBackgroundColor(Color.parseColor("#3498DB"))
                ivIcon.setImageResource(android.R.drawable.ic_menu_directions)
            }
        }

        container.addView(view)
    }

    private fun formatearFecha(fechaIso: String): String {
        return try {
            // "2026-01-11T09:33:30..." -> "11 Jan, 09:33 AM"
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val formatter = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            val date = parser.parse(fechaIso)
            formatter.format(date!!)
        } catch (e: Exception) {
            fechaIso
        }
    }
}