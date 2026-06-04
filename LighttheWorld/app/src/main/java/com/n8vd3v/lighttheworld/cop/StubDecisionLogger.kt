package com.n8vd3v.lighttheworld.cop

data class StubLogEntry(
    val module: String,
    val action: String,
    val details: Map<String, String>,
)

interface StubDecisionLogger {
    fun log(entry: StubLogEntry)
}

object NoOpStubDecisionLogger : StubDecisionLogger {
    override fun log(entry: StubLogEntry) = Unit
}

class InMemoryStubDecisionLogger : StubDecisionLogger {
    private val mutableEntries = mutableListOf<StubLogEntry>()

    val entries: List<StubLogEntry>
        get() = mutableEntries.toList()

    override fun log(entry: StubLogEntry) {
        mutableEntries += entry
    }
}

fun StubDecisionLogger.logDecision(
    module: String,
    action: String,
    details: Map<String, Any?>,
) {
    log(
        StubLogEntry(
            module = module,
            action = action,
            details = details.mapValues { (_, value) -> value?.toString() ?: "null" },
        ),
    )
}
