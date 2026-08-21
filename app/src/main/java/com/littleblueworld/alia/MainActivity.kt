package com.littleblueworld.alia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import com.littleblueworld.alia.app.AppContainer
import com.littleblueworld.alia.app.AppCoordinator
import com.littleblueworld.alia.databinding.ActivityMainBinding

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var coordinator: AppCoordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_LittleBlueWorld)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val container = AppContainer(applicationContext)
        coordinator = AppCoordinator(
            context = this,
            screenHost = binding.screenHost,
            globalOverlayHost = binding.globalOverlayHost,
            stateRepository = container.appStateRepository,
            wishDeliveryOrchestrator = container.wishDeliveryOrchestrator,
            content = container.birthdayContent,
        )
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (!coordinator.onBackPressed()) {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            },
        )
        coordinator.start()
    }

    override fun onStart() {
        super.onStart()
        if (::coordinator.isInitialized) {
            coordinator.onForegrounded()
        }
    }

    override fun onStop() {
        if (::coordinator.isInitialized) {
            coordinator.onBackgrounded()
        }
        super.onStop()
    }

    override fun onDestroy() {
        if (::coordinator.isInitialized) {
            coordinator.destroy()
        }
        super.onDestroy()
    }
}
