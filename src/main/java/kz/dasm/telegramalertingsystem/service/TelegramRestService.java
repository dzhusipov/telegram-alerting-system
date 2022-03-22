package kz.dasm.telegramalertingsystem.service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import kz.dasm.telegramalertingsystem.api.TelegramApi;

@RestController
public class TelegramRestService {

    @GetMapping("/telegram/{chat_id}/{text}")
    public String sendMessage(@PathVariable long chat_id, @PathVariable String message) {
        TelegramApi telegramApi = new TelegramApi();
        return telegramApi.sendMessage(chat_id, message);
    }
    
}
