package com.quadlogixs.debugtool.ui.utils.qrutils

object QRTagDescriptions {

    const val QR_TYPE_P2M = "P2M"
    const val QR_TYPE_P2P = "P2P"

    private val rootP2MDescriptions = mapOf(
        "00" to "Payload Format Indicator",
        "01" to "Point of Initiation Method",
        "52" to "Merchant Category Code",
        "53" to "Transaction Currency",
        "54" to "Transaction Amount",
        "55" to "Tip or Convenience Fee Indicator",
        "58" to "Country Code",
        "59" to "Merchant Name",
        "60" to "Merchant City",
        "62" to "Additional Data Field Template",
        "63" to "CRC",
        "64" to "Merchant Information - Language Template",
        "80" to "Context of Transaction",
        "81" to "Discounts & Loyalty Programs",
        "82" to "Offline-to-Online (O2O)",
        "83" to "e-Commerce",
    )

    private val rootP2PDescriptions = mapOf(
        "00" to "Payload Format Indicator",
        "01" to "Point of Initiation Method",
        "02" to "Scheme Identifier",
        "03" to "Beneficiary Name",
        "04" to "IBAN",
        "05" to "Amount",
        "06" to "Purpose of Payment",
        "07" to "Expiry Date",
        "10" to "CRC",
    )

    private val raastMaiSubTags = mapOf(
        "00" to "GUID (UUID without hyphens)",
        "01" to "Creditor Institution BIC (MSP)",
        "02" to "Merchant IBAN",
    )

    private val convenienceFeeSubTags = mapOf(
        "56" to "Value of Convenience Fee (fixed)",
        "57" to "Value of Convenience Fee (%)",
    )

    private val additionalDataSubTags = mapOf(
        "01" to "Bill Number",
        "02" to "Mobile Number",
        "03" to "Store Label",
        "04" to "Loyalty Number",
        "05" to "Reference Label",
        "06" to "Customer Label",
        "07" to "Terminal Label (Till Code)",
        "08" to "Purpose of Transaction",
        "09" to "Additional Consumer Data Request",
        "10" to "Merchant Tax ID",
        "11" to "Merchant Channel",
        "50" to "Due Date",
        "51" to "Amount after Due Date",
    )

    private val languageTemplateSubTags = mapOf(
        "00" to "Language Code",
        "01" to "Alternate Merchant Name",
        "02" to "Alternate Merchant City",
    )

    private val contextOfTransactionSubTags = mapOf(
        "00" to "Context of Transaction",
    )

    private val schemeSpecificSubTags = mapOf(
        "00" to "UUID",
        "01" to "UETR",
    )

    private val currencyCodes = mapOf(
        "586" to "PKR",
    )

    fun getRootDescription(tag: String, qrType: String): String {
        return when (qrType) {
            QR_TYPE_P2P -> rootP2PDescriptions[tag]
            else -> rootP2MDescriptions[tag]
        } ?: getMaiDescription(tag) ?: getSchemeSpecificRootDescription(tag) ?: "Tag $tag"
    }

    fun getMaiDescription(tag: String): String? {
        val tagInt = tag.toIntOrNull() ?: return null
        return when (tagInt) {
            in 2..3 -> "Merchant Account Information - Visa"
            in 4..5 -> "Merchant Account Information - Mastercard"
            in 6..8 -> "Merchant Account Information - EMVCo"
            in 9..10 -> "Merchant Account Information - Discover"
            in 11..12 -> "Merchant Account Information - Amex"
            in 13..14 -> "Merchant Account Information - JCB"
            in 15..16 -> "Merchant Account Information - UnionPay"
            in 17..25 -> "Merchant Account Information - EMVCo"
            in 26..27 -> "Merchant Account Information - Reserved for future"
            in 28..30 -> "Merchant Account Information - Raast"
            in 31..51 -> "Merchant Account Information - Reserved for future"
            else -> null
        }
    }

    private fun getSchemeSpecificRootDescription(tag: String): String? {
        val tagInt = tag.toIntOrNull() ?: return null
        return when (tagInt) {
            in 84..86 -> "Scheme Specific"
            in 87..88 -> "Acquirer Specific"
            in 89..99 -> "RFU - SBP"
            else -> null
        }
    }

    fun getNestedDescription(parentTag: String, childTag: String): String {
        return when (parentTag) {
            in raastMaiTagRange() -> raastMaiSubTags[childTag] ?: "Sub-tag $childTag"
            "55" -> convenienceFeeSubTags[childTag] ?: "Sub-tag $childTag"
            "62" -> additionalDataSubTags[childTag] ?: "Sub-tag $childTag"
            "64" -> languageTemplateSubTags[childTag] ?: "Sub-tag $childTag"
            "80" -> contextOfTransactionSubTags[childTag] ?: "Sub-tag $childTag"
            in schemeSpecificTagRange() -> schemeSpecificSubTags[childTag] ?: "Sub-tag $childTag"
            else -> "Sub-tag $childTag"
        }
    }

    fun shouldParseNested(parentTag: String): Boolean {
        return parentTag in raastMaiTagRange() ||
            parentTag == "55" ||
            parentTag == "62" ||
            parentTag == "64" ||
            parentTag == "80" ||
            parentTag in schemeSpecificTagRange()
    }

    fun enrichValueDescription(tag: String, value: String, qrType: String): String {
        return when (tag) {
            "00" -> when (value) {
                "01" -> "P2M"
                "02" -> "P2P"
                else -> value
            }
            "01" -> when (value) {
                "11" -> "Static QR"
                "12" -> "Dynamic QR"
                else -> value
            }
            "02" -> if (qrType == QR_TYPE_P2P) {
                getSchemeDetails(value) ?: value
            } else {
                value
            }
            "53" -> currencyCodes[value]?.let { "$value ($it)" } ?: value
            "58" -> if (value == "PK") "PK (Pakistan)" else value
            "55" -> when (value) {
                "01" -> "Prompt customer for tip"
                "02" -> "Fixed tip amount included"
                "03" -> "Percentage-based tip"
                else -> value
            }
            else -> value
        }
    }

    fun detectQrType(tag00Value: String?): String {
        return when (tag00Value) {
            "02" -> QR_TYPE_P2P
            "01" -> QR_TYPE_P2M
            else -> QR_TYPE_P2M
        }
    }

    private fun raastMaiTagRange(): Set<String> =
        (28..30).map { it.toString().padStart(2, '0') }.toSet()

    private fun schemeSpecificTagRange(): Set<String> =
        (84..86).map { it.toString().padStart(2, '0') }.toSet()
}
