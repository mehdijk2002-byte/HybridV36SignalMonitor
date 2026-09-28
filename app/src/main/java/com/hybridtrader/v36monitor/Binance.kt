package com.hybridtrader.v36monitor
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
object Binance {
 private val BASES=listOf("https://data-api.binance.vision","https://api.binance.com","https://api1.binance.com","https://api2.binance.com","https://api3.binance.com")
 private fun get(path:String):String { var last:Exception?=null; for(base in BASES){var conn:HttpURLConnection?=null;try{conn=URL(base+path).openConnection() as HttpURLConnection;conn.connectTimeout=8000;conn.readTimeout=10000;conn.requestMethod="GET";conn.setRequestProperty("Accept","application/json");val code=conn.responseCode;if(code!=200)throw IOException("HTTP $code");return conn.inputStream.bufferedReader().use{it.readText()}}catch(e:Exception){last=e}finally{conn?.disconnect()}};throw last?:IOException("No Binance endpoint reachable") }
 fun serverTime():Long=try{JSONObject(get("/api/v3/time")).getLong("serverTime")}catch(e:Exception){System.currentTimeMillis()}
 fun klines(startTime:Long?,endTime:Long?,limit:Int,now:Long):List<Candle>{val sb=StringBuilder("/api/v3/klines?symbol=ETHUSDT&interval=4h&limit=$limit");if(startTime!=null)sb.append("&startTime=").append(startTime);if(endTime!=null)sb.append("&endTime=").append(endTime);val arr=JSONArray(get(sb.toString()));val out=ArrayList<Candle>(arr.length());for(i in 0 until arr.length()){try{val k=arr.getJSONArray(i);val ot=k.getLong(0);val o=k.getString(1).toDouble();val h=k.getString(2).toDouble();val l=k.getString(3).toDouble();val c=k.getString(4).toDouble();val v=k.getString(5).toDouble();val ct=k.getLong(6);val finite=listOf(o,h,l,c,v).all{!it.isNaN()&&!it.isInfinite()};if(finite&&o>0&&h>0&&l>0&&c>0&&h>=l&&h>=maxOf(o,c)&&l<=minOf(o,c)&&ct>ot)out.add(Candle(ot,o,h,l,c,v,ct,ct<now))}catch(e:Exception){}};return out}
}