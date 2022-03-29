package kz.dasm.telegramalertingsystem.service;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateProxy {

    @Value("${proxy.host}")
    String PROXY_SERVER_HOST;

    @Value("${proxy.port}")
    int PROXY_SERVER_PORT;

    @Value("${proxy.user}")
    String PROXY_SERVER_USER;

    @Value("${proxy.pass}")
    String PROXY_SERVER_PASS;

    public RestTemplate getRequestFactory() {

        if (!PROXY_SERVER_HOST.isEmpty()) {
            Proxy proxy = new Proxy(Type.HTTP, new InetSocketAddress(PROXY_SERVER_HOST, PROXY_SERVER_PORT));
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setProxy(proxy);
            return new RestTemplate(requestFactory);
        } else {
            return new RestTemplate();
        }
    }

}
