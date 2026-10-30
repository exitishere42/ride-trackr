package de.exit.ridetrackr.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownParserTest {

    @Test
    fun testParseInlineMarkdown_bold() {
        val input = "This is **bold text** and regular."
        val annotated = parseInlineMarkdown(input)

        assertEquals("This is bold text and regular.", annotated.text)
        assertTrue("Should have span styles", annotated.spanStyles.isNotEmpty())
    }

    @Test
    fun testParseInlineMarkdown_code() {
        val input = "Use `update-runner` now."
        val annotated = parseInlineMarkdown(input)

        assertEquals("Use update-runner now.", annotated.text)
    }

    @Test
    fun testParseInlineMarkdown_plainText() {
        val input = "Just standard text without markdown."
        val annotated = parseInlineMarkdown(input)

        assertEquals(input, annotated.text)
    }
}
