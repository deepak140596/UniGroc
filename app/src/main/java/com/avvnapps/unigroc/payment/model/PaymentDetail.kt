package com.avvnapps.unigroc.payment.model

import java.io.Serializable

data class PaymentDetail(
    var vpa: String,
    var name: String,
    var payeeMerchantCode: String,
    //var txnId: String,
    var txnRefId: String,
    var description: String,
    var amount: String
) : Serializable {

    /**
     * some apps will anyway overwrite txnId like paytm, so it's better to be leave as empty string
     */
    var txnId: String = ""
        private set
    var currency: String = "INR"
        private set

    companion object {
        val ARG_BUNDLE = PaymentDetail::class.java.name + "arg_bundle"
    }
}
