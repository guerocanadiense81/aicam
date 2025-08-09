package com.luckfox.aicam.ui

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.luckfox.aicam.R
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.concurrent.TimeUnit

class FirstRunActivity: AppCompatActivity() {
  private val scope = CoroutineScope(Dispatchers.Main)
  private val preferredSubnet = "192.168.1.0/24" // your network

  override fun onCreate(b: Bundle?) {
    super.onCreate(b)
    setContentView(R.layout.activity_first_run)
    val tv = findViewById<TextView>(R.id.tv_status)
    val bar = findViewById<ProgressBar>(R.id.pb)
    scope.launch {
      tv.text = "Scanning your network..."
      val prefs = getSharedPreferences("cfg", MODE_PRIVATE)
      val local = localCidr()
      val order = listOf(preferredSubnet, local).distinct()
      var found: Pair<String,String>? = null
      withContext(Dispatchers.IO) {
        for (cidr in order) { found = scanForCam(cidr); if (found!=null) break }
      }
      if (found != null) {
        val (ip, rtsp) = found!!
        prefs.edit().putString("api", "http://$ip:8080/").putString("rtsp", rtsp).apply()
        tv.text = "Camera found at $ip"
        delay(400)
        startActivity(android.content.Intent(this@FirstRunActivity, LiveActivity::class.java))
        finish()
      } else {
        tv.text = "No camera auto-detected. Set IP in Settings."
        delay(1000)
        startActivity(android.content.Intent(this@FirstRunActivity, SettingsActivity::class.java))
        finish()
      }
    }
  }

  private fun localCidr(): String {
    val ifaces = NetworkInterface.getNetworkInterfaces()
    for (ni in ifaces) {
      val addrs = ni.inetAddresses
      while (addrs.hasMoreElements()) {
        val a = addrs.nextElement()
        if (a is Inet4Address && !a.isLoopbackAddress) {
          val parts = a.hostAddress.split('.')
          return "${parts[0]}.${parts[1]}.${parts[2]}.0/24"
        }
      }
    }
    return preferredSubnet
  }

  private fun scanForCam(cidr: String): Pair<String,String>? {
    val base = cidr.substringBeforeLast(".0/24")
    val client = OkHttpClient.Builder()
      .callTimeout(350, TimeUnit.MILLISECONDS)
      .connectTimeout(250, TimeUnit.MILLISECONDS)
      .readTimeout(250, TimeUnit.MILLISECONDS)
      .build()
    for (i in 1..254) {
      val ip = "$base.$i"
      try {
        val req = Request.Builder().url("http://$ip:8080/health").build()
        client.newCall(req).execute().use { resp ->
          if (resp.isSuccessful) {
            val rtsp = "rtsp://$ip/live/main"
            return ip to rtsp
          }
        }
      } catch (_: Exception) {}
    }
    return null
  }
}
