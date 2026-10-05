package me.zly2006.swiftui.nativeui

/** An owner-scoped LRU. Borrowed entries cannot be evicted by nested operations. */
internal class BoundedResourceCache<K : Any, V : Any>(
    private val maxEntries: Int,
    private val maxWeight: Int,
    private val create: (K) -> V,
    private val destroy: (V) -> Unit,
) : AutoCloseable {
    private class Entry<V>(
        val value: V,
        val weight: Int,
        var recency: Long,
    ) {
        var borrowers = 0
    }

    private val entries = linkedMapOf<K, Entry<V>>()
    private var closed = false
    private var clock = 0L
    var weight = 0
        private set
    val size get() = entries.size
    var hits = 0L
        private set
    var constructions = 0L
        private set
    var evictions = 0L
        private set

    init {
        require(maxEntries >= 0 && maxWeight >= 0)
    }

    fun <R> use(
        key: K,
        entryWeight: Int,
        block: (V) -> R,
    ): R {
        check(!closed)
        require(entryWeight >= 0)
        val hit = entries[key]
        if (hit != null) {
            hit.recency = ++clock
            hits++
            hit.borrowers++
            return try {
                block(hit.value)
            } finally {
                hit.borrowers--
            }
        }
        val cacheable = maxEntries > 0 && entryWeight <= maxWeight && makeRoom(entryWeight)
        val value = create(key)
        constructions++
        if (!cacheable) {
            return try {
                block(value)
            } finally {
                destroy(value)
            }
        }
        val entry = Entry(value, entryWeight, ++clock)
        entries[key] = entry
        weight += entryWeight
        entry.borrowers++
        return try {
            block(value)
        } finally {
            entry.borrowers--
        }
    }

    private fun makeRoom(incoming: Int): Boolean {
        while (entries.size >= maxEntries || weight > maxWeight - incoming) {
            var candidate: Map.Entry<K, Entry<V>>? = null
            for (entry in entries.entries) {
                if (entry.value.borrowers == 0 && (candidate == null || entry.value.recency < candidate.value.recency)) {
                    candidate = entry
                }
            }
            if (candidate == null) return false
            entries.remove(candidate.key)
            weight -= candidate.value.weight
            evictions++
            destroy(candidate.value.value)
        }
        return true
    }

    override fun close() {
        if (closed) return
        check(entries.values.all { it.borrowers == 0 }) { "Close the resource cache after all operations return" }
        closed = true
        val held = entries.values.toList()
        entries.clear()
        weight = 0
        held.forEach { destroy(it.value) }
    }
}

/** Limits retained geometry per backend; zero entries disables path caching.
 * @property maxEntries Maximum number of retained paths; zero disables retention.
 * @property maxCommands Maximum total command count across retained paths.
 */
data class NativePathCacheLimits(
    val maxEntries: Int = 32,
    val maxCommands: Int = 2048,
) {
    init {
        require(maxEntries >= 0 && maxCommands >= 0)
    }
}

/** Snapshot of backend-owned path reuse and retention.
 * @property hits Number of borrows that reused a retained native path.
 * @property constructions Number of native paths built, including transient oversized paths.
 * @property evictions Number of retained paths released to satisfy cache bounds.
 * @property entries Number of paths currently retained by the cache.
 * @property commands Total command count across currently retained paths.
 */
data class NativePathCacheStatistics(
    val hits: Long,
    val constructions: Long,
    val evictions: Long,
    val entries: Int,
    val commands: Int,
)
