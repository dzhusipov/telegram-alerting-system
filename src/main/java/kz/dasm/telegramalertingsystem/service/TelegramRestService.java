package kz.dasm.telegramalertingsystem.service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TelegramRestService {

    @GetMapping("/telegram/{message}")
    public String sendMessage(@PathVariable String message) {
        return "Hello, " + message;
    }
    
}
