package com.avvnapps.unigroc.utils

import de.tobiasschuerg.money.Currency

class PriceFormatter {
    val inr = Currency("INR", "INR", 1.00)

    val euro = Currency("EUR", "Euro", 1.0)
    val usd = Currency("USD", "USD", 0.013296)

    
    // equivalent to static scope
    companion object {

        val CURRENCY_SYMBOL = "₹"

        var CURRENCY_FORMAT = " %.2f"

        fun getCurrencySymbol(): String {
            return CURRENCY_SYMBOL
        }

        fun getCurrencyFormat(): String {
            return CURRENCY_FORMAT
        }

        fun getFormattedPrice(price: Double): String {
            return CURRENCY_SYMBOL + String.format(CURRENCY_FORMAT, price)
        }
    }

}