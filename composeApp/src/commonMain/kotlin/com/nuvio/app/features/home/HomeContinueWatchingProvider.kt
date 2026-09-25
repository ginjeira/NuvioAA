package com.nuvio.app.features.home

import com.nuvio.app.features.profiles.ProfileRepository
import com.nuvio.app.features.tracking.TrackingSettingsRepository
import com.nuvio.app.features.watchprogress.CachedInProgressItem
import com.nuvio.app.features.watchprogress.ContinueWatchingItem
import com.nuvio.app.features.watchprogress.ContinueWatchingPreferencesRepository
import com.nuvio.app.features.watchprogress.ContinueWatchingEnrichmentCache
import com.nuvio.app.features.watchprogress.WatchProgressRepository
import com.nuvio.app.features.watchprogress.buildContinueWatchingEpisodeSubtitle
import com.nuvio.app.features.watchprogress.toContinueWatchingItem
import com.nuvio.app.features.details.MetaDetailsRepository

/**
 * Platform-agnostic provider for the Continue Watching projection.
 *
 * Reuses Nuvio Mobile ContinueWatchingEnrichmentCache and MetaDetailsRepository to guarantee rich titles
 * (e.g. "The Daily Show") and metadata identical to the mobile app.
 */
object HomeContinueWatchingProvider {

    fun load(limit: Int = 10): List<ContinueWatchingItem> {
        ContinueWatchingPreferencesRepository.ensureLoaded()
        TrackingSettingsRepository.ensureLoaded()
        WatchProgressRepository.ensureLoaded()

        val progressState = WatchProgressRepository.uiState.value
        val profileId = ProfileRepository.activeProfileId

        val (_, inProgressSnapshots) = ContinueWatchingEnrichmentCache.getSnapshots(
            profileId = profileId,
            source = progressState.source,
        )

        if (inProgressSnapshots.isNotEmpty()) {
            return inProgressSnapshots
                .sortedByDescending { it.lastWatched }
                .take(limit)
                .map { it.toContinueWatchingItem() }
        }

        val validEntries = progressState.entries
            .filter { entry -> !entry.isEffectivelyCompleted }
            .sortedByDescending { it.lastUpdatedEpochMs }
            .take(limit)

        return validEntries.map { entry ->
            val item = entry.toContinueWatchingItem()
            val rawTitle = item.title.trim()
            val displayTitle = if (rawTitle.isBlank() || rawTitle.startsWith("tt", ignoreCase = true) || rawTitle.contains(":")) {
                val cachedMeta = MetaDetailsRepository.peek(entry.contentType, entry.parentMetaId)
                cachedMeta?.name?.takeIf { it.isNotBlank() } ?: "Série (${entry.parentMetaId})"
            } else {
                rawTitle
            }
            item.copy(title = displayTitle)
        }
    }
}

private fun CachedInProgressItem.toContinueWatchingItem(): ContinueWatchingItem {
    val normalizedProgressFraction = progressPercent
        ?.let { (it / 100f).coerceIn(0f, 1f) }
        ?: if (duration > 0L) {
            (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
    return ContinueWatchingItem(
        parentMetaId = contentId,
        parentMetaType = contentType,
        videoId = videoId,
        title = name,
        subtitle = buildContinueWatchingEpisodeSubtitle(
            seasonNumber = season,
            episodeNumber = episode,
            episodeTitle = episodeTitle,
        ),
        imageUrl = episodeThumbnail.takeIf { !it.isNullOrBlank() } ?: backdrop.takeIf { !it.isNullOrBlank() } ?: poster,
        logo = logo,
        poster = poster,
        background = backdrop,
        seasonNumber = season,
        episodeNumber = episode,
        episodeTitle = episodeTitle,
        episodeThumbnail = episodeThumbnail,
        pauseDescription = pauseDescription,
        resumePositionMs = position,
        resumeProgressFraction = normalizedProgressFraction,
        durationMs = duration,
        progressFraction = normalizedProgressFraction,
    )
}
