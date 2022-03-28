
package kz.dasm.telegramalertingsystem.handler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * @author DAsm
 */
@Service
public class TelegramUpdateHandler {

    private static java.util.logging.Logger log = java.util.logging.Logger.getLogger(TelegramUpdateHandler.class.getName());

    @Scheduled(fixedRate = 3000)
    public void TelegramUpdateHandlerTimer() {
        TelegramGetter serverGetter = new TelegramGetter();
        serverGetter.run();
        //log.info("Telegram get update");
    }

}