package com.zhuinden.simplestackdemomultistack.features.main.chromecast

import com.zhuinden.simplestackdemomultistack.R
import com.zhuinden.simplestackdemomultistack.application.MainActivity
import com.zhuinden.simplestackdemomultistack.core.navigation.MultistackViewKey
import kotlinx.parcelize.Parcelize

@Parcelize
data object ChromeCastKey : MultistackViewKey() {
    override fun layout(): Int = R.layout.chromecast_view
    override fun stackIdentifier(): String = MainActivity.StackType.CHROMECAST.name
}
