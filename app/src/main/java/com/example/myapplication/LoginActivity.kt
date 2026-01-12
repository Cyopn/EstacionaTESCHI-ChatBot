package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var etCorreo: EditText
    private lateinit var etContra: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)
        etCorreo = findViewById(R.id.co)
        etContra = findViewById(R.id.con)
        btnLogin = findViewById(R.id.lo)
        tvRegister = findViewById(R.id.tv_go_to_register)
        btnLogin.setOnClickListener {
            if (validarCampos()) {
                ejecutarLogin()
            }
        }
        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validarCampos(): Boolean {
        val correo = etCorreo.text.toString().trim()
        val contra = etContra.text.toString().trim()
        if (correo.isEmpty()) {
            etCorreo.error = "Ingresa tu correo"
            return false
        }
        if (contra.isEmpty()) {
            etContra.error = "Ingresa tu contraseña"
            return false
        }
        return true
    }

    private fun ejecutarLogin() {
        // Usamos lifecycleScope para no bloquear la UI
        lifecycleScope.launch(Dispatchers.IO) {
            val client = OkHttpClient()
            val jsonBody = JSONObject()
            jsonBody.put("correo", etCorreo.text.toString())
            jsonBody.put("contraseña", etContra.text.toString())
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = jsonBody.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url("http://192.168.1.28:8000/api/login/")
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
                                val isAuthenticated = jsonResponse.getBoolean("authenticated")
                                if (isAuthenticated) {
                                    SessionManager.saveUser(this@LoginActivity, jsonResponse)
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Bienvenido ${
                                            jsonResponse.getJSONObject("user").getString("nombre")
                                        }",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    SessionManager.saveUser(this@LoginActivity, jsonResponse)
                                    startService(
                                        Intent(
                                            this@LoginActivity,
                                            NotificationService::class.java
                                        )
                                    )

                                    val intent =
                                        Intent(this@LoginActivity, HomeActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Error de autenticación",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Log.e("login_error", "Error parseando respuesta: ${e.message}")
                            }
                        } else {
                            Log.e("login_error", "Error body: $responseData")
                            Toast.makeText(
                                this@LoginActivity,
                                "Correo o contraseña incorrectos",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("login_error", e.message ?: "Unknown error")
                    Toast.makeText(
                        this@LoginActivity,
                        "Error de conexión con el servidor",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    object SessionManager {
        private const val PREFS_NAME = "user_session"

        fun saveUser(context: android.content.Context, userData: JSONObject) {
            val prefs =
                context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
            val editor = prefs.edit()
            val user = userData.getJSONObject("user")
            editor.putInt("user_id", user.getInt("id"))
            editor.putString("nombre", user.getString("nombre"))
            editor.putString("apellidos", user.getString("apellidos"))
            editor.putString("correo", user.getString("correo"))
            editor.putInt("area_id", user.getInt("area"))
            editor.putString("matricula", user.getString("matricula"))
            editor.putString("telefono", user.getString("telefono"))
            editor.apply()
        }

        fun getUserId(context: android.content.Context): Int {
            val prefs =
                context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
            return prefs.getInt("user_id", -1)
        }
    }

}
