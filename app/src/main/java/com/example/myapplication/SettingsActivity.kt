package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class SettingsActivity : AppCompatActivity() {

    private lateinit var tvNombre: TextView
    private lateinit var tvCorreo: TextView
    private lateinit var tvTelefono: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        // 1. Inicializar vistas
        tvNombre = findViewById(R.id.no)
        tvCorreo = findViewById(R.id.co)
        tvTelefono = findViewById(R.id.te)

        // 2. Cargar datos desde SharedPreferences
        cargarDatosUsuario()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation_settings)
        bottomNav.selectedItemId = R.id.nav_settings


        // Botón para ir a cambiar contraseña
        val btnChangePass = findViewById<RelativeLayout>(R.id.btn_change_password_layout)
        btnChangePass?.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        // Botón para cerrar sesión
        val btnLogout = findViewById<RelativeLayout>(R.id.btn_logout)
        btnLogout?.setOnClickListener {
            cerrarSesion()
        }


        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chat -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }

                R.id.nav_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }

                R.id.nav_settings -> true
                else -> false
            }
        }
    }

    private fun cargarDatosUsuario() {
        // Accedemos a las mismas preferencias que usamos en LoginActivity
        val prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)

        val nombre = prefs.getString("nombre", "No disponible")
        val apellido = prefs.getString("apellidos", "No disponible")
        val correo = prefs.getString("correo", "No disponible")
        val telefono = prefs.getString("telefono", "No disponible")

        // Asignamos a los TextViews
        tvNombre.text = "$nombre $apellido"
        tvCorreo.text = correo
        tvTelefono.text = telefono
    }

    private fun cerrarSesion() {
        // 1. Eliminar los datos guardados en SharedPreferences
        val prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        stopService(Intent(this, NotificationService::class.java)) // <-- AGREGAR ESTO
        // 2. Redirigir al Login y limpiar el stack de actividades
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
