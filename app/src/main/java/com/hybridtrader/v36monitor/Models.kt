package com.hybridtrader.v36monitor

data class Candle(val openTime: Long,val open: Double,val high: Double,val low: Double,val close: Double,val volume: Double,val closeTime: Long,val closed: Boolean)

data class TradeSignal(
    val id: String,val candleTime: Long,val closeTime: Long,val type: String,val entry: Double,val regime: String,val classification: String,val stop: Double,val target: Double,val qty: Double,val notional: Double,var status: String,var result: String?,var pnl: Double,var rMultiple: Double,var exitTime: Long,var exitPrice: Double,val notificationSent: Boolean,val createdAt: Long
)