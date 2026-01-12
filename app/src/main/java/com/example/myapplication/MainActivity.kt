package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash)

        // Espera 2 segundos y decide a dónde ir
        Handler(Looper.getMainLooper()).postDelayed({

            // Usamos el SessionManager que definiste en LoginActivity
            val userId = LoginActivity.SessionManager.getUserId(this)
// Dentro de MainActivity.kt, después de comprobar que el ID existe:
            if (userId != -1) {
                startService(Intent(this, NotificationService::class.java)) // <-- AGREGAR ESTO
                startActivity(Intent(this, HomeActivity::class.java))
            }
            if (userId != -1) {
                // Si el ID es diferente de -1, el usuario ya está logueado
                startActivity(Intent(this, HomeActivity::class.java))
            } else {
                // Si no hay ID guardado, va al Login
                startActivity(Intent(this, LoginActivity::class.java))
            }

            finish() // Cierra el splash para que no se pueda volver atrás
        }, 2000)
    }
}