package com.muhaimen.arenax.volley

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class VolleySingleton private constructor(context: Context) {
    companion object {
        @Volatile
        private var instance: VolleySingleton? = null

        fun getInstance(context: Context): VolleySingleton {
            return instance ?: synchronized(this) {
                instance ?: VolleySingleton(context).also { instance = it }
            }
        }
    }

    private val requestQueue: RequestQueue = Volley.newRequestQueue(context.applicationContext)

    fun <T> addToRequestQueue(req: Request<T>) {
        requestQueue.add(req)
    }
}
