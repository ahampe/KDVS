/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Adapted from:
 * https://gist.github.com/yaraki/4cbeb3be276117c07d22602ab1382b04
 */

package fho.kdvs.global.extensions

import android.content.Context
import android.os.Handler
import android.os.Message
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

/**
 * Extension method with handler class to grant timeout functionality to LiveData.
 * */
private class LiveDataTimeoutHandler(
    private val context: Context,
    private val toast: String?,
    private val liveData: LiveData<*>
) : Handler() {
    companion object {
        const val LIVE_DATA_TIMEOUT = 1
    }

    // Clear live data and make toast
    override fun handleMessage(msg: Message?) {
        if (msg?.what == LIVE_DATA_TIMEOUT) {
            if (liveData.value == null && !toast.isNullOrBlank()) {
                Toast.makeText(context, toast, Toast.LENGTH_LONG)
                    .show()
            }
        }
    }
}

/**
 * Queue a handler message for the timeout duration. If we observe a value, remove message. Otherwise,
 * handle message.
 */
fun <T> LiveData<T>.withMessageOnTimeout(timeout: Long, context: Context, toast: String?): LiveData<T> {
    val result = MediatorLiveData<T>()
    val handler = LiveDataTimeoutHandler(context, toast, this)

    handler.sendMessageDelayed(
        Message.obtain(handler, LiveDataTimeoutHandler.LIVE_DATA_TIMEOUT),
        timeout)

    result.addSource(this) {
        handler.removeMessages(LiveDataTimeoutHandler.LIVE_DATA_TIMEOUT)
    }

    return this
}