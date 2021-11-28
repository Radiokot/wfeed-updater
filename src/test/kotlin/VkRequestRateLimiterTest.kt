import org.junit.Assert
import org.junit.Test
import ua.com.radiokot.feed.updater.vk.util.VkRequestRateLimiter
import kotlin.math.roundToInt

class VkRequestRateLimiterTest {
    @Test
    fun singleRequest() {
        VkRequestRateLimiter.reset()

        val startTime = System.currentTimeMillis()
        VkRequestRateLimiter.waitBeforeRequest()
        val elapsed = (System.currentTimeMillis() - startTime)

        Assert.assertTrue(
                "There must be no significant delay for a single first request",
                elapsed <= 50
        )
    }

    @Test
    fun requestsInSeries() {
        VkRequestRateLimiter.reset()

        val startTime = System.currentTimeMillis()

        val count = 5
        repeat(count) { VkRequestRateLimiter.waitBeforeRequest() }

        val elapsed = (System.currentTimeMillis() - startTime).toDouble()

        val expectedDelays = count - 1
        Assert.assertEquals(
                "There must be $expectedDelays delays ${VkRequestRateLimiter.REQUEST_TIMEOUT_MS} ms each",
                expectedDelays,
                (elapsed / VkRequestRateLimiter.REQUEST_TIMEOUT_MS).roundToInt(),
        )
    }

    @Test
    fun requestAfterLongTime() {
        VkRequestRateLimiter.reset()
    }
}