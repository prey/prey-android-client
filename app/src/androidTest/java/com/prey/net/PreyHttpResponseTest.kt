package com.prey.net

import io.mockk.every
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection
import java.io.ByteArrayInputStream
import java.io.IOException
import java.net.URL

class PreyHttpResponseTest {

    private lateinit var mockConnection: HttpURLConnection

    @Before
    fun setup() {
        val url = URL("http://someurl.com/")
        mockConnection = url.openConnection() as HttpURLConnection
    }

    @Test
    fun test_constructor_with_HttpURLConnection_success_response() {
        // Arrange
        val responseString = "Success response"
        val inputStream = ByteArrayInputStream(responseString.toByteArray())
        every { mockConnection.responseCode } returns HttpURLConnection.HTTP_OK
        every { mockConnection.inputStream } returns inputStream
        // Act
        val response = PreyHttpResponse(mockConnection)
        // Assert
        assertEquals(HttpURLConnection.HTTP_OK, response.getStatusCode())
        assertTrue(response.getResponseAsString()?.contains(responseString) ?: false)
    }

    @Test
    fun test_constructor_with_status_code_and_response_string() {
        // Arrange
        val statusCode = 200
        val responseString = "Test response"
        // Act
        val response = PreyHttpResponse(statusCode, responseString)
        // Assert
        assertEquals(statusCode, response.getStatusCode())
        assertEquals(responseString, response.getResponseAsString())
    }

    @Test
    fun test_constructor_with_header_fields() {
        // Arrange
        val statusCode = 200
        val responseString = "Test response"
        val headers = mapOf(
            "Content-Type" to listOf("application/json"),
            "Authorization" to listOf("Bearer token")
        )
        // Act
        val response = PreyHttpResponse(statusCode, responseString, headers)
        // Assert
        assertEquals(statusCode, response.getStatusCode())
        assertEquals(responseString, response.getResponseAsString())
        assertEquals(headers, response.getMapHeaderFields())
    }

    @Test
    fun test_error_response() {
        // Arrange
        val errorString = "Error response"
        val errorStream = ByteArrayInputStream(errorString.toByteArray())
        every { mockConnection.responseCode } returns HttpURLConnection.HTTP_BAD_REQUEST
        every { mockConnection.errorStream } returns errorStream
        // Act
        val response = PreyHttpResponse(mockConnection)
        // Assert
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatusCode())
        assertTrue(response.getResponseAsString()?.contains(errorString) ?: false)
    }

    @Test
    fun test_toString_method() {
        // Arrange
        val statusCode = 200
        val responseString = "Test response"
        val response = PreyHttpResponse(statusCode, responseString)
        // Act
        val result = response.toString()
        // Assert
        assertEquals("$statusCode $responseString", result)
    }

    @Test
    fun test_empty_response_on_exception() {
        // Arrange
        every { mockConnection.responseCode } throws IOException("Network error")
        // Act
        val response = PreyHttpResponse(mockConnection)
        // Assert
        assertEquals("", response.getResponseAsString())
    }

}