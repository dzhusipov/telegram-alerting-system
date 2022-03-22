
package kz.dasm.telegramalertingsystem.handler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * @author DAsm
 */
@Service
public class TelegramUpdateHandler {
    
    @Scheduled(fixedRate = 3000)
    public void TelegramUpdateHandlerTimer() {
        TelegramGetter serverGetter = new TelegramGetter();
        serverGetter.run();
        //logger.info("Telegram get update");

    }

}