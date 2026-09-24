package com.nuvio.car

import android.util.Log
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import com.nuvio.app.features.home.MetaPreview

class CatalogSectionScreen(
    carContext: CarContext,
    private val sectionTitle: String,
    private val items: List<MetaPreview>
) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        if (items.isEmpty()) {
            return MessageTemplate.Builder("Nenhum item nesta seção.")
                .setTitle(sectionTitle)
                .setHeaderAction(Action.BACK)
                .build()
        }

        val listBuilder = ItemList.Builder()
        items.take(20).forEach { item ->
            val rowBuilder = Row.Builder()
                .setTitle(item.name)
            
            item.description?.takeIf { it.isNotBlank() }?.let { desc ->
                rowBuilder.addText(desc.take(80))
            } ?: item.releaseInfo?.takeIf { it.isNotBlank() }?.let { info ->
                rowBuilder.addText(info)
            }

            rowBuilder.setOnClickListener {
                Log.i("NuvioCar", "CatalogSectionScreen: selected item ${item.name} (id=${item.id})")
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
            .setTitle(sectionTitle)
            .setSingleList(listBuilder.build())
            .setHeaderAction(Action.BACK)
            .build()
    }
}
