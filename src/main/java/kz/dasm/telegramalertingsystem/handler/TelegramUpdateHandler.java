
package kz.dasm.telegramalertingsystem.handler;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author DAsm
 */
@Service
public class TelegramUpdateHandler {
    
    @Value("${proxy.host}")
    String PROXY_SERVER_HOST;

    @Value("${proxy.port}")
    int PROXY_SERVER_PORT;

    private static java.util.logging.Logger log = java.util.logging.Logger.getLogger(TelegramUpdateHandler.class.getName());

    @Scheduled(fixedRate = 3000)
    public void TelegramUpdateHandlerTimer() {
        log.info("Telegram get update started ...");
        TelegramGetter serverGetter = new TelegramGetter();
        serverGetter.run(getRequestFactory());
    }

    private RestTemplate getRequestFactory() {

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