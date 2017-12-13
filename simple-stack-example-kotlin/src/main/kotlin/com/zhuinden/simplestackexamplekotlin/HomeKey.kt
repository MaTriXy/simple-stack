package com.zhuinden.simplestackexamplekotlin

import android.os.Parcelable
import paperparcel.PaperParcel

/**
 * Created by Owner on 2017.11.13.
 */
@PaperParcel
object HomeKey : BaseKey() {
    override fun createFragment() = HomeFragment()

    @JvmField val CREATOR: Parcelable.Creator<HomeKey> = PaperParcelHomeKey.CREATOR

    override fun toString(): String = this::class.java.name // you NEED to implement this in an `object` because of Kotlin!
}
