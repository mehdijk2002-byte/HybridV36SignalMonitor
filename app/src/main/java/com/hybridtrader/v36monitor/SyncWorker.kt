package com.hybridtrader.v36monitor

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class SyncWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    override fun doWork(): Result {
        return try {
            val db = Db.get(applicationContext)
            val expected = (System.currentTimeMillis() / Strategy.INTERVAL) * Strategy.INTERVAL - Strategy.INTERVAL
            SyncManager.sync(applicationContext, network = !db.hasClosedCandle(expected))
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}