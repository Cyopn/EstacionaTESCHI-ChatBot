package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
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

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var etNuevaContra: EditText
    private lateinit var etConfirmarContra: EditText
    private lateinit var btnCambiar: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.change_password)

        // 1. Inicializar vistas
        etNuevaContra = findViewById(R.id.con)
        etConfirmarContra = findViewById(R.id.con2)
        btnCambiar = findViewById(R.id.btn_change_password)

        // 3. Botón Cambiar Contraseña
        btnCambiar.setOnClickListener {
            if (validarCampos()) {
                ejecutarCambioPassword()
            }
        }

        // 4. Configurar Navegación Inferior
        setupBottomNavigation()
    }

    private fun validarCampos(): Boolean {
        val pass = etNuevaContra.text.toString().trim()
        val confirmPass = etConfirmarContra.text.toString().trim()

        if (pass.isEmpty()) {
            etNuevaContra.error = "Ingresa la nueva contraseña"
            return false
        }
        if (pass.length < 6) {
            etNuevaContra.error = "La contraseña debe tener al menos 6 caracteres"
            return false
        }
        if (confirmPass.isEmpty()) {
            etConfirmarContra.error = "Confirma tu contraseña"
            return false
        }
        if (pass != confirmPass) {
            etConfirmarContra.error = "Las contraseñas no coinciden"
            return false
        }
        return true
    }

    private fun ejecutarCambioPassword() {
        // Obtener matrícula de SharedPreferences
        val prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val matricula = prefs.getString("matricula", null)

        if (matricula == null) {
            Toast.makeText(this, "Error: No se encontró la sesión", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val client = OkHttpClient()

            val jsonBody = JSONObject()
            jsonBody.put("matricula", matricula)
            jsonBody.put("nueva_contraseña", etNuevaContra.text.toString())

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = jsonBody.toString().toRequestBody(mediaType)

            // Cambia la URL por tu endpoint real de cambio de contraseña
            val request = Request.Builder()
                .url("http://192.168.1.28:8000/api/users/")
                .put(body)
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    val isSuccessful = response.isSuccessful
                    withContext(Dispatchers.Main) {
                        if (isSuccessful) {
                            Toast.makeText(
                                this@ChangePasswordActivity,
                                "Contraseña actualizada con éxito",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish() // Regresa a la pantalla anterior
                        } else {
                            Toast.makeText(
                                this@ChangePasswordActivity,
                                "Error al cambiar contraseña",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("ChangePass", "Error: ${e.message}")
                    Toast.makeText(
                        this@ChangePasswordActivity,
                        "Error de conexión",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation_change)
        bottomNav.selectedItemId = R.id.nav_settings

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chat -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }

                R.id.nav_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    finish()
                    true
                }

                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    finish()
                    true
                }

                else -> false
            }
        }
    }
}