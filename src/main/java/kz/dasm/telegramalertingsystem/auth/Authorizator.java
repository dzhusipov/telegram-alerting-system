package kz.dasm.telegramalertingsystem.auth;

import java.util.Timer;

import kz.dasm.telegramalertingsystem.email.SendEmail;

public class Authorizator {

    public static Timer timer;


    public void sendEmail(String code, String email_to) {
        SendEmail sendMail = new SendEmail();
        sendMail.sendMail(email_to, "Ваш код доступа в телеграм: " + code + "\n"
                + "Для авторизации введите /code " + code);
    }

}