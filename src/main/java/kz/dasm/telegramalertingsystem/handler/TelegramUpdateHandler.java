
package kz.dasm.telegramalertingsystem.handler;

import org.springframework.scheduling.annotation.Scheduled;

/**
 *
 * @author DAsm
 */

public class TelegramUpdateHandler {

    @Scheduled(fixedRate = 3000)
    public void TelegramUpdateHandlerTimer() {
        TelegramGetter serverGetter = new TelegramGetter();
        serverGetter.start();
    }
    
    public void EmailUpdateHandlerTimer() {
        // TODO need to fix
        /*ReadEmail readEmail = new ReadEmail();
        readEmail.readLetters();
        */
    }
}