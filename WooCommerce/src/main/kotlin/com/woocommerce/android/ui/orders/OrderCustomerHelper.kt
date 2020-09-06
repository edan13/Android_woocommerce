package com.woocommerce.android.ui.orders

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.woocommerce.android.R
import com.woocommerce.android.analytics.AnalyticsTracker
import com.woocommerce.android.analytics.AnalyticsTracker.Stat
import org.wordpress.android.fluxc.model.WCOrderModel
import org.wordpress.android.util.ToastUtils

object OrderCustomerHelper {
    enum class Action {
        EMAIL,
        CALL,
        SMS
    }

    fun dialPhone(context: Context, order: WCOrderModel, phone: String) {
        AnalyticsTracker.track(
                Stat.ORDER_CONTACT_ACTION, mapOf(
                AnalyticsTracker.KEY_ID to order.remoteOrderId,
                AnalyticsTracker.KEY_STATUS to order.status,
                AnalyticsTracker.KEY_TYPE to Action.CALL.name.toLowerCase()))

        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phone")
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            AnalyticsTracker.track(
                    Stat.ORDER_CONTACT_ACTION_FAILED,
                    this.javaClass.simpleName,
                    e.javaClass.simpleName, "No phone app was found")

            ToastUtils.showToast(context, R.string.error_no_phone_app)
        }
    }

    fun createEmail(context: Context, order: WCOrderModel, emailAddr: String) {
        AnalyticsTracker.track(
                Stat.ORDER_CONTACT_ACTION, mapOf(
                AnalyticsTracker.KEY_ID to order.remoteOrderId,
                AnalyticsTracker.KEY_STATUS to order.status,
                AnalyticsTracker.KEY_TYPE to Action.EMAIL.name.toLowerCase()))

        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:$emailAddr") // only email apps should handle this
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            AnalyticsTracker.track(
                    Stat.ORDER_CONTACT_ACTION_FAILED,
                    this.javaClass.simpleName,
                    e.javaClass.simpleName, "No e-mail app was found")

            ToastUtils.showToast(context, R.string.error_no_email_app)
        }
    }

    fun sendSms(context: Context, order: WCOrderModel, phone: String) {
        AnalyticsTracker.track(
                Stat.ORDER_CONTACT_ACTION, mapOf(
                AnalyticsTracker.KEY_ID to order.remoteOrderId,
                AnalyticsTracker.KEY_STATUS to order.status,
                AnalyticsTracker.KEY_TYPE to Action.SMS.name.toLowerCase()))

        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("smsto:$phone")
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            AnalyticsTracker.track(
                    Stat.ORDER_CONTACT_ACTION_FAILED,
                    this.javaClass.simpleName,
                    e.javaClass.simpleName, "No SMS app was found")

            ToastUtils.showToast(context, R.string.error_no_sms_app)
        }
    }
}
