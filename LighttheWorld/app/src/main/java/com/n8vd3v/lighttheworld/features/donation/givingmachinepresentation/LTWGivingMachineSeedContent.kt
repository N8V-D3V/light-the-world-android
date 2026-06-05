package com.n8vd3v.lighttheworld.features.donation.givingmachinepresentation

object LTWGivingMachineSeedContent {

    fun defaultEnvironment(): GivingMachinePresentationEnvironment = environmentFor(defaultContent())

    fun defaultContent(): LTWGivingMachinePresentationContent = LTWGivingMachinePresentationContent(
        catalogItems = catalogItems(),
        infoPresentationContent = infoPresentationContent(),
    )

    fun environmentFor(
        content: LTWGivingMachinePresentationContent,
    ): GivingMachinePresentationEnvironment = GivingMachinePresentationEnvironment(
        homeChallengeSurfaceState = GivingMachineHomeSurfaceState(isVisible = true),
        catalogContext = GivingMachinePresentationCatalogContext(
            givingMachineCatalog = content.catalogItems,
            currentCatalogState = GivingMachineCatalogPresentationState.AVAILABLE,
        ),
        infoPresentationContent = content.infoPresentationContent,
    )

    fun infoPresentationContent(): InfoPresentationContent = InfoPresentationContent(
        screenTitle = "About Giving Machine",
        contentSections = listOf(
            InfoContentSection(
                title = "How it works",
                body = "Arm a gift with one tap, confirm it from the machine, and then continue into the calmer cart and checkout presentation.",
            ),
            InfoContentSection(
                title = "Campaign focus",
                body = "Giving Machine donations complement the daily challenge experience by turning generosity into a clear seasonal action.",
            ),
            InfoContentSection(
                title = "Acknowledgements",
                body = "This presentation content is loaded from a local JSON source today and is designed to be replaceable by a future remote content source.",
            ),
        ),
    )

    fun catalogItems(): List<PresentedGivingMachineItem> = listOf(
        item("01", "take-home-meal", "Take Home Meal", "Help send a ready meal home with a child."),
        item("02", "refugee-backpack", "Backpack for a Refugee", "Fill a backpack with school supplies for a newly arrived refugee."),
        item("03", "warm-blankets", "Warm Blankets", "Provide warm blankets for a family shelter stay."),
        item("04", "winter-coat", "Winter Coat", "Supply a winter coat for cold-weather shelter support."),
        item("05", "baby-care-kit", "Baby Care Kit", "Support parents with diapers, wipes, and baby essentials."),
        item("06", "school-kit", "School Kit", "Equip a student with notebooks, pencils, and classroom basics."),
        item("07", "food-box", "Food Box", "Stock a pantry box with staples for the week ahead."),
        item("08", "water-filters", "Water Filters", "Help a household access cleaner, safer drinking water."),
        item("09", "clinic-visit", "Clinic Visit", "Cover a basic wellness visit for someone without easy access."),
        item("10", "transit-pass", "Transit Pass", "Provide local transportation support for work or appointments."),
        item("11", "hygiene-packs", "Hygiene Packs", "Prepare soap, toothbrushes, and care items for outreach teams."),
        item("12", "garden-starter", "Garden Starter", "Supply seeds and tools for a family food garden."),
    )

    private fun item(
        slotNumber: String,
        itemIdentifier: String,
        title: String,
        description: String,
    ) = PresentedGivingMachineItem(
        itemIdentifier = itemIdentifier,
        slotNumber = slotNumber,
        title = title,
        description = description,
        selectionState = GivingMachineSlotSelectionStateValue.UNSELECTED,
    )
}
