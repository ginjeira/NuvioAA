package com.nuvio.car

import com.nuvio.androidApp.catalog.CatalogRepository
import com.nuvio.androidApp.catalog.models.Series

object CarCatalogBridge {

    private val repo by lazy { CatalogRepository() }

    suspend fun getSeries(): List<Series> {
        return repo.getAllSeries()
    }
}
