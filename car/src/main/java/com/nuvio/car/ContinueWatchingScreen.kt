package com.nuvio.car

import android.util.Log
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.*
import com.nuvio.app.features.watchprogress.WatchProgressEntry

class ContinueWatchingScreen(
    carContext: CarContext,
    private val entries: List<WatchProgressEntry>
) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val listBuilder = ItemList.Builder()
        entries.forEach { entry ->
            val title = entry.title
            val progressPercent = if (entry.durationMs > 0) {
                ((entry.lastPositionMs * 100) / entry.durationMs).coerceIn(0, 100)
            } else 0

            val row = Row.Builder()
                .setTitle(title)
                .addText("Progresso: $progressPercent%")
                .setOnClickListener {
                    Log.i("NuvioCar", "ContinueWatchingScreen: selected resume item ${entry.title}")
                    screenManager.push(
                        CarStreamsScreen(
                            carContext = carContext,
                            type = entry.contentType,
                            videoId = entry.videoId,
                            parentMetaId = entry.parentMetaId,
                            title = title
                        )
                    )
                }
                .build()
            listBuilder.addItem(row)
        }

        return ListTemplate.Builder()
            .setTitle("Continuar a Assistir")
            .setSingleList(listBuilder.build())
            .setHeaderAction(Action.BACK)
            .build()
    }
}
