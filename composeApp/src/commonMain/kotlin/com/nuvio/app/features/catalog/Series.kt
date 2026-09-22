package com.nuvio.app.features.catalog.models

data class Series(
    val id: String,
    val title: String,
    val artworkUrl: String,
    val episodes: List<Episode>
)
