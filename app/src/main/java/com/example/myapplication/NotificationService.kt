package com.example.myapplication

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL

class NotificationService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private val NOTIFICATION_ID = 1

    override fun onCreate() {
        super.onCreate()
        crearCanalSilencioso()
        val notification = NotificationCompat.Builder(this, "canal_servicio_segundo_plano")
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("Esperando notificaciones...")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(NOTIFICATION_ID, notification)
        crearCanalNotificaciones()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val userId = LoginActivity.SessionManager.getUserId(this)
        if (userId != -1) {
            iniciarStream(userId)
        }
        return START_STICKY
    }

    private fun iniciarStream(userId: Int) {
        serviceScope.launch {
            try {
                val url =
                    URL("http://192.168.1.28:8000/api/notifications/stream/?usuario_id=$userId")
                val conn = url.openConnection()
                conn.setRequestProperty("Accept", "text/event-stream")

                conn.getInputStream().bufferedReader().forEachLine { line ->
                    if (line.startsWith("data: ")) {
                        val jsonContent = line.removePrefix("data: ").trim()
                        mostrarNotificacionNativa(jsonContent)
                    }
                }
            } catch (e: Exception) {
                Log.e("NotificationService", "Error en stream: ${e.message}")
                delay(5000)
                iniciarStream(userId)
            }
        }
    }

    private fun mostrarNotificacionNativa(jsonStr: String) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = System.currentTimeMillis().toInt()
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val jsonResponse = JSONObject(jsonStr)
        Log.d("NotificationService", "Notificación recibida: $jsonResponse")
        val data = jsonResponse.getJSONObject("data")
        val titulo = data.getString("cuerpo")
        val cuerpo = data.getString("descripcion")
        val notification = NotificationCompat.Builder(this, "canal_alertas")
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(Notification.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(notificationId, notification)
    }

    private fun crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "canal_alertas",
                "Alertas del Asistente",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    private fun crearCanalSilencioso() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "canal_servicio_segundo_plano",
                "Servicio en segundo plano",
                NotificationManager.IMPORTANCE_MIN
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}