package me.zly2006.swiftui.nativeui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class BoundedResourceCacheTest {
    private class Resource(
        val key: String,
    ) {
        var releases = 0
    }

    private class Fixture(
        entries: Int = 2,
        weight: Int = 4,
    ) {
        val created = mutableListOf<Resource>()
        val cache = BoundedResourceCache<String, Resource>(entries, weight, { Resource(it).also(created::add) }, { it.releases++ })
    }

    @Test fun hitsReuseResourcesAndLruEvictionKeepsTheRecentlyUsedEntry() {
        val f = Fixture()
        val a = f.cache.use("a", 2) { it }
        val b = f.cache.use("b", 2) { it }
        assertSame(a, f.cache.use("a", 2) { it })
        f.cache.use("c", 2) { }
        assertEquals(0, a.releases)
        assertEquals(1, b.releases)
        assertEquals(2, f.cache.size)
        assertEquals(4, f.cache.weight)
        f.cache.close()
        f.cache.close()
        assertTrue(f.created.all { it.releases == 1 })
        assertFailsWith<IllegalStateException> { f.cache.use("a", 2) { } }
    }

    @Test fun pinnedEntriesSurviveNestedBorrowingAndOverflowIsTemporary() {
        val f = Fixture(1, 2)
        f.cache.use("a", 2) { a ->
            f.cache.use("b", 2) { b ->
                assertEquals(0, a.releases)
                assertEquals(0, b.releases)
                assertEquals(1, f.cache.size)
            }
            assertEquals(1, f.created.single { it.key == "b" }.releases)
            assertEquals(0, a.releases)
            assertFailsWith<IllegalStateException> { f.cache.close() }
        }
        f.cache.close()
        assertTrue(f.created.all { it.releases == 1 })
    }

    @Test fun oversizedEntriesAreNeverRetainedAndExceptionsReleaseTemporaryResources() {
        val f = Fixture(2, 3)
        assertFailsWith<IllegalArgumentException> { f.cache.use("large", 4) { throw IllegalArgumentException("body") } }
        assertEquals(0, f.cache.size)
        assertEquals(0, f.cache.weight)
        assertEquals(1, f.created.single().releases)
        f.cache.use("a", 2) { }
        f.cache.use("b", 2) { }
        assertEquals(1, f.cache.size)
        assertEquals(2, f.cache.weight)
        f.cache.close()
        assertTrue(f.created.all { it.releases == 1 })
    }

    @Test fun disabledCachesAndSeparateOwnersDoNotShareResources() {
        val disabled = Fixture(0, 4)
        repeat(3) { disabled.cache.use("a", 2) { } }
        assertEquals(3, disabled.created.size)
        assertTrue(disabled.created.all { it.releases == 1 })
        val first = Fixture()
        val second = Fixture()
        assertNotEquals(first.cache.use("a", 2) { it }, second.cache.use("a", 2) { it })
        first.cache.close()
        second.cache.close()
        disabled.cache.close()
    }

    @Test fun nativePathSnapshotsInputAndKeepsHashKeysStable() {
        val input = mutableListOf<Path.Element>(Path.Element.Move(0.0, 0.0), Path.Element.Line(1.0, 1.0))
        val geometry = Path(input)
        val hash = geometry.hashCode()
        input[1] = Path.Element.Line(9.0, 9.0)
        assertEquals(hash, geometry.hashCode())
        assertEquals(Path.Element.Line(1.0, 1.0), geometry.commands[1])
        assertEquals(geometry, geometry.copy())
        assertNotEquals(geometry, Path(input))
    }
}
