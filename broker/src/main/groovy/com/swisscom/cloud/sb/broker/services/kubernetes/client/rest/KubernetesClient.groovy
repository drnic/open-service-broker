package com.swisscom.cloud.sb.broker.services.kubernetes.client.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.swisscom.cloud.sb.broker.services.kubernetes.config.KubernetesConfig
import com.swisscom.cloud.sb.broker.util.RestTemplateBuilder
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException

@CompileStatic
@Component
@Log4j
class KubernetesClient<RESPONSE> {

    KubernetesConfig kubernetesConfig
    RestTemplateBuilder restTemplateBuilder
    public static final Long BACKOFFDELAY = 10000l
    public static final Double BACKOFFMULTIPLIER = 2.1d

    @Autowired
    KubernetesClient(KubernetesConfig kubernetesConfig, RestTemplateBuilder restTemplateBuilder) {
        this.kubernetesConfig = kubernetesConfig
        this.restTemplateBuilder = restTemplateBuilder
    }

    @Retryable(maxAttempts=3,value=RestClientException.class,backoff = @Backoff(delay=KubernetesClient.BACKOFFDELAY ,multiplier=KubernetesClient.BACKOFFMULTIPLIER))
    ResponseEntity<RESPONSE> exchange(String url, HttpMethod method,
                                      String body, Class<RESPONSE> responseType) {
        //TODO get rid of SSL validation disabling, trust the server side certificate instead
        def restTemplate = restTemplateBuilder.withSSLValidationDisabled().
                withClientSideCertificate(kubernetesConfig.kubernetesClientCertificate, kubernetesConfig.kubernetesClientKey).build()
        log.info(url + " - " + convertYamlToJson(body))
        return restTemplate.exchange(
                "https://" + kubernetesConfig.getKubernetesHost() + ":" + kubernetesConfig.getKubernetesPort() + "/" +
                        url, method, new HttpEntity<String>(convertYamlToJson(body), getJsonHeaders()), responseType)
    }

    private HttpHeaders getJsonHeaders() {
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        return headers
    }

    String convertYamlToJson(String yaml) {
        if (yaml == null || yaml.isEmpty()) {
            return ""
        }
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory())
        Object obj = yamlReader.readValue(yaml, Object.class)
        ObjectMapper jsonWriter = new ObjectMapper()
        return jsonWriter.writeValueAsString(obj)
    }

}
