package defra.pipeline.reports.selenium

import org.apache.commons.io.IOUtils
import org.apache.http.Header
import org.apache.http.HttpEntity
import org.apache.http.HttpException
import org.apache.http.HttpHeaders
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.HttpRequestRetryHandler
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.conn.ssl.TrustSelfSignedStrategy
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicHeader
import org.apache.http.ssl.SSLContextBuilder
import org.apache.log4j.Level
import org.apache.log4j.LogManager

import javax.net.ssl.SSLContext

class JenkinsHttpClient {

    private final HttpClient httpClient

    JenkinsHttpClient() {
        // for creating mock instances
    }

    JenkinsHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient
    }

    JenkinsHttpClient(String jenkinsUser, String jenkinsToken) {
        /* TODO: to enable SSL we need to configure a keystore with Jenkins keychain
           The default JVM keystore doesn't contain the full keychain */
        def jenkinsAuth = "Basic " + Base64.getEncoder().encodeToString("${jenkinsUser}:${jenkinsToken}".getBytes("UTF-8"))
        final SSLContext sslContext = new SSLContextBuilder()
            .loadTrustMaterial(null, new TrustSelfSignedStrategy())
            .build()
        final SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext)
        final Header header = new BasicHeader(HttpHeaders.AUTHORIZATION, jenkinsAuth)
        final RequestConfig requestConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(10000)
            .setConnectTimeout(10000)
            .setSocketTimeout(10000)
            .build()
        final HttpRequestRetryHandler retry = new DefaultHttpRequestRetryHandler(3, true)
        this.httpClient = HttpClients.custom()
            .setSSLSocketFactory(sslConnectionSocketFactory)
            .setDefaultHeaders(Collections.singletonList(header))
            .setDefaultRequestConfig(requestConfig)
            .setRetryHandler(retry)
            .build()
    }

    byte[] readBytes(String url) {
        def count = 0
        while (count < 3) {
            count++
            try {
                return readBytesInternal(url)
            } catch (SocketTimeoutException e) {
                println "Socket timeout connecting to ${url}"
                if (count >= 3) {
                    throw e
                }
            }
        }
    }

    byte[] readBytesInternal(String url) {
        println "Reading from ${url}"
        HttpGet httpGet = new HttpGet(url)
        CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(httpGet)
        final int status = response.getStatusLine().getStatusCode()
        if (status < 200 || status >= 300) {
            response.close()
            throw new JenkinsHttpClientException(status, "HTTP Status: ${status}")
        }
        HttpEntity entity = response.getEntity()
        InputStream is = entity.getContent()
        byte[] bytes = IOUtils.toByteArray(is)
        response.close()
        return bytes
    }

}
