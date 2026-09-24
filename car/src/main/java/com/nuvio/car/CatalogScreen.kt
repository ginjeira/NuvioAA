package com.nuvio.car

import android.util.Log
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.lifecycle.lifecycleScope
import com.nuvio.app.features.addons.AddonRepository
import com.nuvio.app.features.home.HomeRepository
import com.nuvio.app.features.home.MetaPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CatalogScreen(carContext: CarContext) : Screen(carContext) {

    init {
        Log.i("NuvioCar", "CatalogScreen init: initializing AddonRepository and HomeRepository")
        AddonRepository.initialize()
        val addons = AddonRepository.uiState.value.addons
        HomeRepository.refresh(addons)

        lifecycleScope.launch {
            HomeRepository.uiState.collectLatest { state ->
                Log.i("NuvioCar", "CatalogScreen: HomeRepository uiState updated. loading=${state.isLoading}, sections=${state.sections.size}")
                invalidate()
            }
        }
    }

    override fun onGetTemplate(): Template {
        val homeState = HomeRepository.uiState.value

        if (homeState.isLoading && homeState.sections.isEmpty()) {
            return ListTemplate.Builder()
                .setTitle("Catálogo")
                .setLoading(true)
                .setHeaderAction(Action.BACK)
                .build()
        }

        val allItems = mutableListOf<MetaPreview>()
        homeState.sections.forEach { section ->
            allItems.addAll(section.items)
        }

        val distinctItems = allItems.distinctBy { it.id }

        if (distinctItems.isEmpty()) {
            return MessageTemplate.Builder("Nenhum item encontrado no catálogo. Verifique seus addons no aplicativo.")
                .setTitle("Catálogo")
                .setHeaderAction(Action.BACK)
                .build()
        }

        val listBuilder = ItemList.Builder()
        distinctItems.take(12).forEach { item ->
            val rowBuilder = Row.Builder()
                .setTitle(item.name)
            
            item.description?.takeIf { it.isNotBlank() }?.let { desc ->
                rowBuilder.addText(desc.take(80))
            } ?: item.releaseInfo?.takeIf { it.isNotBlank() }?.let { info ->
                rowBuilder.addText(info)
            }

            rowBuilder.setOnClickListener {
                Log.i("NuvioCar", "CatalogScreen: selected item ${item.name} (id=${item.id}, type=${item.type})")
                screenManager.push(
                    CarStreamsScreen(
                        carContext = carContext,
                        type = item.type,
                        videoId = item.id,
                        parentMetaId = item.id,
                        title = item.name
                    )
                )
            }

            listBuilder.addItem(rowBuilder.build())
        }

        return ListTemplate.Builder()
            .setTitle("Catálogo Nuvio")
            .setSingleList(listBuilder.build())
            .setHeaderAction(Action.BACK)
            .build()
    }
}
