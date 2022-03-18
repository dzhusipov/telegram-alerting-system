package kz.dasm.telegramalertingsystem.controller;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TelegramRestController {

    @Bean
    @GetMapping("/")
    String test() {
        return "{\"test\":\"ok\"}";
    }
}
