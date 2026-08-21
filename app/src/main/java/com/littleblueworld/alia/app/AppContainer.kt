package com.littleblueworld.alia.app

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.littleblueworld.alia.content.BirthdayContent
import com.littleblueworld.alia.state.AppStateRepository
import com.littleblueworld.alia.state.DataStoreAppStateRepository
import com.littleblueworld.alia.wish.SupabaseWishApi
import com.littleblueworld.alia.wish.WishDeliveryOrchestrator
import com.littleblueworld.alia.wish.WishRepository
import com.littleblueworld.alia.wish.WorkManagerWishRetryScheduler

private val Context.appStateDataStore by preferencesDataStore(
    name = "little_blue_world_state",
)

class AppContainer(context: Context) {
    private val applicationContext = context.applicationContext

    val birthdayContent: BirthdayContent = BirthdayContent()

    val appStateRepository: AppStateRepository = DataStoreAppStateRepository(
        dataStore = applicationContext.appStateDataStore,
    )

    private val appConfig = AppConfig.fromBuildConfig()

    val wishRepository = WishRepository(
        stateRepository = appStateRepository,
        wishApi = SupabaseWishApi(appConfig),
    )

    private val wishRetryScheduler = WorkManagerWishRetryScheduler(applicationContext)

    val wishDeliveryOrchestrator = WishDeliveryOrchestrator(
        wishRepository = wishRepository,
        retryScheduler = wishRetryScheduler,
    )
}
