package com.example.myapplication

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

class RegisterActivity : AppCompatActivity() {
    private lateinit var sp: Spinner
    private lateinit var btnRegister: Button

    // Declarar los campos de texto
    private lateinit var etNombre: EditText
    private lateinit var etApellido: EditText
    private lateinit var etMatricula: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etContra: EditText
    private lateinit var etContraConfirm: EditText
    private lateinit var etTelefono: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        // 1. Inicializar vistas
        sp = findViewById(R.id.ar)
        btnRegister = findViewById(R.id.re)
        etNombre = findViewById(R.id.no)
        etApellido = findViewById(R.id.ap)
        etMatricula = findViewById(R.id.ma)
        etCorreo = findViewById(R.id.co)
        etContra = findViewById(R.id.con)
        etContraConfirm = findViewById(R.id.con2)
        etTelefono = findViewById(R.id.te)

        // 2. Cargar áreas en el Spinner
        cargarAreas()

        // 3. Evento del botón Registrarse
        btnRegister.setOnClickListener {
            if (validarCampos()) {
                enviarRegistro()
            }
        }
    }

    private fun validarCampos(): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

        if (etNombre.text.isEmpty()) {
            etNombre.error = "Requerido"; return false
        }
        if (etApellido.text.isEmpty()) {
            etApellido.error = "Requerido"; return false
        }
        if (etMatricula.text.isEmpty()) {
            etMatricula.error = "Requerido"; return false
        }
        if (!etCorreo.text.toString().matches(emailPattern.toRegex())) {
            etCorreo.error = "Correo inválido"; return false
        }
        if (etContra.text.length < 6) {
            etContra.error = "Mínimo 6 caracteres"; return false
        }
        if (etContra.text.toString() != etContraConfirm.text.toString()) {
            etContraConfirm.error = "Las contraseñas no coinciden"; return false
        }
        if (etTelefono.text.isEmpty()) {
            etTelefono.error = "Requerido"; return false
        }

        return true
    }

    private fun enviarRegistro() {
        lifecycleScope.launch(Dispatchers.IO) {
            val client = OkHttpClient()

            // Obtener el objeto Area seleccionado
            val areaSeleccionada = sp.selectedItem as? Area

            // --- CREACIÓN DEL JSON ---
            val jsonBody = JSONObject()
            jsonBody.put("nombre", etNombre.text.toString())
            jsonBody.put("apellidos", etApellido.text.toString())
            jsonBody.put("matricula", etMatricula.text.toString())
            jsonBody.put("correo", etCorreo.text.toString())
            jsonBody.put("contraseña", etContra.text.toString())
            jsonBody.put("telefono", etTelefono.text.toString())

            // Enviamos el ID en lugar del nombre
            jsonBody.put("area", areaSeleccionada?.id ?: 0)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = jsonBody.toString().toRequestBody(mediaType)

            // ... resto del código del Request ...


            val request = Request.Builder()
                .url("http://192.168.1.28:8000/api/users/") // Cambia por tu endpoint de registro
                .post(body)
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    val result = response.isSuccessful
                    withContext(Dispatchers.Main) {
                        if (result) {
                            Toast.makeText(
                                this@RegisterActivity,
                                "¡Registro Exitoso!",
                                Toast.LENGTH_LONG
                            ).show()
                            finish() // Cerrar actividad
                        } else {
                            Toast.makeText(
                                this@RegisterActivity,
                                "Error: ${response.code}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegisterActivity, "Error de conexión", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun cargarAreas() {
        lifecycleScope.launch(Dispatchers.IO) {
            val client = OkHttpClient()
            val url = "http://192.168.1.28:8000/api/areas/"
            val request = Request.Builder().url(url).get().build()
            try {
                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string() ?: ""
                    val jsonRoot = JSONObject(responseBody)
                    val jsonArray = jsonRoot.getJSONArray("areas")

                    // Cambiamos a lista de objetos Area
                    val areaList = mutableListOf<Area>()

                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        // Extraemos id y nombre
                        areaList.add(
                            Area(
                                item.getInt("id"),
                                item.getString("nombre")
                            )
                        )
                    }

                    withContext(Dispatchers.Main) {
                        // El ArrayAdapter usará el método toString() de la clase Area
                        val adapter = ArrayAdapter(
                            this@RegisterActivity,
                            android.R.layout.simple_spinner_item,
                            areaList
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        sp.adapter = adapter
                    }
                }
            } catch (e: Exception) {
                Log.e("rega", "Error cargando áreas: ${e.message}")
            }
        }
    }


    data class Area(val id: Int, val nombre: String) {
        // Sobrescribimos toString para que el Spinner muestre solo el nombre
        override fun toString(): String = nombre
    }


}
