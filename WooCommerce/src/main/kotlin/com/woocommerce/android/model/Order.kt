package com.woocommerce.android.model

import android.os.Parcelable
import com.woocommerce.android.extensions.fastStripHtml
import com.woocommerce.android.extensions.roundError
import com.woocommerce.android.model.Order.Address
import com.woocommerce.android.model.Order.Address.Type.BILLING
import com.woocommerce.android.model.Order.Address.Type.SHIPPING
import com.woocommerce.android.model.Order.Item
import com.woocommerce.android.ui.products.ProductHelper
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.wordpress.android.fluxc.model.WCOrderModel
import org.wordpress.android.fluxc.model.order.OrderIdentifier
import org.wordpress.android.fluxc.network.rest.wpcom.wc.order.CoreOrderStatus
import org.wordpress.android.fluxc.network.rest.wpcom.wc.order.CoreOrderStatus.PENDING
import org.wordpress.android.util.DateTimeUtils
import java.math.BigDecimal
import java.math.RoundingMode.HALF_UP
import java.util.Date

@Parcelize
data class Order(
    val identifier: OrderIdentifier,
    val remoteId: Long,
    val number: String,
    val localSiteId: Int,
    val dateCreated: Date,
    val dateModified: Date,
    val datePaid: Date?,
    val status: CoreOrderStatus,
    val total: BigDecimal,
    val productsTotal: BigDecimal,
    val totalTax: BigDecimal,
    val shippingTotal: BigDecimal,
    val discountTotal: BigDecimal,
    val refundTotal: BigDecimal,
    val currency: String,
    val customerNote: String,
    val discountCodes: String,
    val paymentMethod: String,
    val paymentMethodTitle: String,
    val pricesIncludeTax: Boolean,
    val billingAddress: Address,
    val shippingAddress: Address,
    val items: List<Item>
) : Parcelable {
    @Parcelize
    data class Item(
        val itemId: Long,
        val productId: Long,
        val name: String,
        val price: BigDecimal,
        val sku: String,
        val quantity: Int,
        val subtotal: BigDecimal,
        val totalTax: BigDecimal,
        val total: BigDecimal,
        val variationId: Long
    ) : Parcelable {
        @IgnoredOnParcel
        val uniqueId: Long = ProductHelper.productOrVariationId(productId, variationId)
    }

    @Parcelize
    data class Address(
        val address1: String,
        val address2: String,
        val city: String,
        val company: String,
        val country: String,
        val firstName: String,
        val lastName: String,
        val postcode: String,
        val state: String,
        val type: Type
    ) : Parcelable {
        enum class Type {
            BILLING,
            SHIPPING
        }
    }

    /*
     * Calculates the max quantity for each item by subtracting the number of already-refunded items
     */
    fun getMaxRefundQuantities(
        refunds: List<Refund>,
        unpackagedOrderItems: List<Item> = this.items
    ): Map<Long, Int> {
        val map = mutableMapOf<Long, Int>()
        val groupedRefunds = refunds.flatMap { it.items }.groupBy { it.uniqueId }
        unpackagedOrderItems.map { item ->
            map[item.uniqueId] = item.quantity - (groupedRefunds[item.uniqueId]?.sumBy { it.quantity } ?: 0)
        }
        return map
    }

    fun hasNonRefundedItems(refunds: List<Refund>): Boolean = getMaxRefundQuantities(refunds).values.any { it > 0 }

    fun hasUnpackagedProducts(shippingLabels: List<ShippingLabel>): Boolean {
        val productNames = mutableSetOf<String>()
        shippingLabels.map { productNames.addAll(it.productNames) }
        return this.items.size != productNames.size
    }

    /**
     * Returns products from an order that is not associated with any shipping labels
     * AND is also not refunded
     */
    fun getUnpackagedAndNonRefundedProducts(
        refunds: List<Refund>,
        shippingLabels: List<ShippingLabel>
    ): List<Item> {
        val productNames = mutableSetOf<String>()
        shippingLabels.map { productNames.addAll(it.productNames) }

        val unpackagedProducts = this.items.filter { !productNames.contains(it.name) }
        return getNonRefundedProducts(refunds, unpackagedProducts)
    }

    /**
     * Returns products that are not refunded in an order
     * @param [refunds] List of refunds for the order
     * @param [unpackagedProducts] list of products not associated with any shipping labels.
     * This is left null, in cases where we only want to fetch non refunded products from an order.
     */
    fun getNonRefundedProducts(
        refunds: List<Refund>,
        unpackagedProducts: List<Item> = this.items
    ): List<Item> {
        val leftoverProducts = getMaxRefundQuantities(refunds, unpackagedProducts).filter { it.value > 0 }
        val filteredItems = unpackagedProducts.filter { leftoverProducts.contains(it.uniqueId) }
            .map {
                val newQuantity = leftoverProducts[it.uniqueId]
                val quantity = it.quantity.toBigDecimal()
                val totalTax = if (quantity > BigDecimal.ZERO) {
                    it.totalTax.divide(quantity, 2, HALF_UP)
                } else BigDecimal.ZERO

                it.copy(
                    quantity = newQuantity ?: error("Missing product"),
                    total = it.price.times(newQuantity.toBigDecimal()),
                    totalTax = totalTax
                )
            }
        return filteredItems
    }
}

fun WCOrderModel.toAppModel(): Order {
    return Order(
            OrderIdentifier(this),
            this.remoteOrderId,
            this.number,
            this.localSiteId,
            DateTimeUtils.dateUTCFromIso8601(this.dateCreated) ?: Date(),
            DateTimeUtils.dateUTCFromIso8601(this.dateModified) ?: Date(),
            DateTimeUtils.dateUTCFromIso8601(this.datePaid),
            CoreOrderStatus.fromValue(this.status) ?: PENDING,
            this.total.toBigDecimalOrNull()?.roundError() ?: BigDecimal.ZERO,
            this.getOrderSubtotal().toBigDecimal().roundError(),
            this.totalTax.toBigDecimalOrNull()?.roundError() ?: BigDecimal.ZERO,
            this.shippingTotal.toBigDecimalOrNull()?.roundError() ?: BigDecimal.ZERO,
            this.discountTotal.toBigDecimalOrNull()?.roundError() ?: BigDecimal.ZERO,
            -this.refundTotal.toBigDecimal().roundError(), // WCOrderModel.refundTotal is NEGATIVE
            this.currency,
            this.customerNote,
            this.discountCodes,
            this.paymentMethod,
            this.paymentMethodTitle,
            this.pricesIncludeTax,
            this.getBillingAddress().let {
                Address(
                        it.address1,
                        it.address2,
                        it.city,
                        it.company,
                        it.country,
                        it.firstName,
                        it.lastName,
                        it.postcode,
                        it.state,
                        BILLING
                )
            },
            this.getShippingAddress().let {
                Address(
                        it.address1,
                        it.address2,
                        it.city,
                        it.company,
                        it.country,
                        it.firstName,
                        it.lastName,
                        it.postcode,
                        it.state,
                        SHIPPING
                )
            },
            getLineItemList()
                    .filter { it.productId != null && it.id != null }
                    .map {
                        Item(
                                it.id!!,
                                it.productId!!,
                                it.name?.fastStripHtml() ?: "",
                                it.price?.toBigDecimalOrNull()?.roundError() ?: BigDecimal.ZERO,
                                it.sku ?: "",
                                it.quantity?.toInt() ?: 0,
                                it.subtotal?.toBigDecimalOrNull()?.roundError() ?: BigDecimal.ZERO,
                                it.totalTax?.toBigDecimalOrNull()?.roundError() ?: BigDecimal.ZERO,
                                it.total?.toBigDecimalOrNull()?.roundError() ?: BigDecimal.ZERO,
                                it.variationId ?: 0
                        )
                    }
    )
}
