package ar.edu.unq.eperdemic.services.elastic.api

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase
import org.apache.http.client.methods.HttpUriRequest
import org.springframework.http.HttpMethod
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import java.net.URI

class RequestFactory {
    val restTemplate = RestTemplate()

    private class HttpComponentsClientHttpRequestWithBodyFactory : HttpComponentsClientHttpRequestFactory() {
        override fun createHttpUriRequest(httpMethod: HttpMethod, uri: URI): HttpUriRequest {
            return if (httpMethod == HttpMethod.GET) {
                HttpGetRequestWithEntity(uri)
            } else super.createHttpUriRequest(httpMethod, uri)
        }
    }

    private class HttpGetRequestWithEntity(uri: URI?) : HttpEntityEnclosingRequestBase() {
        override fun getMethod(): String {
            return HttpMethod.GET.name
        }

        init {
            super.setURI(uri)
        }
    }

    init {
        restTemplate.requestFactory = HttpComponentsClientHttpRequestWithBodyFactory()
    }
}