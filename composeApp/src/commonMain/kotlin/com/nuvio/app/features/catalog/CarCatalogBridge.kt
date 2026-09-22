package com.nuvio.car

import com.nuvio.app.features.catalog.models.Series
import com.nuvio.app.features.catalog.models.Episode

object CarCatalogBridge {

    fun getSeries(): List<Series> {
        return listOf(
            Series(
                id = "1",
                title = "Exemplo",
                artworkUrl = "https://via.placeholder.com/300",
                episodes = listOf(
                    Episode(
                        id = "1-1",
                        title = "Episódio 1",
                        artworkUrl = "https://via.placeholder.com/300",
                        streamUrl = "https://example.com/stream1"
                    )
                )
            )
        )
    }
}
