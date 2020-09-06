package com.woocommerce.android.ui.orders.list

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.woocommerce.android.BuildConfig
import com.woocommerce.android.R
import com.woocommerce.android.model.TimeGroup
import com.woocommerce.android.ui.orders.OrderStatusTag
import com.woocommerce.android.ui.orders.list.OrderListItemUIType.LoadingItem
import com.woocommerce.android.ui.orders.list.OrderListItemUIType.OrderListItemUI
import com.woocommerce.android.ui.orders.list.OrderListItemUIType.SectionHeader
import com.woocommerce.android.util.CurrencyFormatter
import com.woocommerce.android.widgets.tags.TagView
import kotlinx.android.synthetic.main.order_list_item.view.*
import org.wordpress.android.fluxc.model.WCOrderStatusModel
import org.wordpress.android.util.DateTimeUtils
import java.util.Date

class OrderListAdapter(
    val listener: OrderListListener,
    val currencyFormatter: CurrencyFormatter
) : PagedListAdapter<OrderListItemUIType, ViewHolder>(OrderListDiffItemCallback) {
    companion object {
        private const val VIEW_TYPE_ORDER_ITEM = 0
        private const val VIEW_TYPE_SECTION_HEADER = 2
        private const val VIEW_TYPE_LOADING = 1
    }

    var activeOrderStatusMap: Map<String, WCOrderStatusModel> = emptyMap()

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is OrderListItemUI -> VIEW_TYPE_ORDER_ITEM
            is LoadingItem -> VIEW_TYPE_LOADING
            is SectionHeader -> VIEW_TYPE_SECTION_HEADER
            null -> VIEW_TYPE_LOADING // Placeholder by paged list
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ORDER_ITEM -> OrderItemUIViewHolder(R.layout.order_list_item, parent)
            VIEW_TYPE_LOADING -> {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.skeleton_order_list_item_auto, parent, false)
                LoadingViewHolder(view)
            }
            VIEW_TYPE_SECTION_HEADER -> SectionHeaderViewHolder(R.layout.order_list_header, parent)
            else -> {
                // Fail fast if a new view type is added so we can handle it
                throw IllegalStateException("The view type '$viewType' needs to be handled")
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is OrderItemUIViewHolder -> {
                if (BuildConfig.DEBUG && item !is OrderListItemUI) {
                    error(
                        "If we are presenting WCOrderItemUIViewHolder, the item has to be of type WCOrderListUIItem " +
                            "for position: $position"
                    )
                }
                holder.onBind((item as OrderListItemUI))
            }
            is SectionHeaderViewHolder -> {
                if (BuildConfig.DEBUG && item !is SectionHeader) {
                    error(
                        "If we are presenting SectionHeaderViewHolder, the item has to be of type SectionHeader " +
                            "for position: $position"
                    )
                }
                holder.onBind((item as SectionHeader))
            }
            else -> {}
        }
    }

    fun setOrderStatusOptions(orderStatusOptions: Map<String, WCOrderStatusModel>) {
        if (orderStatusOptions.keys != activeOrderStatusMap.keys) {
            this.activeOrderStatusMap = orderStatusOptions
            notifyDataSetChanged()
        }
    }

    /**
     * Returns the order date formatted as a date string, or null if the date is missing or invalid.
     * Note that the year is not shown when it's the same as the current one
     */
    private fun getFormattedOrderDate(context: Context, orderDate: String): String? {
        DateTimeUtils.dateUTCFromIso8601(orderDate)?.let { date ->
            val flags = if (DateTimeUtils.isSameYear(date, Date())) {
                DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_NO_YEAR
            } else {
                DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_MONTH
            }
            return DateUtils.formatDateTime(context, date.time, flags)
        } ?: return null
    }

    private inner class OrderItemUIViewHolder(
        @LayoutRes layout: Int,
        parentView: ViewGroup
    ) : RecyclerView.ViewHolder(LayoutInflater.from(parentView.context).inflate(layout, parentView, false)) {
        private val orderDateView = itemView.orderDate
        private val orderNumView = itemView.orderNum
        private val orderNameView = itemView.orderName
        private val orderTotalView = itemView.orderTotal
        private val orderTagList = itemView.orderTags
        private val divider = itemView.divider

        fun onBind(orderItemUI: OrderListItemUI) {
            // Grab the current context from the underlying view
            val ctx = this.itemView.context
            orderDateView.text = getFormattedOrderDate(ctx, orderItemUI.dateCreated)
            orderNumView.text = "#${orderItemUI.orderNumber}"
            orderNameView.text = orderItemUI.orderName
            orderTotalView.text = currencyFormatter.formatCurrency(orderItemUI.orderTotal, orderItemUI.currencyCode)
            divider.visibility = if (orderItemUI.isLastItemInSection) View.GONE else View.VISIBLE

            // clear existing tags and add new ones
            orderTagList.removeAllViews()
            processTagView(orderItemUI.status, this)

            this.itemView.setOnClickListener {
                listener.openOrderDetail(orderItemUI.remoteOrderId.value, orderItemUI.status)
            }
        }

        /**
         * Converts the order status label into an [OrderStatusTag], creates the associated [TagView],
         * and add it to the holder. No need to trim the label text since this is done in [OrderStatusTag]
         */
        private fun processTagView(status: String, holder: OrderItemUIViewHolder) {
            val orderStatus = activeOrderStatusMap[status]
                    ?: createTempOrderStatus(status)
            val orderTag = OrderStatusTag(orderStatus)
            val tagView = TagView(holder.itemView.context)
            tagView.tag = orderTag
            holder.orderTagList.addView(tagView)
        }

        private fun createTempOrderStatus(status: String): WCOrderStatusModel {
            return WCOrderStatusModel().apply {
                statusKey = status
                label = status
            }
        }
    }

    private class LoadingViewHolder(view: View) : RecyclerView.ViewHolder(view)

    private class SectionHeaderViewHolder(
        @LayoutRes layout: Int,
        parentView: ViewGroup
    ) : RecyclerView.ViewHolder(LayoutInflater.from(parentView.context).inflate(layout, parentView, false)) {
        private val titleView: TextView = itemView.findViewById(R.id.orderListHeader)
        fun onBind(header: SectionHeader) {
            titleView.setText(TimeGroup.valueOf(header.title.name).labelRes)
        }
    }
}

private val OrderListDiffItemCallback = object : DiffUtil.ItemCallback<OrderListItemUIType>() {
    override fun areItemsTheSame(oldItem: OrderListItemUIType, newItem: OrderListItemUIType): Boolean {
        if (oldItem is SectionHeader && newItem is SectionHeader) {
            return oldItem.title == newItem.title
        }
        if (oldItem is LoadingItem && newItem is LoadingItem) {
            return oldItem.remoteId == newItem.remoteId
        }
        if (oldItem is OrderListItemUI && newItem is OrderListItemUI) {
            return oldItem.remoteOrderId == newItem.remoteOrderId
        }
        if (oldItem is LoadingItem && newItem is OrderListItemUI) {
            return oldItem.remoteId == newItem.remoteOrderId
        }
        return false
    }

    /**
     * We can use a basic `==` here because the `equals()` method for these classes have been overridden to
     * properly compare the necessary fields.
     *
     * @see [OrderListItemUIType.equals]
     */
    override fun areContentsTheSame(oldItem: OrderListItemUIType, newItem: OrderListItemUIType) = oldItem == newItem
}
