/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2025 Oleg Trifonov
 *
 * trolCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * trolCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.ui.viewer.text.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import org.w3c.dom.Document
import org.w3c.dom.ls.DOMImplementationLS
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.SAXParseException
import java.io.IOException
import java.io.StringReader
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

/**
 * Created by trol on 18/09/14.
 * 
 */
object CodeFormatter {
    private val GSON: Gson = GsonBuilder().setPrettyPrinting().create()
    private val JSON_PARSER = JsonParser()

    @JvmStatic
    @Throws(CodeFormatException::class)
    fun formatXml(unformattedXml: String): String {
        if (unformattedXml.trim { it <= ' ' }.isEmpty()) {
            throw CodeFormatException("XML input is null or empty", 0, 0, null)
        }

        try {
            val document = parseXmlFile(unformattedXml)

            val domImpl = document.implementation as DOMImplementationLS
            val serializer = domImpl.createLSSerializer().apply {
                domConfig.setParameter("format-pretty-print", true)
            }

            val writer = StringWriter()
            val output = domImpl.createLSOutput().apply {
                encoding = "UTF-8"
                characterStream = writer
            }
            serializer.write(document, output)
            return writer.toString()
        } catch (e: CodeFormatException) {
            throw e
        } catch (e: Exception) {
            throw CodeFormatException("Failed to format XML: " + e.message, 0, 0, e)
        }
    }

    @Throws(CodeFormatException::class)
    private fun parseXmlFile(src: String): Document {
        try {
            val dbf = DocumentBuilderFactory.newInstance().apply {
                setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
                setFeature("http://xml.org/sax/features/external-general-entities", false)
                setFeature("http://xml.org/sax/features/external-parameter-entities", false)
                isNamespaceAware = true
            }

            val stream = InputSource(StringReader(src))
            return dbf.newDocumentBuilder().parse(stream)
        } catch (e: SAXParseException) {
            throw CodeFormatException(e.message, e.lineNumber, e.columnNumber, e)
        } catch (e: ParserConfigurationException) {
            throw CodeFormatException("XML Parser configuration error: " + e.message, 0, 0, e)
        } catch (e: SAXException) {
            throw CodeFormatException("Failed to parse XML: " + e.message, 0, 0, e)
        } catch (e: IOException) {
            throw CodeFormatException("Failed to parse XML: " + e.message, 0, 0, e)
        }
    }


    @JvmStatic
    @Throws(CodeFormatException::class)
    fun formatJson(json: String): String? {
        if (json.trim { it <= ' ' }.isEmpty()) {
            throw CodeFormatException("JSON input is null or empty", 0, 0, null)
        }

        try {
            val el = JSON_PARSER.parse(json)
            return GSON.toJson(el)
        } catch (e: JsonSyntaxException) {
            throw CodeFormatException("Invalid JSON syntax: " + e.message, 0, 0, e)
        } catch (e: Exception) {
            throw CodeFormatException("Failed to format JSON: " + e.message, 0, 0, e)
        }
    }
}