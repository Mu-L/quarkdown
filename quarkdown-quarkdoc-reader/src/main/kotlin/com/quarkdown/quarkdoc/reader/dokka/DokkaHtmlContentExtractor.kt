package com.quarkdown.quarkdoc.reader.dokka

import com.quarkdown.quarkdoc.reader.DocsContentExtractor
import com.quarkdown.quarkdoc.reader.DocsFunction
import com.quarkdown.quarkdoc.reader.DocsParameter
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

private const val PARAMETERS_HEADER = "Parameters"
private const val CHAINING_HEADER = "Chaining"

private const val OPTIONAL_PARAMETER_PROPERTY = "optional"
private const val NAMED_PARAMETER_PROPERTY = "named"
private const val BODY_PARAMETER_PROPERTY = "body"

/**
 * Extractor of content from Dokka-generated HTML files.
 */
class DokkaHtmlContentExtractor(
    private val html: String,
) : DocsContentExtractor {
    override fun extractContent(): String? =
        Jsoup
            .parse(html)
            .selectFirst("#main .content")
            ?.apply {
                selectFirst(".top-right-position:has(.copy-icon)")?.remove()
            }?.outerHtml()

    override fun extractFunctionData(): DocsFunction? {
        val main =
            Jsoup
                .parse(html)
                .selectFirst("#main > .main-content")
                ?.takeIf { it.attr("data-page-type") == "member" }
                ?: return null

        return DocsFunction(
            name = main.selectFirst("h1")?.text() ?: "x",
            parameters = extractFunctionParameters(main),
            isLikelyChained = main.selectFirst("h4:contains($CHAINING_HEADER)") != null,
        )
    }

    /**
     * Converts a row of the parameters table into a [DocsParameter].
     */
    private fun rowToParameter(row: Element): DocsParameter? {
        val name = row.children().firstOrNull()?.text() ?: return null
        val content = row.selectFirst(".title")
        val descriptionHtml = content?.html() ?: ""
        val properties =
            content?.select("dl:first-child li") // Generated by Quarkdoc's AdditionalParameterPropertiesTransformer.

        fun hasProperty(property: String): Boolean = properties?.any { it.text().contains(property, ignoreCase = true) } == true

        return DocsParameter(
            name = name,
            description = descriptionHtml,
            isOptional = hasProperty(OPTIONAL_PARAMETER_PROPERTY),
            isLikelyNamed = hasProperty(NAMED_PARAMETER_PROPERTY),
            isLikelyBody = hasProperty(BODY_PARAMETER_PROPERTY),
        )
    }

    private fun extractFunctionParameters(document: Element): List<DocsParameter> {
        val table =
            document
                .select("h4:contains($PARAMETERS_HEADER)")
                .firstOrNull()
                ?.nextElementSibling()
                ?: return emptyList()

        return table
            .getElementsByClass("main-subrow")
            .mapNotNull(::rowToParameter)
    }
}
