package com.prey

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PreyAccountDataTest {
    private lateinit var preyAccountData: PreyAccountData

    @Before
    fun setUp() {
        preyAccountData = PreyAccountData()
    }

    @Test
    fun testApiKey() {
        val testApiKey = "test-api-key"
        preyAccountData.setApiKey(testApiKey)
        assertEquals(testApiKey, preyAccountData.getApiKey())
    }

    @Test
    fun testDeviceId() {
        val testDeviceId = "test-device-id"
        preyAccountData.setDeviceId(testDeviceId)
        assertEquals(testDeviceId, preyAccountData.getDeviceId())
    }

    @Test
    fun testPassword() {
        val testPassword = "test-password"
        preyAccountData.setPassword(testPassword)
        assertEquals(testPassword, preyAccountData.getPassword())
    }

    @Test
    fun testEmail() {
        val testEmail = "test@example.com"
        preyAccountData.setEmail(testEmail)
        assertEquals(testEmail, preyAccountData.getEmail())
    }

    @Test
    fun testName() {
        val testName = "Test Name"
        preyAccountData.setName(testName)
        assertEquals(testName, preyAccountData.getName())
    }

    @Test
    fun testRefererId() {
        val testRefererId = "test-referer-id"
        preyAccountData.setRefererId(testRefererId)
        assertEquals(testRefererId, preyAccountData.getRefererId())
    }

    @Test
    fun testPreyVersion() {
        val testVersion = "1.0.0"
        preyAccountData.setPreyVersion(testVersion)
        assertEquals(testVersion, preyAccountData.getPreyVersion())
    }

    @Test
    fun testIsMissing() {
        assertFalse(preyAccountData.isMissing())
        preyAccountData.setMissing(true)
        assertTrue(preyAccountData.isMissing())
    }

}