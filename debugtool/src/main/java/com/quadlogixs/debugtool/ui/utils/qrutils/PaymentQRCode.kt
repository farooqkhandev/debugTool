package com.quadlogixs.debugtool.ui.utils.qrutils

import kotlinx.serialization.Serializable

@Serializable
class PaymentQRCode(
    var payLoadFormatIndicator: String = "",
    var type: String = "",
    var method: String = "",
    var qrString: String = "",

    //P2P
    var fName: String = "",
    var iban: String = "",
    var schemeId: String = "",
    var purposeOfPayment: String = "Other",
    var expiryDate: String = "",

    //Merchant
    var countryCode: String = "",
    var currency: String = "",
    var merchantCategoryCode: String = "",
    var merchantName: String = "",
    var merchantCity: String = "",
    var tipOrFeeAmount: String = "",
    var tipOrFeePercent: String = "",

    var merchantAccInformation: MutableList<MerchantAccountInformation> = mutableListOf(),
    var additionalData: AdditionalData? = AdditionalData(),
    var UETR: String = "",
    var uuid: String = "",
    var contextOfTransaction: String = "",
    //Common
    var amount: String = "",
    var crc: String = "",
)

@Serializable
data class MerchantAccountInformation(
    var accountType: String = "",
    var accountInformation: String = "",
    var swiftCode: String = "",
    var iban: String = "",
    var uuid: String = "",
)

@Serializable
data class AdditionalData(
    var billNumber: String = "",
    var mobileNumber: String = "",
    var storeLabel: String = "",
    var loyaltyNumber: String = "",
    var referenceLabel: String = "",
    var customerLabel: String = "",
    var tillCode: String = "",
    var purposeOfTransaction: String = "",
    var additionalConsumerDataRequest: String = "",
    var merchantTaxID: String = "",
    var merchantChannel: String = "",
    var dueDate: String = "",
    var amountAfterDueDate: String = "",
    var qRReferenceId: String = ""
)
