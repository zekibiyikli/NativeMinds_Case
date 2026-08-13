package com.zekibiyikli.nativemindscase.data.premium

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.zekibiyikli.nativemindscase.core.config.AppConfig
import com.zekibiyikli.nativemindscase.core.time.TimeProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class PremiumRepositoryTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    /** Gun sinirini testte elle atlayabilmek icin. */
    private class FakeTimeProvider(var millis: Long = 0L) : TimeProvider {
        override fun nowMillis(): Long = millis
        fun advanceDays(days: Int) {
            millis += days * 24L * 60 * 60 * 1000
        }
    }

    private fun TestScope.newRepository(time: TimeProvider): PremiumRepository {
        val file = File(temporaryFolder.root, "test_${System.nanoTime()}.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { file }
        )
        return PremiumRepository(dataStore, time)
    }

    @Test
    fun `gunluk limite kadar erisim verir sonra reddeder`() = runTest {
        val repository = newRepository(FakeTimeProvider())

        repeat(AppConfig.Premium.FREE_DAILY_LIMIT) { index ->
            assertTrue(
                "$index. okuma izin almaliydi",
                repository.registerRead(itemId = "item_$index").isGranted
            )
        }

        assertFalse(repository.registerRead(itemId = "limit_ustu").isGranted)
        assertEquals(0, repository.remainingFreeReads.first())
    }

    @Test
    fun `ayni icerigi tekrar acmak kotadan dusmez`() = runTest {
        val repository = newRepository(FakeTimeProvider())

        assertTrue(repository.registerRead(itemId = "ayni").isGranted)
        assertTrue(repository.registerRead(itemId = "ayni").isGranted)
        assertTrue(repository.registerRead(itemId = "ayni").isGranted)

        assertEquals(AppConfig.Premium.FREE_DAILY_LIMIT - 1, repository.remainingFreeReads.first())
    }

    @Test
    fun `gun degisince kota sifirlanir`() = runTest {
        val time = FakeTimeProvider()
        val repository = newRepository(time)

        repeat(AppConfig.Premium.FREE_DAILY_LIMIT) { index ->
            repository.registerRead(itemId = "item_$index")
        }
        assertFalse(repository.registerRead(itemId = "limit_ustu").isGranted)

        time.advanceDays(1)

        assertTrue(repository.registerRead(itemId = "ertesi_gun").isGranted)
        assertEquals(AppConfig.Premium.FREE_DAILY_LIMIT - 1, repository.remainingFreeReads.first())
    }

    @Test
    fun `premium kullaniciya limit uygulanmaz`() = runTest {
        val repository = newRepository(FakeTimeProvider())
        repository.setPremium(enabled = true)

        repeat(AppConfig.Premium.FREE_DAILY_LIMIT + 5) { index ->
            assertTrue(repository.registerRead(itemId = "item_$index").isGranted)
        }

        // Premium'da kota hic dokunulmadigi icin kalan hak tam.
        assertEquals(AppConfig.Premium.FREE_DAILY_LIMIT, repository.remainingFreeReads.first())
    }
}
