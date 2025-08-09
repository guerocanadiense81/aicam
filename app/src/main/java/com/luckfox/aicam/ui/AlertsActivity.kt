package com.luckfox.aicam.ui
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.luckfox.aicam.R
import com.luckfox.aicam.api.Api
import com.luckfox.aicam.model.Event
import kotlinx.coroutines.*
import java.net.URL
import android.graphics.BitmapFactory

class AlertsActivity: AppCompatActivity() {
  private val scope = CoroutineScope(Dispatchers.Main)
  override fun onCreate(b: Bundle?) {
    super.onCreate(b); setContentView(R.layout.activity_alerts)
    val rv = findViewById<RecyclerView>(R.id.rv_alerts)
    rv.layoutManager = LinearLayoutManager(this)
    val adapter = AAdapter(mutableListOf())
    rv.adapter = adapter
    val prefs = getSharedPreferences("cfg", MODE_PRIVATE)
    val apiBase = prefs.getString("api","http://<CAM_IP>:8080/")!!
    val lastTs = prefs.getLong("lastTs",0L)
    scope.launch {
      try { val api = Api.client(apiBase); val res = api.alerts(lastTs)
        if (res.events.isNotEmpty()) {
          prefs.edit().putLong("lastTs", res.events.maxOf{it.ts}).apply()
          adapter.setData(res.events, apiBase)
        }
      } catch (_: Exception) {}
    }
  }
  class AAdapter(private val items: MutableList<Event>): RecyclerView.Adapter<VH>(){
    private var base=""
    fun setData(list: List<Event>, baseUrl:String){ items.clear(); items.addAll(list.reversed()); base=baseUrl; notifyDataSetChanged() }
    override fun onCreateViewHolder(p: android.view.ViewGroup, v: Int): VH {
      val view = android.view.LayoutInflater.from(p.context).inflate(R.layout.item_alert, p, false)
      return VH(view)
    }
    override fun getItemCount()=items.size
    override fun onBindViewHolder(h: VH, i: Int){
      val e=items[i]
      h.time.text = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(java.util.Date(e.ts*1000))
      h.itemView.setOnClickListener{
        val ctx = h.itemView.context
        ctx.startActivity(Intent(ctx, AlertDetailActivity::class.java).putExtra("clip", base+e.clip))
      }
      try { Thread{
        val bmp = BitmapFactory.decodeStream(URL(base+e.snapshot).openStream()); h.img.post{ h.img.setImageBitmap(bmp) }
      }.start() } catch (_:Exception){}
    }
  }
  class VH(v: android.view.View): RecyclerView.ViewHolder(v){
    val img: ImageView = v.findViewById(R.id.img)
    val time: TextView = v.findViewById(R.id.time)
  }
}
