package defra.pipeline.reports.selenium

import org.apache.http.HttpEntity
import org.apache.http.StatusLine
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.junit.Test

import static org.junit.Assert.*

class JenkinsHttpClientTest {

    @Test
    void read_statusCode200_returnsBytes() {
        final byte[] data = "The Data".getBytes("UTF-8")
        final InputStream is = new ByteArrayInputStream(data)

        final HttpEntity entity = [
                getContent: { is }
        ] as HttpEntity

        def closed = false
        final CloseableHttpResponse response = [
                getStatusLine: { return [ getStatusCode: { 200 } ] as StatusLine },
                getEntity: { return entity },
                close: { closed = true }
        ] as CloseableHttpResponse

        HttpGet actualRequest = null
        HttpClient httpClient = [
            execute: { def request -> actualRequest = request; return response }
        ] as HttpClient

        JenkinsHttpClient jenkinsHttpClient = new JenkinsHttpClient(httpClient)
        byte[] result = jenkinsHttpClient.readBytes("http://www.test.com")
        assertEquals(data.length, result.length)
        assertEquals(new String(data, "UTF-8"), new String(result, "UTF-8"))
        assertEquals("http://www.test.com", actualRequest.getURI().toString())
        assertTrue("Response was not closed", closed)
    }

    @Test
    void read_statusCode100_throwsException() {
        def closed = false
        final CloseableHttpResponse response = [
                getStatusLine: { return [ getStatusCode: { 100 } ] as StatusLine },
                close: { closed = true }
        ] as CloseableHttpResponse

        HttpGet actualRequest = null
        HttpClient httpClient = [
                execute: { def request -> actualRequest = request; return response }
        ] as HttpClient

        JenkinsHttpClient jenkinsHttpClient = new JenkinsHttpClient(httpClient)
        try {
            jenkinsHttpClient.readBytes("http://www.test.com")
            fail("Expected to throw IOException")
        } catch (JenkinsHttpClientException e) {
            assertEquals("HTTP Status: 100", e.getMessage())
            assertEquals("http://www.test.com", actualRequest.getURI().toString())
            assertTrue("Response was not closed", closed)
        }
    }

    @Test
    void read_statusCode400_throwsException() {
        def closed = false
        final CloseableHttpResponse response = [
                getStatusLine: { return [ getStatusCode: { 400 } ] as StatusLine },
                close: { closed = true }
        ] as CloseableHttpResponse

        HttpGet actualRequest = null
        HttpClient httpClient = [
                execute: { def request -> actualRequest = request; return response }
        ] as HttpClient

        JenkinsHttpClient jenkinsHttpClient = new JenkinsHttpClient(httpClient)
        try {
            jenkinsHttpClient.readBytes("http://www.test.com")
            fail("Expected to throw IOException")
        } catch (JenkinsHttpClientException e) {
            assertEquals("HTTP Status: 400", e.getMessage())
            assertEquals("http://www.test.com", actualRequest.getURI().toString())
            assertTrue("Response was not closed", closed)
        }
    }

    @Test
    void read_socketTimeout_exceptionThrownAfterThreeRetries() {
        HttpGet actualRequest = null
        def count = 0
        HttpClient httpClient = [
                execute: { def request ->
                    actualRequest = request
                    count++
                    throw new SocketTimeoutException("msg")
                }
        ] as HttpClient

        JenkinsHttpClient jenkinsHttpClient = new JenkinsHttpClient(httpClient)
        try {
            jenkinsHttpClient.readBytes("http://www.test.com")
            fail("Expected to throw SocketTimeoutException")
        } catch (SocketTimeoutException e) {
            assertEquals("http://www.test.com", actualRequest.getURI().toString())
            assertEquals(3, count)
        }
    }


}
