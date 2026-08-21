package com.littleblueworld.alia.state

import com.littleblueworld.alia.content.BirthdayContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WishRulesTest {
    @Test
    fun `blank draft stays none and nonblank draft becomes draft`() {
        assertEquals(WishState.NONE, WishRules.draftState("  \n"))
        assertEquals(WishState.DRAFT, WishRules.draftState("a wish"))
    }

    @Test
    fun `sealing is valid only from editable state with content`() {
        assertFalse(WishRules.canSeal(WishState.NONE, ""))
        assertTrue(WishRules.canSeal(WishState.NONE, "a wish"))
        assertTrue(WishRules.canSeal(WishState.DRAFT, "a wish"))
        assertFalse(WishRules.canSeal(WishState.SEALED, "a wish"))
        assertFalse(WishRules.canSeal(WishState.KEPT_LOCAL, "a wish"))
    }

    @Test
    fun `destination is available only after seal`() {
        WishState.entries.filter { it != WishState.SEALED }.forEach { state ->
            assertFalse(WishRules.canChooseDestination(state))
        }
        assertTrue(WishRules.canChooseDestination(WishState.SEALED))
    }

    @Test
    fun `wish interaction and privacy copy stay frozen`() {
        val content = BirthdayContent()

        assertEquals(listOf("one last thing.", "make a wish."), content.wishEntry)
        assertEquals("hold to seal my wish", content.wishSealCta)
        assertEquals("where should this wish go?", content.wishDestination)
        assertEquals("KEEP IT WITH ME", content.wishKeepTitle)
        assertEquals("SEND IT INTO THE LITTLE WORLD", content.wishSendTitle)
        assertEquals("the person who made this can see it.", content.wishSendDescription)
        assertEquals(1_350L, WishRules.HOLD_DURATION_MS)
        assertEquals(10, WishRules.HOLD_PROGRESS_BLOCKS)
    }

    @Test
    fun `request id accepts canonical UUID only`() {
        assertTrue(WishRules.isValidRequestId("3f67e80a-3912-4f5d-a1e7-c321bfecbd17"))
        assertFalse(WishRules.isValidRequestId("not-a-uuid"))
        assertFalse(WishRules.isValidRequestId("3F67E80A-3912-4F5D-A1E7-C321BFECBD17"))
    }
}
