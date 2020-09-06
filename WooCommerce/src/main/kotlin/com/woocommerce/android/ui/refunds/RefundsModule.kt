package com.woocommerce.android.ui.refunds

import com.woocommerce.android.di.FragmentScope
import com.woocommerce.android.ui.refunds.RefundsModule.IssueRefundFragmentModule
import com.woocommerce.android.ui.refunds.RefundsModule.RefundAmountDialogModule
import com.woocommerce.android.ui.refunds.RefundsModule.RefundByAmountFragmentModule
import com.woocommerce.android.ui.refunds.RefundsModule.RefundByItemsFragmentModule
import com.woocommerce.android.ui.refunds.RefundsModule.RefundConfirmationDialogModule
import com.woocommerce.android.ui.refunds.RefundsModule.RefundDetailFragmentModule
import com.woocommerce.android.ui.refunds.RefundsModule.RefundItemsPickerDialogModule
import com.woocommerce.android.ui.refunds.RefundsModule.RefundSummaryFragmentModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module(includes = [
    IssueRefundFragmentModule::class,
    RefundByAmountFragmentModule::class,
    RefundByItemsFragmentModule::class,
    RefundSummaryFragmentModule::class,
    RefundDetailFragmentModule::class,
    RefundItemsPickerDialogModule::class,
    RefundConfirmationDialogModule::class,
    RefundAmountDialogModule::class
])
object RefundsModule {
    @Module
    abstract class RefundSummaryFragmentModule {
        @FragmentScope
        @ContributesAndroidInjector(modules = [RefundSummaryModule::class])
        abstract fun refundSummaryFragment(): RefundSummaryFragment
    }

    @Module
    abstract class RefundDetailFragmentModule {
        @FragmentScope
        @ContributesAndroidInjector(modules = [RefundDetailModule::class])
        abstract fun refundDetailFragment(): RefundDetailFragment
    }

    @Module
    abstract class RefundByItemsFragmentModule {
        @FragmentScope
        @ContributesAndroidInjector(modules = [RefundByItemsModule::class])
        abstract fun refundByItemsFragment(): RefundByItemsFragment
    }

    @Module
    abstract class RefundByAmountFragmentModule {
        @FragmentScope
        @ContributesAndroidInjector(modules = [RefundByAmountModule::class])
        abstract fun refundByAmountFragment(): RefundByAmountFragment
    }

    @Module
    abstract class IssueRefundFragmentModule {
        @FragmentScope
        @ContributesAndroidInjector(modules = [IssueRefundModule::class])
        abstract fun issueRefundFragment(): IssueRefundFragment
    }

    @Module
    abstract class RefundItemsPickerDialogModule {
        @FragmentScope
        @ContributesAndroidInjector(modules = [RefundItemsPickerModule::class])
        abstract fun itemsPickerDialog(): RefundItemsPickerDialog
    }

    @Module
    abstract class RefundConfirmationDialogModule {
        @FragmentScope
        @ContributesAndroidInjector(modules = [RefundConfirmationModule::class])
        abstract fun refundConfirmationDialog(): RefundConfirmationDialog
    }

    @Module
    abstract class RefundAmountDialogModule {
        @FragmentScope
        @ContributesAndroidInjector(modules = [RefundAmountModule::class])
        abstract fun refundAmountDialog(): RefundAmountDialog
    }
}
