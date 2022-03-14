package kz.dasm.telegramalertingsystem.email;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class SendEmail {
    private static final String EMAIL_FROM = "telegram-bot@YOUR_COMPANY_DOMAIN.COM";
    private static final String EMAIL_HOST = "SMTP_SERVER";
    private static final String EMAIL_SUBJECT = "Код доступа для telegram оповещений.";

    /**
     * Метод отправки почтового сообщения пользователю с кодом доступа в телеграм. 
     * @param email_to  кому отправлем
     * @param text      текст отправки
     */
    public void sendMail(String email_to, String text) {
        
        //this.disableProxy();
        
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", EMAIL_HOST);
        Session session = Session.getDefaultInstance(properties);
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(email_to));
            message.setSubject(EMAIL_SUBJECT);
            message.setText(text);
            Transport.send(message);
        } catch (MessagingException mex) {
            System.out.println("DAsm: Error in SendMail: " + System.getProperty("http.proxyHost") + System.getProperty("https.proxyHost"));
            Logger.getLogger(SendEmail.class.getName()).log(Level.SEVERE, null, mex);
        }
    }
    
}
