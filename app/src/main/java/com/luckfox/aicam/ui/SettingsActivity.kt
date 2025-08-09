package com.luckfox.aicam.ui
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.luckfox.aicam.R
import com.luckfox.aicam.api.Api
import com.luckfox.aicam.model.CamConfig
import kotlinx.coroutines.*

class SettingsActivity: AppCompatActivity() {
  private val scope = CoroutineScope(Dispatchers.Main)
  override fun onCreate(b: Bundle?) {
    super.onCreate(b); setContentView(R.layout.activity_settings)
    val etApi = findViewById<EditText>(R.id.et_api)
    val etRtsp = findViewById<EditText>(R.id.et_rtsp)
    val sbThr = findViewById<SeekBar>(R.id.sb_thr)
    val tvThr = findViewById<TextView>(R.id.tv_thr)
    val etFps = findViewById<EditText>(R.id.et_fps)
    val etRes = findViewById<EditText>(R.id.et_res)
    val etCd  = findViewById<EditText>(R.id.et_cooldown)
    val prefs = getSharedPreferences("cfg", MODE_PRIVATE)
    etApi.setText(prefs.getString("api","http://<CAM_IP>:8080/"))
    etRtsp.setText(prefs.getString("rtsp","rtsp://<CAM_IP>/live/main"))
    sbThr.progress = prefs.getInt("thr",45); tvThr.text = (sbThr.progress/100.0).toString()
    etFps.setText(prefs.getInt("fps",20).toString())
    etRes.setText(prefs.getString("res","1280x720"))
    etCd.setText(prefs.getInt("cd",15).toString())

    sbThr.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
      override fun onProgressChanged(s: SeekBar?, p: Int, f: Boolean) { tvThr.text = (p/100.0).toString() }
      override fun onStartTrackingTouch(s: SeekBar?) {}
      override fun onStopTrackingTouch(s: SeekBar?) {}
    })
    findViewById<Button>(R.id.btn_save).setOnClickListener {
      val api = etApi.text.toString(); val rtsp = etRtsp.text.toString()
      val thr = sbThr.progress/100.0; val fps = etFps.text.toString().toInt()
      val res = etRes.text.toString(); val cd = etCd.text.toString().toInt()
      prefs.edit().putString("api",api).putString("rtsp",rtsp)
        .putInt("thr", (thr*100).toInt()).putInt("fps",fps)
        .putString("res",res).putInt("cd",cd).apply()
      scope.launch {
        try {
          val apiSvc = Api.client(api)
          apiSvc.setConfig(CamConfig(rtsp,res,fps,thr,cd))
          Toast.makeText(this@SettingsActivity,"Saved",Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
          Toast.makeText(this@SettingsActivity,"Error: "+e.message,Toast.LENGTH_LONG).show()
        }
      }
    }
  }
}
