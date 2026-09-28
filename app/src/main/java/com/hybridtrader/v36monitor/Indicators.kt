package com.hybridtrader.v36monitor

import kotlin.math.abs
import kotlin.math.max

object Ind {
    class Dmi(val plus: DoubleArray, val minus: DoubleArray, val adx: DoubleArray)
    fun sma(src: DoubleArray, len: Int): DoubleArray {
        val out = DoubleArray(src.size) { Double.NaN }
        for (i in len - 1 until src.size) {
            var s = 0.0; var ok = true
            for (k in i - len + 1..i) { val v=src[k]; if(v.isNaN()){ok=false;break}; s+=v }
            if(ok) out[i]=s/len
        }
        return out
    }
    fun ema(src: DoubleArray,len:Int)=recursive(src,len,2.0/(len+1))
    fun rma(src: DoubleArray,len:Int)=recursive(src,len,1.0/len)
    private fun recursive(src:DoubleArray,len:Int,alpha:Double):DoubleArray {
        val out=DoubleArray(src.size){Double.NaN}; var f=-1
        for(i in src.indices) if(!src[i].isNaN()){f=i;break}
        if(f<0)return out; val seed=f+len-1; if(seed>=src.size)return out
        var s=0.0; for(k in f..seed)s+=src[k]; var prev=s/len; out[seed]=prev
        for(i in seed+1 until src.size){prev=alpha*src[i]+(1.0-alpha)*prev;out[i]=prev}; return out
    }
    fun rsi(close:DoubleArray,len:Int):DoubleArray {
        val n=close.size; val up=DoubleArray(n){Double.NaN}; val dn=DoubleArray(n){Double.NaN}
        for(i in 1 until n){val d=close[i]-close[i-1];up[i]=maxOf(d,0.0);dn[i]=maxOf(-d,0.0)}
        val au=rma(up,len);val ad=rma(dn,len)
        return DoubleArray(n){i->val u=au[i];val d=ad[i];if(u.isNaN()||d.isNaN())Double.NaN else if(d==0.0)100.0 else 100.0-100.0/(1.0+u/d)}
    }
    fun trueRange(h:DoubleArray,l:DoubleArray,c:DoubleArray)=DoubleArray(h.size){i->if(i==0)h[0]-l[0] else max(h[i]-l[i],max(abs(h[i]-c[i-1]),abs(l[i]-c[i-1])))}
    fun atr(h:DoubleArray,l:DoubleArray,c:DoubleArray,len:Int)=rma(trueRange(h,l,c),len)
    fun dmi(h:DoubleArray,l:DoubleArray,c:DoubleArray,len:Int,adxLen:Int):Dmi {
        val n=h.size;val nan=Double.NaN;val pdm=DoubleArray(n){nan};val mdm=DoubleArray(n){nan};val tr=DoubleArray(n){nan}
        for(i in 1 until n){val up=h[i]-h[i-1];val dn=l[i-1]-l[i];pdm[i]=if(up>dn&&up>0)up else 0.0;mdm[i]=if(dn>up&&dn>0)dn else 0.0;tr[i]=max(h[i]-l[i],max(abs(h[i]-c[i-1]),abs(l[i]-c[i-1])))}
        val rt=rma(tr,len);val rp=rma(pdm,len);val rm=rma(mdm,len)
        val plus=DoubleArray(n){if(rt[it].isNaN()||rt[it]==0.0)nan else 100.0*rp[it]/rt[it]}
        val minus=DoubleArray(n){if(rt[it].isNaN()||rt[it]==0.0)nan else 100.0*rm[it]/rt[it]}
        val dx=DoubleArray(n){i->if(plus[i].isNaN()||minus[i].isNaN())nan else {val s=plus[i]+minus[i];abs(plus[i]-minus[i])/(if(s==0.0)1.0 else s)}}
        val adxRaw=rma(dx,adxLen);return Dmi(plus,minus,DoubleArray(n){100.0*adxRaw[it]})
    }
}