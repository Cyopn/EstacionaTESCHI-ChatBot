package com.example.myapplication

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.assistant_home)

        val btnStartChat = findViewById<Button>(R.id.btn_start_chat)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Botón Iniciar Chat
        btnStartChat.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

        // Configuración de la barra de navegación inferior
        bottomNav.selectedItemId = R.id.nav_chat // El icono de chat es el "Home" en este caso
        // Dentro de onCreate de HomeActivity.kt
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chat -> true // Ya estamos aquí
                R.id.nav_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish() // Cerramos esta para que no se amontonen
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
}
