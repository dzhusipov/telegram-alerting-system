package kz.dasm.telegramalertingsystem.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import kz.dasm.telegramalertingsystem.api.TelegramApi;
import kz.dasm.telegramalertingsystem.db.DataBase;
import kz.dasm.telegramalertingsystem.models.Response;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.alibaba.fastjson.JSON;

@RestController
public class TelegramRestService {

    private static Logger log = Logger.getLogger(TelegramApi.class.getName());
    
    @Value("${proxy.host}")
    String PROXY_SERVER_HOST;

    @Value("${proxy.port}")
    int PROXY_SERVER_PORT;

    @Value("${proxy.user}")
    String PROXY_SERVER_USER;

    @Value("${proxy.pass}")
    String PROXY_SERVER_PASS;

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

    @GetMapping("/telegram/send-message-to-chat/{chat_id}/{text}")
    public String sendMessageToChat(@PathVariable long chat_id, @PathVariable String message) {
        TelegramApi telegramApi = new TelegramApi(getRequestFactory());
        return telegramApi.sendMessage(chat_id, message);
    }

    @GetMapping("/telegram/send-message-to-event/{event_name}/{event_message}")
    public String sendMessageToEvent(@PathVariable String event_name, @PathVariable String event_message) {
        TelegramApi telegramApi = new TelegramApi(getRequestFactory());        

        log.info("event_name: " + event_name);
        log.info("event_message: " + event_message);
        List<String> chat_id = getChatId(event_name);
        Response response_from_telegram;
        String response_from_telegram_string = "";
        String result = "OK";
        Iterator<String> chat_idIterator = chat_id.iterator();
        DataBase db = new DataBase();
        String chat_id_from_list;
        try {
            while (chat_idIterator.hasNext()) {
                chat_id_from_list = chat_idIterator.next();
                if (chat_id_from_list.length() != 0) {

                    response_from_telegram_string = telegramApi.sendMessage(Long.parseLong(chat_id_from_list), event_message, "");

                    if (response_from_telegram_string.contains("\"ok\":false")) {
                        if (response_from_telegram_string.contains("\"description\":\"Bad Request: group chat was upgraded to a supergroup chat\"")) {
                            response_from_telegram = JSON.parseObject(response_from_telegram_string, Response.class);
                            db.updateChatID(Long.valueOf(chat_id_from_list), response_from_telegram.getParameters().getMigrate_to_chat_id(), true);
                        } else {
                            log.info("Error response: " + response_from_telegram_string);
                        }
                    }

                    /**
                     * 1) Проверяем ок не ок. 2) Если ок - возвращяем результат
                     * 3) Нет смотрим по какой ошибке. Если мигранули - меняем
                     * скрипт
                     */
                    result = "OK";

                } else {
                    result = "Error";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.closeConnection();
        }
        return result;
    }


    /**
     * int getChatId(String event). Получаем чат айди для того, чтобы понимать к
     * какой группе отправлять сообщения
     */
    private List<String> getChatId(String event) {
        DataBase db = new DataBase();
        List<String> chat_id = db.getEventsChatID(event, false);
        db.closeConnection();
        return chat_id;
    }
    
    
}
