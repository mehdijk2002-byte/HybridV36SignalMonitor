package com.hybridtrader.v36monitor
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
object Notifier {
 const val CHANNEL="v36_signals";private const val STALE_MS=8L*3600L*1000L
 fun ensureChannel(ctx:Context){if(Build.VERSION.SDK_INT>=26){val nm=ctx.getSystemService(NotificationManager::class.java);if(nm.getNotificationChannel(CHANNEL)==null){val ch=NotificationChannel(CHANNEL,"Trading signals",NotificationManager.IMPORTANCE_HIGH);ch.description="New confirmed LONG / SHORT signals on ETHUSDT 4H";nm.createNotificationChannel(ch)}}}
 fun dispatch(ctx:Context,db:Db){val pending=db.unsent();if(pending.isEmpty())return;ensureChannel(ctx);val now=System.currentTimeMillis();for(s in pending){if(now-s.closeTime>STALE_MS){db.markSent(s.id);continue};if(!canNotify(ctx))continue;db.markSent(s.id);post(ctx,s)}}
 private fun canNotify(ctx:Context):Boolean{if(Build.VERSION.SDK_INT>=33&&ctx.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return false;return NotificationManagerCompat.from(ctx).areNotificationsEnabled()}
 private fun px(v:Double)=String.format(Locale.US,"%,.2f",v)
 private fun post(ctx:Context,s:TradeSignal){val utc=SimpleDateFormat("yyyy-MM-dd HH:mm 'UTC'",Locale.US);utc.timeZone=TimeZone.getTimeZone("UTC");val closeMs=s.closeTime+1;val target=if(s.target.isNaN())"trailing" else px(s.target);val line="Entry ${px(s.entry)} · Stop ${px(s.stop)} · Target $target";val big=line+"\nRegime: ${s.regime} (${s.classification})\nCandle close: ${utc.format(Date(closeMs))}";val open=Intent(ctx,MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP);val pi=PendingIntent.getActivity(ctx,0,open,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT);val n=NotificationCompat.Builder(ctx,CHANNEL).setSmallIcon(R.drawable.ic_stat).setContentTitle("ETHUSDT 4H — ${s.type}").setContentText(line).setStyle(NotificationCompat.BigTextStyle().bigText(big)).setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true).setShowWhen(true).setWhen(closeMs).setContentIntent(pi).build();try{NotificationManagerCompat.from(ctx).notify((s.candleTime/1000L).toInt(),n)}catch(e:SecurityException){}}
}