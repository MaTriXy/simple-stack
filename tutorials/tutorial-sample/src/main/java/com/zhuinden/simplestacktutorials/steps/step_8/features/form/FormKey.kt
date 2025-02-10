package com.zhuinden.simplestacktutorials.steps.step_8.features.form

import androidx.fragment.app.Fragment
import com.zhuinden.simplestack.ServiceBinder
import com.zhuinden.simplestackextensions.servicesktx.add
import com.zhuinden.simplestackextensions.servicesktx.lookup
import com.zhuinden.simplestacktutorials.steps.step_8.core.navigation.FragmentKey
import kotlinx.parcelize.Parcelize

@Parcelize
data object FormKey : FragmentKey() {
    override fun instantiateFragment(): Fragment = FormFragment()

    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            add(
                FormViewModel(
                    resultHandler = lookup(),
                    backstack = backstack,
                )
            )
        }
    }

    operator fun invoke() = this
}