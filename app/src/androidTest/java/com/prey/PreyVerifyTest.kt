package com.prey

import org.junit.Test
import org.junit.Assert.*

class PreyVerifyTest {

    @Test
    fun test_status_code_get_and_set() {
        val verify = PreyVerify()
        verify.setStatusCode(200)
        assertEquals(200, verify.getStatusCode())
    }

    @Test
    fun test_status_description_get_and_set() {
        val verify = PreyVerify()
        verify.setStatusDescription("Success")
        assertEquals("Success", verify.getStatusDescription())
    }

    @Test
    fun test_initial_values() {
        val verify = PreyVerify()
        assertEquals(-1, verify.getStatusCode())
        assertThrows(NullPointerException::class.java) {
            verify.getStatusDescription()
        }
    }

}