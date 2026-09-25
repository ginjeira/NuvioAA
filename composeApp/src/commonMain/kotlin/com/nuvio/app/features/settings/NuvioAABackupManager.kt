package com.nuvio.app.features.settings

import com.nuvio.app.features.addons.AddonStorage
import com.nuvio.app.features.debrid.DebridSettingsRepository
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class NuvioAABackupData(
    val version: Int = 1,
    val addonUrls: List<String> = emptyList(),
    val addonEnabledStates: Map<String, Boolean> = emptyMap(),
    val providerApiKeys: Map<String, String> = emptyMap(),
)

object NuvioAABackupManager {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    fun exportBackupJson(): String {
        val addonUrls = AddonStorage.loadInstalledAddonUrls(1)
        val addonStates = AddonStorage.loadAddonEnabledStates(1)
        DebridSettingsRepository.ensureLoaded()
        val debridSnapshot = DebridSettingsRepository.snapshot()

        val data = NuvioAABackupData(
            addonUrls = addonUrls,
            addonEnabledStates = addonStates,
            providerApiKeys = debridSnapshot.providerApiKeys,
        )

        return json.encodeToString(NuvioAABackupData.serializer(), data)
    }

    fun importBackupJson(jsonString: String): Boolean {
        return try {
            val data = json.decodeFromString(NuvioAABackupData.serializer(), jsonString)
            if (data.addonUrls.isNotEmpty()) {
                AddonStorage.saveInstalledAddonUrls(1, data.addonUrls)
            }
            if (data.addonEnabledStates.isNotEmpty()) {
                AddonStorage.saveAddonEnabledStates(1, data.addonEnabledStates)
            }
            if (data.providerApiKeys.isNotEmpty()) {
                data.providerApiKeys.forEach { (providerId, apiKey) ->
                    DebridSettingsRepository.setProviderApiKey(providerId, apiKey)
                }
            }
            true
        } catch (_: Exception) {
            false
        }
    }
}
