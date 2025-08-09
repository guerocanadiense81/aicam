package com.luckfox.aicam.ui
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.luckfox.aicam.R

class MainActivity: AppCompatActivity() {
  override fun onCreate(b: Bundle?) {
    super.onCreate(b)
    setContentView(R.layout.activity_main)
    findViewById<android.view.View>(R.id.btn_live).setOnClickListener {
      startActivity(Intent(this, LiveActivity::class.java))
    }
    findViewById<android.view.View>(R.id.btn_alerts).setOnClickListener {
      startActivity(Intent(this, AlertsActivity::class.java))
    }
    findViewById<android.view.View>(R.id.btn_settings).setOnClickListener {
      startActivity(Intent(this, SettingsActivity::class.java))
    }
  }
}
