/*
 * Copyright 2017 Gabor Varadi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhuinden.simplestackexamplemvvm.core.database


import androidx.lifecycle.MutableLiveData
import com.zhuinden.simplestackexamplemvvm.core.database.DatabaseManager.QueryDefinition
import com.zhuinden.simplestackexamplemvvm.core.scheduler.Scheduler
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by Zhuinden on 2017.07.26..
 */
class LiveResults<T : Any>(
    private val backgroundScheduler: Scheduler,
    private val databaseManager: DatabaseManager,
    val table: DatabaseManager.Table,
    private val mapper: DatabaseManager.Mapper<T>,
    private val queryDefinition: QueryDefinition,
) : MutableLiveData<List<T>>() {

    private val isInvalid = AtomicBoolean(true) // from ComputableLiveData
    private val isComputing = AtomicBoolean(false) // from ComputableLiveData
    fun refresh() {
        isInvalid.set(true)
        backgroundScheduler.execute {

            // directly taken from ComputableLiveData to eliminate possible race conditions.
            // imagine scenario that a write happens that modifies 1 item and takes 500 ms to query.
            // another write happens 200 ms later that deletes all items. Now querying takes 20 ms.
            // the previous write would overwrite the deletion that occurs later.
            // using the code of ComputableLiveData, we can eliminate that scenario.
            // the second write would cause "isInvalid" to be set,
            // so the query will be run again on the first thread's execution until it is done.
            // Computation will also only happen on the first initiating thread,
            // other threads will exit immediately instead.
            var didCompute: Boolean
            do {
                didCompute = false
                // compute can happen only in 1 thread but no reason to lock others.
                if (isComputing.compareAndSet(false, true)) {
                    // as long as it is invalid, keep computing.
                    try {
                        var value: List<T> = emptyList()
                        while (isInvalid.compareAndSet(true, false)) {
                            didCompute = true
                            value = databaseManager.findAll(table, mapper, queryDefinition)
                        }
                        if (didCompute) {
                            postValue(value)
                        }
                    } finally {
                        // release compute lock
                        isComputing.set(false)
                    }
                }
                // THESE COMMENTS ARE FROM ComputableLiveData.
                // check invalid after releasing compute lock to avoid the following scenario.
                // - Thread A runs compute()
                // - Thread A checks invalid, it is false
                // - Main thread sets invalid to true
                // - Thread B runs, fails to acquire compute lock and skips
                // - Thread A releases compute lock
                // We've left invalid in set state. The check below recovers.
            } while (didCompute && isInvalid.get())
        }
    }

    override fun onActive() {
        refresh()
    }

    init {
        databaseManager.addLiveResults(this)
    }
}