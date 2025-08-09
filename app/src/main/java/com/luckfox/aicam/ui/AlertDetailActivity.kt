package com.luckfox.aicam.ui
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.luckfox.aicam.R

class AlertDetailActivity: AppCompatActivity() {
  override fun onCreate(b: Bundle?) {
    super.onCreate(b); setContentView(R.layout.activity_alert_detail)
    val vv = findViewById<VideoView>(R.id.video)
    val clip = intent.getStringExtra("clip") ?: return
    vv.setVideoURI(Uri.parse(clip))
    vv.setOnPreparedListener { it.isLooping=false; vv.start() }
  }
}
