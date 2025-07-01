package com.deepromeet.atcha.common.utils

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.StringWriter
import java.nio.charset.StandardCharsets
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

object PrettyBodyFormatter {
    private val jsonMapper = ObjectMapper()

    fun format(
        body: ByteArray,
        headers: Map<String, Collection<String>>
    ): String {
        val raw = body.toString(StandardCharsets.UTF_8).trim()
        val contentType = headers["Content-Type"]?.firstOrNull() ?: ""

        return when {
            contentType.contains("application/json", true) ||
                raw.startsWith("{") || raw.startsWith("[") -> prettyJson(raw)

            contentType.contains("xml", true) ||
                raw.startsWith("<") -> prettyXml(raw)

            else -> raw
        }
    }

    private fun prettyJson(raw: String): String =
        runCatching {
            jsonMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(jsonMapper.readTree(raw))
        }.getOrDefault(raw)

    private fun prettyXml(raw: String): String =
        runCatching {
            val doc =
                DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(raw.byteInputStream())

            val transformer =
                TransformerFactory.newInstance()
                    .newTransformer().apply {
                        setOutputProperty(OutputKeys.INDENT, "yes")
                        setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
                        setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
                    }

            val writer = StringWriter()
            transformer.transform(DOMSource(doc), StreamResult(writer))
            writer.toString()
        }.getOrDefault(raw)
}
