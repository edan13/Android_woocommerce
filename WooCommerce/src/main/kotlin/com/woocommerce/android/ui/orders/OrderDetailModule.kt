package com.woocommerce.android.ui.orders

import com.woocommerce.android.ui.orders.OrderDetailContract.Presenter
import dagger.Binds
import dagger.Module

@Module
internal abstract class OrderDetailModule {
    @Binds
    abstract fun provideOrderDetailPresenter(orderDetailPresenter: OrderDetailPresenter): Presenter
}






