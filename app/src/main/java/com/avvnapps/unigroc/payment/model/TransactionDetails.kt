package com.avvnapps.unigroc.payment.model

data class TransactionDetails(
    val transactionId: String?,
    val responseCode: String?,
    val approvalRefNo: String?,
    val status: String?,
    val transactionRefId: String?
) {
    var appName: String = ""
}
