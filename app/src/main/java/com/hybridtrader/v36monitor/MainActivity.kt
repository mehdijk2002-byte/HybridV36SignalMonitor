package com.hybridtrader.v36monitor
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
private val BG=0xFF0B0E11.toInt();private val WHITE=0xFFEAECEF.toInt();private val GRAY=0xFF848E9C.toInt();private val GREEN=0xFF0ECB81.toInt();private val RED=0xFFF6465D.toInt();private val YELLOW=0xFFF0B90B.toInt()
class MainActivity:Activity(){
 private lateinit var priceTv:TextView;private lateinit var regimeTv:TextView;private lateinit var signalTv:TextView;private lateinit var detailTv:TextView;private lateinit var statusTv:TextView;private lateinit var chart:CandleChartView
 private val handler=Handler(Looper.getMainLooper());private val exec=Executors.newSingleThreadExecutor();private val busy=AtomicBoolean(false)
 private val tick=object:Runnable{override fun run(){refresh();handler.postDelayed(this,30000L)}}
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);val d=resources.displayMetrics.density;fun dp(v:Int)=(v*d).toInt()
  val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(BG);setPadding(dp(14),dp(10),dp(14),dp(10))}
  val header=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL};header.addView(tv("ETHUSDT",20f,WHITE,true));header.addView(tv("  4H",14f,GRAY,false));priceTv=tv("—",26f,WHITE,true).apply{gravity=Gravity.END};header.addView(priceTv,LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f));root.addView(header)
  regimeTv=tv("—",18f,GRAY,true);signalTv=tv("WAIT",18f,GRAY,true);val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL};row.addView(block("REGIME",regimeTv),LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f));row.addView(block("SIGNAL",signalTv),LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f));root.addView(row)
  detailTv=tv("",12f,GRAY,false);root.addView(detailTv);chart=CandleChartView(this);root.addView(chart,LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,1f));statusTv=tv("Loading…",11f,GRAY,false);root.addView(statusTv);setContentView(root)
  ViewCompat.setOnApplyWindowInsetsListener(root){v,insets->val bars=insets.getInsets(WindowInsetsCompat.Type.systemBars());v.setPadding(dp(14)+bars.left,dp(10)+bars.top,dp(14)+bars.right,dp(10)+bars.bottom);WindowInsetsCompat.CONSUMED};WindowInsetsControllerCompat(window,root).isAppearanceLightStatusBars=false
  if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),100)
 }
 override fun onResume(){super.onResume();handler.post(tick)};override fun onPause(){handler.removeCallbacks(tick);super.onPause()};override fun onDestroy(){exec.shutdownNow();super.onDestroy()}
 private fun tv(text:String,sp:Float,color:Int,bold:Boolean)=TextView(this).apply{this.text=text;textSize=sp;setTextColor(color);if(bold)setTypeface(typeface,Typeface.BOLD)}
 private fun block(label:String,value:TextView)=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;addView(tv(label,10f,GRAY,false));addView(value)}
 private fun refresh(){if(!busy.compareAndSet(false,true))return;try{exec.execute{try{val app=applicationContext;if(SyncManager.latest==null){val cached=SyncManager.sync(app,false);runOnUiThread{if(!isDestroyed)render(cached)}};val a=SyncManager.sync(app,true);runOnUiThread{if(!isDestroyed)render(a)}}catch(t:Throwable){runOnUiThread{if(!isDestroyed)statusTv.text="Error: ${t.message}"}}finally{busy.set(false)}}}catch(e:Exception){busy.set(false)}}
 private fun fmt(v:Double)=String.format(Locale.US,"%,.2f",v)
 private fun render(a:Strategy.Analysis){if(a.candles.isEmpty()){statusTv.text=if(SyncManager.lastError!=null)"Waiting for connection…" else "Loading…";return};priceTv.text=fmt(a.candles.last().close);regimeTv.text=a.regime;regimeTv.setTextColor(when(a.regime){"TREND BULL"->GREEN;"TREND BEAR"->RED;"RANGE"->YELLOW;else->GRAY});val os=a.openSignal;if(os!=null){signalTv.text=os.type;signalTv.setTextColor(if(os.type=="LONG")GREEN else RED);val target=if(os.target.isNaN())"trailing" else fmt(os.target);detailTv.text="Entry ${fmt(os.entry)}   Stop ${fmt(os.stop)}   Target $target"}else{signalTv.text="WAIT";signalTv.setTextColor(GRAY);detailTv.text=""};val err=SyncManager.lastError;statusTv.text=when{a.closedCount<260->"Building history…";err!=null->"Offline — showing cached data";else->"Updated "+SimpleDateFormat("HH:mm:ss",Locale.US).format(Date(SyncManager.lastSyncMs))};chart.setData(a)}
}