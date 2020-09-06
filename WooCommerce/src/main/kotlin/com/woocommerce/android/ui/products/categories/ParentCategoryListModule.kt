package com.woocommerce.android.ui.products.categories

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import androidx.savedstate.SavedStateRegistryOwner
import com.woocommerce.android.R
import com.woocommerce.android.di.ViewModelAssistedFactory
import com.woocommerce.android.viewmodel.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
abstract class ParentCategoryListModule {
    @Module
    companion object {
        @JvmStatic
        @Provides
        fun provideDefaultArgs(fragment: ParentCategoryListFragment): Bundle? {
            return fragment.arguments
        }

        @JvmStatic
        @Provides
        fun provideSavedStateRegistryOwner(fragment: ParentCategoryListFragment): SavedStateRegistryOwner {
            return fragment.findNavController().getBackStackEntry(R.id.nav_graph_add_product_category)
        }
    }

    @Binds
    @IntoMap
    @ViewModelKey(AddProductCategoryViewModel::class)
    abstract fun bindFactory(factory: AddProductCategoryViewModel.Factory): ViewModelAssistedFactory<out ViewModel>
}
