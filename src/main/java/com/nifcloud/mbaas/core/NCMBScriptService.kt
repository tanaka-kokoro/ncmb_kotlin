/*
 * Copyright 2017-2022 FUJITSU CLOUD TECHNOLOGIES LIMITED All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nifcloud.mbaas.core

import android.os.AsyncTask
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

/**
 * Service class for script api
 */
internal class NCMBScriptService : NCMBService() {

    /**
     * execute script to NIFCLOUD mobile backend
     *
     * @param scriptName script name
     * @param method     HTTP method
     * @param header     header data
     * @param body       content data
     * @param query      query params
     * @param baseUrl    script base url
     * @return Result to script
     * @throws NCMBException exception from NIFCLOUD mobile backend
     */
    @Throws(NCMBException::class)
    fun executeScript(
        scriptName: String,
        method: String,
        scriptHeader: HashMap<String, String>,
        scriptBody: JSONObject,
        scriptQuery: JSONObject
    ) : ByteArray? {
        val reqParams = executeScriptParams(
            scriptName,
            method,
            scriptHeader,
            scriptBody,
            scriptQuery,
            null,
            null
        )
        val response = sendRequest(reqParams)
        var responseScript : ByteArray? = null
        when (response) {
            is NCMBResponse.Success -> {
                responseScript = response.data as ByteArray
            }
            is NCMBResponse.Failure -> {
                throw response.resException
            }
        }
        return responseScript
    }

    /**
     * execute script to NIFCLOUD mobile backend in background thread
     *
     * @param scriptName script name
     * @param method     HTTP method
     * @param header     header
     * @param body       content data
     * @param query      query params
     * @param baseUrl    script base url
     * @param callback   callback for after script execute
     */
    fun executeScriptInBackground(
        scriptName: String,
        method: String,
        scriptHeader: HashMap<String, String>,
        scriptBody: JSONObject,
        scriptQuery: JSONObject,
        executeCallback: NCMBCallback
    ) {
        val executeHandler = NCMBHandler { scriptcallback, response ->
            when (response) {
                is NCMBResponse.Success -> {
                    executeCallback.done(null, responseScript = response.data as ByteArray)
                }
                is NCMBResponse.Failure -> {
                    executeCallback.done(response.resException)
                }
            }
        }
        val reqParams : RequestParams = executeScriptParams(
            scriptName,
            method,
            scriptHeader,
            scriptBody,
            scriptQuery,
            executeCallback,
            executeHandler)
        sendRequestAsync(reqParams, executeCallback, executeHandler)

    }

    /*
    * @param
    * @param executeCallback callback when process finished
    * @param executeHandler sdk after-connection tasks
    * @return parameters in object
    */
    protected fun executeScriptParams(scriptName: String,
                                      method: String,
                                      scriptHeader: HashMap<String, String>,
                                      scriptBody: JSONObject,
                                      scriptQuery: JSONObject,
                                      executeCallback: NCMBCallback?,
                                      executeHandler: NCMBHandler?): RequestParams {
        val url = NCMB.getApiBaseUrl(isScript = true) + mServicePath + "/" + scriptName
        val method = method
        val contentType = NCMBRequest.HEADER_CONTENT_TYPE_JSON
        return RequestParams(url = url,
            method = method, scriptHeader = scriptHeader, params = scriptBody, contentType = contentType, query = scriptQuery, callback = executeCallback, handler = executeHandler)
    }

    companion object {
        /**
         * execute api path
         */
        const val SERVICE_PATH = "script"

    }

    /**
     * Constructor
     *
     * @param context Service context
     */
    init {
        mServicePath = SERVICE_PATH
    }
}