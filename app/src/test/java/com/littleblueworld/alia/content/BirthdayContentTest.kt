package com.littleblueworld.alia.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BirthdayContentTest {

    private val content = BirthdayContent()

    @Test
    fun frozenCollectionsHaveRequiredCardinality() {
        assertEquals(4, content.bottleMessages.size)
        assertEquals(6, content.archiveCaptions.size)
        assertEquals(5, content.faceReveal.size)
        assertEquals(500, BirthdayContent.MAX_WISH_LENGTH)
    }

    @Test
    fun signatureLoadingLineHasRequiredImmediateResult() {
        assertTrue(content.loadingPersonal.contains(content.loadingSignatureStart))
        assertEquals("found it.", content.loadingSignatureResult)
    }

    @Test
    fun photoAccessibilityLabelsAreOneBasedAndBounded() {
        assertEquals("Photo of Alia, 1 of 6", content.photoAccessibilityLabel(1))
        assertEquals("Photo of Alia, 6 of 6", content.photoAccessibilityLabel(6))
        assertThrows(IllegalArgumentException::class.java) {
            content.photoAccessibilityLabel(0)
        }
    }
}
