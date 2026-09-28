package com.hybridtrader.v36monitor
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
class CandleChartView(ctx:Context):View(ctx){
 private var data:Strategy.Analysis?=null
 private val p=Paint(Paint.ANTI_ALIAS_FLAG)
 fun setData(a:Strategy.Analysis){data=a;invalidate()}
 override fun onDraw(c:Canvas){super.onDraw(c);val a=data?:return;val candles=a.candles.takeLast(80);if(candles.isEmpty())return;val hi=candles.maxOf{it.high};val lo=candles.minOf{it.low};val span=(hi-lo).coerceAtLeast(0.01);val w=width.toFloat()/candles.size
  candles.forEachIndexed{i,k->val x=(i+.5f)*w;val yh=((hi-k.high)/span*height).toFloat();val yl=((hi-k.low)/span*height).toFloat();val yo=((hi-k.open)/span*height).toFloat();val yc=((hi-k.close)/span*height).toFloat();p.color=if(k.close>=k.open)0xFF0ECB81.toInt() else 0xFFF6465D.toInt();p.strokeWidth=2f;c.drawLine(x,yh,x,yl,p);c.drawRect(x-w*.3f,minOf(yo,yc),x+w*.3f,maxOf(yo,yc).coerceAtLeast(minOf(yo,yc)+2f),p)}
 }
}