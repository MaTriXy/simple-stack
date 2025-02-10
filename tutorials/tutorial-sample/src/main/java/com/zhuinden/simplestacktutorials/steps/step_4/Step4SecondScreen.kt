package com.zhuinden.simplestacktutorials.steps.step_4

import com.zhuinden.simplestacktutorials.R
import kotlinx.parcelize.Parcelize

@Parcelize
data object Step4SecondScreen : Step4Screen() {  // generate equals/hashCode/toString
    operator fun invoke() = this

    override fun layout(): Int = R.layout.step4_second_view
}