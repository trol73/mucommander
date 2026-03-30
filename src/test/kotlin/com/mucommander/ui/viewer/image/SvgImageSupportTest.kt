package com.mucommander.ui.viewer.image

import io.kotest.matchers.ints.shouldBeAtLeast
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.apache.batik.transcoder.Transcoder
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.PNGTranscoder
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class SvgImageSupportTes {
    @Test
    fun `SVG image loader`() {

        val width = 400f
        val height = 400f
        // create a PNG transcoder.
        val t: Transcoder = PNGTranscoder().apply {
            addTranscodingHint(PNGTranscoder.KEY_XML_PARSER_VALIDATING, false)
            addTranscodingHint(PNGTranscoder.KEY_WIDTH, width)
            addTranscodingHint(PNGTranscoder.KEY_HEIGHT, height)
        }
        val svg = javaClass.classLoader.getResourceAsStream("images/circle.svg")
        svg.shouldNotBeNull()

        val out = ByteArrayOutputStream()
        val input = TranscoderInput(svg)
        val output = TranscoderOutput(out)
        t.transcode(input, output)
        out.size() shouldBeAtLeast 11379
        with(ByteBuffer.wrap(out.toByteArray())) {
            getByte() shouldBe 0x89
            getByte() shouldBe 'P'.toInt()
            getByte() shouldBe 'N'.toInt()
            getByte() shouldBe 'G'.toInt()
            getByte() shouldBe 0x0d
            getByte() shouldBe 0x0a
            getByte() shouldBe 0x1a
            getByte() shouldBe 0x0a
            getInt()
            getInt()
            getInt() shouldBe width
            getInt() shouldBe height
        }

    }

    private fun ByteBuffer.getByte() = get().toInt() and 0xFF
}