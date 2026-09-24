package com.nuvio.car

import android.util.Log
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.lifecycle.lifecycleScope
import com.nuvio.app.features.addons.AddonRepository
import com.nuvio.app.features.home.HomeRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CatalogCategorySelectionScreen(carContext: CarContext) : Screen(carContext) {

    init {
        Log.i("NuvioCar", "CatalogCategorySelectionScreen init")
        AddonRepository.initialize()
        val addons = AddonRepository.uiState.value.addons
        HomeRepository.refresh(addons)

        lifecycleScope.launch {
            HomeRepository.uiState.collectLatest { state ->
                Log.i("NuvioCar", "CatalogCategorySelectionScreen: sections updated count=${state.sections.size}")
                invalidate()
            }
        }
    }

    override fun onGetTemplate(): Template {
        val homeState = HomeRepository.uiState.value

        if (homeState.isLoading && homeState.sections.isEmpty()) {
            return ListTemplate.Builder()
                .setTitle("Catálogos")
                .setLoading(true)
                .setHeaderAction(Action.BACK)
                .build()
        }

        val sections = homeState.sections
        if (sections.isEmpty()) {
            return MessageTemplate.Builder("Nenhum catálogo disponível. Verifique seus addons.")
                .setTitle("Catálogos")
                .setHeaderAction(Action.BACK)
                .build()
        }

        val listBuilder = ItemList.Builder()
        sections.forEach { section ->
            val row = Row.Builder()
                .setTitle(section.title)
                .addText("${section.items.size} itens disponíveis")
                .setBrowsable(true)
                .setOnClickListener {
                    Log.i("NuvioCar", "CatalogCategorySelectionScreen: selected section ${section.title}")
                    screenManager.push(CatalogSectionScreen(carContext, section.title, section.items))
                }
                .build()
            listBuilder.addItem(row)
        }

        return ListTemplate.Builder()
            .setTitle("Categorias de Catálogo")
            .setSingleList(listBuilder.build())
            .setHeaderAction(Action.BACK)
            .build()
    }
}
