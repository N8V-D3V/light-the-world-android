package com.n8vd3v.lighttheworld.cop

data class StubLogEntry(
    val module: String,
    val action: String,
    val details: Map<String, String>,
)

interface StubModuleLogger {
    fun log(entry: StubLogEntry)
}

object NoOpStubModuleLogger : StubModuleLogger {
    override fun log(entry: StubLogEntry) = Unit
}

class InMemoryStubModuleLogger : StubModuleLogger {
    private val mutableEntries = mutableListOf<StubLogEntry>()

    val entries: List<StubLogEntry>
        get() = mutableEntries.toList()

    override fun log(entry: StubLogEntry) {
        mutableEntries += entry
    }
}

fun StubModuleLogger.logDecision(
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
