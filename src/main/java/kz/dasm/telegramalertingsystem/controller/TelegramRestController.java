package kz.dasm.telegramalertingsystem.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TelegramRestController {
    
    @GetMapping("/employees")
    String test() {
        return "{\"test\":\"ok\"}";
    }
}
