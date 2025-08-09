package com.luckfox.aicam.ui
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.luckfox.aicam.R
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout

class LiveActivity: AppCompatActivity() {
  private lateinit var vlc: LibVLC
  private lateinit var mp: MediaPlayer
  private lateinit var layout: VLCVideoLayout
  override fun onCreate(b: Bundle?) {
    super.onCreate(b); setContentView(R.layout.activity_live)
    layout = findViewById(R.id.video_layout)
    val args = arrayListOf("--rtsp-tcp","--network-caching=300")
    vlc = LibVLC(this, args); mp = MediaPlayer(vlc); mp.attachViews(layout,null,false,false)
    val prefs = getSharedPreferences("cfg", MODE_PRIVATE)
    val rtsp = prefs.getString("rtsp","rtsp://<CAM_IP>/live/main")!!
    val m = Media(vlc, Uri.parse(rtsp)); mp.media = m; m.release(); mp.play()
  }
  override fun onDestroy() { mp.stop(); mp.detachViews(); vlc.release(); super.onDestroy() }
}
