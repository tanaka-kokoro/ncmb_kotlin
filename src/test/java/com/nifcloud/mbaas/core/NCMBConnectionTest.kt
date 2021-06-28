package com.nifcloud.mbaas.core

import org.json.JSONObject
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import okhttp3.mockwebserver.MockWebServer
import org.junit.*
import org.junit.rules.TestRule
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * 主に通信を行うNCMBConnectionテストクラス
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = intArrayOf(26), manifest = Config.NONE)
class NCMBConnectionTest {

    private var mServer: MockWebServer = MockWebServer()

    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()
    @Before
    fun setup() {
        var ncmbDispatcher = NCMBDispatcher()
        mServer.dispatcher = ncmbDispatcher
        mServer.start()
        NCMB.initialize(
            RuntimeEnvironment.application.getApplicationContext(),
            "appKey",
            "cliKey",
            mServer.url("/").toString(),
            "2013-09-01")
    }

    /**
     * - 内容：POST通信が成功することを確認する
     * - 結果：responseDataが返却されること
     */


    @Test
    fun ncmbConnectionPostObjectInBackgroundValidCase() {
        val url: String = mServer.url("/2013-09-01/classes/TestClass").toString()
        val json = JSONObject()
        json.put("key", "value")
        val tmpRequest = NCMBRequest(url,
            "POST",
            json,
            "application/json",
            JSONObject(),
            null,
            "appKey",
            "cliKey",
            "2020-03-13T01:27:35.569Z"
        )
        val tmpConnection = NCMBConnection(tmpRequest)

        val callback = NCMBCallback { e, res ->
            if (e != null) {
                //保存失敗
                print("Failed" + e.message)
                Assert.assertNotNull(e)
            } else {
                //保存成功
                print("Success")
                when(res) {
                    is NCMBResponse.Success -> {
                        print(res.data)
                    }
                }
                Assert.assertNull(e)
            }
        }
        val handler = NCMBHandler { callback, res ->
            //Handler Action is set here
            when(res) {
                is NCMBResponse.Failure -> {
                    callback!!.done(res.resException)
                }
            }
        }
        tmpConnection.sendRequestAsynchronously(callback,handler)
    }

}
