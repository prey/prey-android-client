package com.prey.activities.js

import android.content.Context
import androidx.test.core.app.ApplicationProvider

import com.prey.PreyAccountData
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.net.PreyWebServices
import com.prey.net.TestWebServices

import org.json.JSONObject
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class WebAppInterfaceTest {

    @Mock
    private lateinit var context: Context
    lateinit var preyWebServices: PreyWebServices

    @Mock
    private lateinit var webAppInterface: WebAppInterface
    lateinit var preyConfig: PreyConfig

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

    }

    @Before
    fun onBefore() {


        context = ApplicationProvider.getApplicationContext()

        //   webAppInterface=WebAppInterface()
        //  preyConfig=PreyConfig.getInstance(context)
    }

    @Test
    fun testSignupSuccess() {
        // Arrange
        val name = "Test Name"
        val email = "test@example.com"
        val password1 = "password1"
        val password2 = "password2"
        val policyRuleAge = "policy_rule_age"
        val policyRulePrivacyTerms = "policy_rule_privacy_terms"
        val offers = "offers"
        val apikey = "A123"
        val deviceId = "d456"
        val accountData = PreyAccountData()
        accountData.setEmail(email)
        accountData.setName(name)
        accountData.setPassword(password1)
        accountData.setApiKey(apikey)
        accountData.setDeviceId(deviceId)
        accountData.setMissing(false)
        accountData.setPreyVersion("2.1")
        accountData.setRefererId("1")
        val testWebServices = TestWebServices()
        testWebServices.setAccountData(accountData)
        val status = JSONObject()
        testWebServices.setStatus(status)
        PreyConfig.getInstance(context).setWebServices(testWebServices)
        webAppInterface.setContext(context)
        // Act
        val result = webAppInterface.signup(
            name,
            email,
            password1,
            password2,
            policyRuleAge,
            policyRulePrivacyTerms,
            offers
        )
        PreyLogger.i("result:$result")
        // Assert
        Assert.assertEquals(result, "")
        Assert.assertEquals(PreyConfig.getInstance(context).getApiKey(), apikey)
        Assert.assertEquals(PreyConfig.getInstance(context).getDeviceId(), deviceId)
    }

    @Test
    fun testSignupFailure() {
        // Arrange
        val name = "Test Name"
        val email = "test@example.com"
        val password1 = "password1"
        val password2 = "password2"
        val policyRuleAge = "policy_rule_age"
        val policyRulePrivacyTerms = "policy_rule_privacy_terms"
        val offers = "offers"
        val apikey = "A123"
        val deviceId = "d456"
        val accountData = PreyAccountData()
        accountData.setEmail(email)
        accountData.setName(name)
        accountData.setPassword(password1)
        accountData.setApiKey(apikey)
        accountData.setDeviceId(deviceId)
        accountData.setMissing(false)
        accountData.setPreyVersion("2.1")
        accountData.setRefererId("1")
        val testWebServices = TestWebServices()
        testWebServices.setAccountData(accountData)
        val status = JSONObject()
        testWebServices.setStatus(status)
        val errorText = "Test exception"
        val exception = Exception(errorText)
        testWebServices.setErrorException(exception)
        PreyConfig.getInstance(context).setWebServices(testWebServices)
        webAppInterface.setContext(context)
        // Act
        val result = webAppInterface.signup(
            name,
            email,
            password1,
            password2,
            policyRuleAge,
            policyRulePrivacyTerms,
            offers
        )
        PreyLogger.i("result:$result")
        // Assert
        Assert.assertEquals(result, errorText)
    }

}