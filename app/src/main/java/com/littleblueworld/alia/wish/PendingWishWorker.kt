package com.littleblueworld.alia.wish

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.littleblueworld.alia.app.AppContainer

class PendingWishWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        val repository = AppContainer(applicationContext).wishRepository
        return when (WishRetryPolicy.workerDecision(repository.retryPendingWish())) {
            WishWorkerDecision.SUCCESS -> Result.success()
            WishWorkerDecision.RETRY -> Result.retry()
            WishWorkerDecision.FAILURE -> Result.failure()
        }
    }
}
