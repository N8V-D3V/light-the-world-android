package com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class LTWGivingMachinePresentationContent(
    val catalogItems: List<PresentedGivingMachineItem>,
    val infoPresentationContent: InfoPresentationContent,
)

interface LTWGivingMachinePresentationContentSource {
    fun loadContent(): LTWGivingMachinePresentationContent?
}

class LTWJsonGivingMachinePresentationContentSource(
    private val context: Context,
) : LTWGivingMachinePresentationContentSource {

    private val cachedContent: LTWGivingMachinePresentationContent? by lazy {
        loadContentFromAssets()
    }

    override fun loadContent(): LTWGivingMachinePresentationContent? = cachedContent

    private fun loadContentFromAssets(): LTWGivingMachinePresentationContent? {
        return try {
            val jsonString = context.assets
                .open("giving_machine_content.json")
                .bufferedReader()
                .use { it.readText() }
            val root = JSONObject(jsonString)
            LTWGivingMachinePresentationContent(
                catalogItems = parseCatalogItems(root.getJSONArray("catalogItems")),
                infoPresentationContent = parseInfoPresentation(root.getJSONObject("infoPresentation")),
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun parseCatalogItems(
        catalogItemsJson: JSONArray,
    ): List<PresentedGivingMachineItem> = buildList {
        for (index in 0 until catalogItemsJson.length()) {
            val item = catalogItemsJson.getJSONObject(index)
            add(
                PresentedGivingMachineItem(
                    itemIdentifier = item.getString("itemIdentifier"),
                    slotNumber = item.getString("slotNumber"),
                    title = item.getString("title"),
                    description = item.getString("description"),
                    imageReference = item.optString("imageReference").takeIf { it.isNotBlank() },
                    selectionState = GivingMachineSlotSelectionStateValue.UNSELECTED,
                ),
            )
        }
    }

    private fun parseInfoPresentation(
        infoPresentationJson: JSONObject,
    ): InfoPresentationContent = InfoPresentationContent(
        screenTitle = infoPresentationJson.getString("screenTitle"),
        contentSections = parseInfoSections(infoPresentationJson.getJSONArray("contentSections")),
    )

    private fun parseInfoSections(
        contentSectionsJson: JSONArray,
    ): List<InfoContentSection> = buildList {
        for (index in 0 until contentSectionsJson.length()) {
            val section = contentSectionsJson.getJSONObject(index)
            add(
                InfoContentSection(
                    title = section.getString("title"),
                    body = section.getString("body"),
                ),
            )
        }
    }
}
