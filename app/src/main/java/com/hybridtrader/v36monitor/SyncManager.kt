package com.hybridtrader.v36monitor
import android.content.Context
import java.io.IOException
object SyncManager {
 private val lock=Any()
 @Volatile var latest:Strategy.Analysis?=null
 @Volatile var lastError:String?=null
 @Volatile var lastSyncMs:Long=0L
 fun sync(ctx:Context,network:Boolean=true):Strategy.Analysis=synchronized(lock){val app=ctx.applicationContext;val db=Db.get(app);if(network){try{fetchAndStore(db);lastError=null;lastSyncMs=System.currentTimeMillis()}catch(e:Exception){lastError=e.message?:e.javaClass.simpleName}};val a=Strategy.analyze(db.loadCandles());db.saveSignals(a.signals,System.currentTimeMillis());Notifier.dispatch(app,db);latest=a;a}
 private fun fetchAndStore(db:Db){val now=Binance.serverTime();val lastOpen=db.latestOpenTime();if(lastOpen==null){var end:Long?=null;for(page in 0 until 3){val batch=Binance.klines(null,end,1000,now);if(batch.isEmpty())break;db.upsertCandles(batch);end=batch.first().openTime-1};if(db.latestOpenTime()==null)throw IOException("No market data received")}else{var start:Long=lastOpen;for(page in 0 until 30){val batch=Binance.klines(start,null,1000,now);if(batch.isEmpty())break;db.upsertCandles(batch);if(batch.size<1000)break;start=batch.last().openTime+1}};fillGaps(db,now)}
 private fun fillGaps(db:Db,now:Long){for(attempt in 0 until 5){val t=db.openTimes();var gapFrom=-1L;var gapTo=-1L;for(i in 1 until t.size){if(t[i]-t[i-1]>Strategy.INTERVAL){gapFrom=t[i-1];gapTo=t[i];break}};if(gapFrom<0)return;val batch=Binance.klines(gapFrom,gapTo,1000,now);if(batch.isEmpty())return;db.upsertCandles(batch)}}
}