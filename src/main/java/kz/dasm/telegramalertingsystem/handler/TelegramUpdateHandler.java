
package kz.dasm.telegramalertingsystem.handler;

import org.slf4j.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * @author DAsm
 */
@Service
public class TelegramUpdateHandler {
    Logger logger = LoggerFactory.getLogger(TelegramUpdateHandler.class);
    @Scheduled(fixedRate = 3000)
    public void TelegramUpdateHandlerTimer() {
        //TelegramGetter serverGetter = new TelegramGetter();
        //serverGetter.run();
        logger.info("Telegram get update");

    }

}