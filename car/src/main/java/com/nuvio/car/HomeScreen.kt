package com.nuvio.car

import android.util.Log
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.lifecycle.lifecycleScope
import com.nuvio.app.features.watchprogress.WatchProgressRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeScreen(carContext: CarContext) : Screen(carContext) {

    init {
        WatchProgressRepository.ensureLoaded()
        lifecycleScope.launch {
            WatchProgressRepository.uiState.collectLatest {
                invalidate()
            }
        }
    }

    override fun onGetTemplate(): Template {
        Log.i("NuvioCar", "HomeScreen: onGetTemplate called")
        val progressState = WatchProgressRepository.uiState.value
        val recentProgress = progressState.entries.sortedByDescending { it.lastUpdatedEpochMs }.take(5)

        val itemList = ItemList.Builder()
            .addItem(
                Row.Builder()
                    .setTitle("Catálogo de Addons")
                    .addText("Navegue por filmes, séries e coleções")
                    .setBrowsable(true)
                    .setOnClickListener {
                        Log.i("NuvioCar", "HomeScreen: Catalog categories clicked")
                        screenManager.push(CatalogCategorySelectionScreen(carContext))
                    }
                    .build()
            )

        if (recentProgress.isNotEmpty()) {
            itemList.addItem(
                Row.Builder()
                    .setTitle("Continuar a Assistir (${recentProgress.size})")
                    .addText("Retome de onde parou")
                    .setBrowsable(true)
                    .setOnClickListener {
                        Log.i("NuvioCar", "HomeScreen: Continue watching clicked")
                        screenManager.push(ContinueWatchingScreen(carContext, recentProgress))
                    }
                    .build()
            )
        }
        
        return ListTemplate.Builder()
            .setTitle("Nuvio Auto")
            .setSingleList(itemList.build())
            .build()
    }
}
