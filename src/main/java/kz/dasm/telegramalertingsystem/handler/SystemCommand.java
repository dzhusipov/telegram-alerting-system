package kz.dasm.telegramalertingsystem.handler;

import java.io.IOException;

import kz.dasm.telegramalertingsystem.api.TelegramApi;
import kz.dasm.telegramalertingsystem.auth.Authorizator;
import kz.dasm.telegramalertingsystem.db.DataBase;
import kz.dasm.telegramalertingsystem.models.Response;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class SystemCommand {
    //Logger
    private static Logger log = Logger.getLogger(SystemCommand.class.getName());

    private int getRandCode() {
        int randomNum = (ThreadLocalRandom.current().nextInt(3, 8 + 1)) * 1000;
        randomNum = randomNum + ((ThreadLocalRandom.current().nextInt(3, 8 + 1)) * 100);
        randomNum = randomNum + ((ThreadLocalRandom.current().nextInt(3, 8 + 1)) * 10);
        randomNum = randomNum + (ThreadLocalRandom.current().nextInt(3, 8 + 1));
        return randomNum;
    }

    private void send_email(String email_to, long chat_id) {
        int code = getRandCode();
        Authorizator auth = new Authorizator();

        auth.sendEmail(String.valueOf(code), email_to);
        DataBase db = new DataBase();
        db.createNewAuthCode(chat_id, code);
    }

    public static boolean isValidEmailAddress(String email) {
        boolean result = true;
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }

    private boolean check_email(String email) {
        log.info(email);
        if (!isValidEmailAddress(email)) {
            return false;
        }

        String[] full_email = email.split("@");
        String domain = full_email[1];

        if (!domain.equalsIgnoreCase("YOUR_COMPANY_DOMAIN.COM") && !domain.equalsIgnoreCase("ANOTHER_COMPANY_DOMAIN.COM")) {
            return false;
        }

        return true;
    }

    private int check_code(int code, long chat_id) {
        DataBase db = new DataBase();
        if (db.checkCode(code, chat_id) == 0) {
            db.closeConnection();
            return 0;
        }
        if (db.checkCode(code, chat_id) == 2) {
            db.closeConnection();
            return 2;
        } else {
            db.closeConnection();
            return 1;
        }
    }

    enum Stage {
        START, EVENT, EVENTLIST, HELP, ALARM
    }

    /**
     * Конструктор. Инициализируем подключение к БД. - Дяденька, а почему ты так
     * много комментов оставляешь? - спросишь ты у меня. - Спроси у Алиева
     * Вильяра. - отвечу тебе я.
     *
     */
    public SystemCommand() {

    }

    /**
     * void analyzeText(String text, Response resp, int index). Анализируем
     * входной текст, в частности конкретный update. Так как GetUpdates в
     * телеграме возвратит массив, то мы конкретный индекс массива
     * рассматриваем. Отсюда вытекает: text - Текст пользователя resp - сам
     * Response index - Индекс в массиве который рассматриваем.
     *
     * В принципе, можно переделать и сделать в тысячу раз проще. Но как галсит
     * знаменитая поговорка : Работает? - НЕ ТРОГАЙ! Отмазался как бог, да? Я же
     * найман - мастер отмазок!
     *
     * @param text
     * @param resp
     * @param index
     * @throws java.io.IOException
     */
    public void analyzeText(String text, Response resp, int index) throws IOException {
        log.info(text); // смотри что прислали
        DataBase db = new DataBase();

        long chat_id = resp.getResult()[index].getMessage().getChat().getId();
        TelegramApi telegramAPI = new TelegramApi();
        this.updateChatTitles(resp, index);
        log.info(getCommand(text));
        if (getCommand(text).equalsIgnoreCase("/email")) {
            if (!getCommandArg(text).equalsIgnoreCase("NoTextError") && check_email(getCommandArg(text)) && !getCommandArg(text).contains(" ")) {
                this.send_email(getCommandArg(text), chat_id);
                telegramAPI.sendMessage(chat_id, "Email was sent. Check inbox pls.");
            }
            return;
        }

        if (getCommand(text).equalsIgnoreCase("/code") && !getCommandArg(text).equalsIgnoreCase("NoTextError") && isInteger(getCommandArg(text))) {
            if (check_code(Integer.valueOf(getCommandArg(text)), chat_id) == 1) {
                telegramAPI.sendMessage(chat_id, "Code is incorrect");
                return;
            }
            if (check_code(Integer.valueOf(getCommandArg(text)), chat_id) == 2) {
                telegramAPI.sendMessage(chat_id, "Code is expired");
                return;
            } else {
                telegramAPI.sendMessage(chat_id, "Well done! Now you can use bot.");
                //telegramAPI.sendSticker(chat_id, "CAADAgADKgEAAjdtEAIXLp6mv2VZvAI");
            }
        }

        if (!this.isAuthorized(chat_id)) {
            telegramAPI.sendMessage(chat_id, "You are not authorized.");
            telegramAPI.sendMessage(chat_id, "For authorizationm please enter /email E-mail");
            return;
        }

        switch (getCommand(text)) {
            case "/start":
                telegramAPI.sendMessage(chat_id, "Bot can be user for only internal usage.");
                telegramAPI.sendMessage(chat_id, "Plsease enter /followevent EVENT_NAME");
                telegramAPI.sendMessage(chat_id, "For exsmple SystemEvents");
                break;
            case "/stop_spam":
                telegramAPI.sendMessage(chat_id, "Spam is stopped");
                break;
            case "/start_spam":
                telegramAPI.sendMessage(chat_id, "Spam is started");
                break;
            case "/followevent":
                if (getCommandArg(text).equalsIgnoreCase("NoTextError")) {
                    telegramAPI.sendMessage(chat_id, "For example you need type: /followevent zabbix_shmabix");
                } else {

                    /**
                     * Этот условия для того что бы один человек много раз не
                     * подписывался на событие
                     */
                    if (db.getSignetEvents(chat_id, getCommandArg(text), false) == 0) {
                        telegramAPI.sendMessage(chat_id, "You are already subscribed for event " + getCommandArg(text));
                    }
                    if (db.getSignetEvents(chat_id, getCommandArg(text), false) == 1) {
                        telegramAPI.sendMessage(chat_id, "You are subscribed for event - " + getCommandArg(text));
                        /*
                    Тут нужно сохранить айди чата в связке с названием комнаты
                    Если уже есть такая группа а название другое, то предложить ввести другую команду
                    А вообще тут нужно проверку повесить, на то, есть ли у этой шайки права на получение сообщений.
                         */
                        String chat_name = getChatName(resp, index);
                        db.updateOrInsertChatEvents(chat_id, getCommandArg(text), chat_name, false);
                        db.closeConnection();
                    }
                }
                break;

            case "/eventslist":
                telegramAPI.sendMessage(chat_id, db.getEvenstList(false));
                db.closeConnection();
                break;

            case "/myevents":
                telegramAPI.sendMessage(chat_id, db.getMyEventsList(chat_id));
                db.closeConnection();
                break;

            case "/help":
                telegramAPI.sendMessage(chat_id,
                        "/start - Start with bot\n"
                        /*
                                        Пока оставим эту затею, и займемся тем кто будет случать.
                                        + "/start_spam - Стартуем рассылку (def true)\n"
                                        + "/stop_spam - Останавливаем рассылку\n"
                         */
                        + "/help - Command list\n"
                        + "/followevent - Follow event\n"
                        + "/eventslist - List of all events\n"
                        + "/myevents - My events subscriptions\n"
                        + "/unfollowevent - Unfollow from event\n"
                        + "/followemail - Follow to email events\n"
                        + "/unfollowemail - Unfollow from email events \n"
                        + "/emaileventslist - List of email events");
                break;
            case "/alarm":
                telegramAPI.sendMessage(chat_id, ""
                        + "/alarmTime 8:45 - time in GMT+6\n"
                        + "/alarmDays 1,2,3,4,5 - week days\n"
                        + "/alarmStop - stop this shit");

                break;
            /*case "/alarmTime":
                if (AlarmUtils.checkTime(text)) {
                    setTime(getCommandArg(text));
                    telegramAPI.sendMessage(chat_id, "Будем будить Вас в "
                            + getCommandArg(text) + " пока не соизволите поднять свое "
                            + "тело с кровати. Я тебе не мамка, чтобы будить "
                            + "с любовью и нежностью.");
                } else {
                    telegramAPI.sendMessage(chat_id, "/alarmTime 8:45 - точное время GMT+6");
                }
                break;
            case "/alarmDays":
                if (checkDays(text)) {
                    setDays(getCommandArg(text));
                    telegramAPI.sendMessage(chat_id, "Будем будить Вас в "
                            + getCommandArg(text) + " пока не соизволите поднять свое "
                            + "тело с кровати. Я тебе не мамка, чтобы будить "
                            + "с любовью и нежностью.");
                }
                break;
            */
            case "/delevent":

                if (db.countEvents4Delete(getCommandArg(text)) == true) {
                    db.deleteEvent(getCommandArg(text));
                    telegramAPI.sendMessage(chat_id, getCommandArg(text) + " event was deleted!");
                    db.closeConnection();
                    break;
                }
                if (db.countEvents4Delete(getCommandArg(text)) == false) {
                    telegramAPI.sendMessage(chat_id, getCommandArg(text) + " event was not deleted! ");
                    db.closeConnection();
                    break;
                }
            case "/unfollowevent": {
                if (db.isFollowEvent(getCommandArg(text), chat_id, false) == false) {
                    db.unfollowFromEvent(getCommandArg(text), chat_id, false);
                    telegramAPI.sendMessage(chat_id, "You are unsubscribed from event - " + getCommandArg(text));
                    db.closeConnection();
                    break;
                }
                if (db.isFollowEvent(getCommandArg(text), chat_id, true) == true) {
                    telegramAPI.sendMessage(chat_id, getCommandArg(text) + " you can't unfollow from event that weas not subscribed");
                    db.closeConnection();
                    break;
                }
            }

            case "/shutdown": {
                telegramAPI.sendMessage(chat_id, "Just wait, I'll off you :).");
                break;
            }

            case "/followemail": {

                if (getCommandArg(text).equalsIgnoreCase("NoTextError")) {
                    telegramAPI.sendMessage(chat_id, "For example you need type: /followemail zabbix_shmabix");
                } else /**
                 * Этот условия для того что бы один человек много раз не
                 * подписывался на событие
                 */
                if (db.getSignetEvents(chat_id, getCommandArgBroadcast(text), true) == 1) {
                    telegramAPI.sendMessage(chat_id, "You subscribed for email event - " + getCommandArgBroadcast(text));
                    /*
                            Тут нужно сохранить айди чата в связке с названием комнаты
                            Если уже есть такая группа а название другое, то предложить ввести другую команду
                            А вообще тут нужно проверку повесить, на то, есть ли у этой шайки права на получение сообщений.
                     */
                    String chat_name = getChatName(resp, index);
                    db.updateOrInsertChatEvents(chat_id, getCommandArgBroadcast(text), chat_name, true);
                    db.closeConnection();
                } else {
                    telegramAPI.sendMessage(chat_id, "You already subscribed to email event " + getCommandArgBroadcast(text));
                }
                break;
            }

            case "/unfollowemail": {
                if (db.isFollowEvent(getCommandArgBroadcast(text), chat_id, true) == false) {
                    db.unfollowFromEvent(getCommandArgBroadcast(text), chat_id, true);
                    telegramAPI.sendMessage(chat_id, "Unfollow from event - " + getCommandArgBroadcast(text));
                    db.closeConnection();
                    break;
                }
                if (db.isFollowEvent(getCommandArg(text), chat_id, true) == true) {
                    telegramAPI.sendMessage(chat_id, getCommandArg(text) + " you can't unfollow from event that weas not subscribed");
                    db.closeConnection();
                    break;
                }
            }

            case "/emaileventslist":
                telegramAPI.sendMessage(chat_id, db.getEvenstList(true));
                db.closeConnection();
                break;

            /*case "/broadcastmessage":

                if (new Long(566709922).equals(chat_id) || new Long(151137540).equals(chat_id)) {
                    List<Long> chats = db.getAllChatsToBroadcastMessage();
                    for (int i = 0; i < chats.size(); i++) {
                        telegramAPI.sendMessage(chats.get(i), getCommandArgBroadcast(text));
                    }
                    db.closeConnection();
                }else{
                    telegramAPI.sendMessage(chat_id, "Молодец, нашел фичу скрытую, только она не для тебя.");
                }
                break;
             */
            

        }

    }

    /**
     * String getCommand(String text). Метод возвращает команду из всего текста.
     * Ведь текст может содержать как команду так и сам текст Можно оба метода
     * переписать и сделать одним. НО! Читай коммент выше.
     *
     * @param text
     * @return sdfsdf
     */
    protected String getCommand(String text) {
        String result = "";

        if (text != null) {
            if (text.indexOf("@") != -1) {
                text = text.split("@")[0];
            }
            if (text.length() - text.replaceAll(" ", "").length() > 0) {
                String arr[] = text.split(" ");
                result = arr[0];
            } else {
                result = text;
            }
        } else {
            result = "";
        }

        return result;
    }

    /**
     * String getCommandArg(String text). Метод возвращает аргуметы команды
     * Например /start sidor он вернет sidor Есть контакт? Если аргумента нет то
     * он вернет NoTextError
     */
    public String getCommandArg(String text) {
        text = text.trim().replaceAll(" +", " ");
        String arr[] = text.split(" ");
        if (arr.length > 1) {
            return arr[1];
        } else {
            return "NoTextError";
        }
    }

    /**
     * String getCommandArg(String text). Метод возвращает аргуметы команды
     * Например /start sidor он вернет sidor Есть контакт? Если аргумента нет то
     * он вернет NoTextError
     *
     * @param text
     * @return
     */
    public String getCommandArgBroadcast(String text) {
        text = text.trim().replaceAll(" +", " ");
        String arr[] = text.split(" ");
        String result = "";

        for (int i = 1; i < arr.length; i++) {
            result += arr[i] + " ";
        }

        if (arr.length > 1) {
            return result.trim();
        } else {
            return "NoTextError";
        }
    }

    private void setTime(String commandArg) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void setDays(String commandArg) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private boolean checkDays(String text) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void updateChatTitles(Response resp, int index) {
        long chat_id = resp.getResult()[index].getMessage().getChat().getId();
        DataBase db = new DataBase();
        String title_name = "";
        switch (resp.getResult()[index].getMessage().getChat().getType()) {
            case "private":

                if (resp.getResult()[index].getMessage().getChat().getLast_name() != null) {
                    title_name = resp.getResult()[index].getMessage().getChat().getFirst_name() + resp.getResult()[index].getMessage().getChat().getLast_name();
                    db.updateEventsChatName(chat_id, title_name);
                } else {
                    title_name = resp.getResult()[index].getMessage().getChat().getFirst_name();
                    db.updateEventsChatName(chat_id, title_name);
                }
                break;
            case "group":
                title_name = resp.getResult()[index].getMessage().getChat().getTitle();
                db.updateEventsChatName(chat_id, title_name);
                break;
            case "supergroup":
                title_name = resp.getResult()[index].getMessage().getChat().getTitle();
                db.updateEventsChatName(chat_id, title_name);
                break;
            case "channel":

                break;
        }
        db.closeConnection();

    }

    private String getChatName(Response resp, int index) {

        String title_name = "";
        switch (resp.getResult()[index].getMessage().getChat().getType()) {
            case "private":
                if (resp.getResult()[index].getMessage().getChat().getLast_name() != null) {
                    title_name = resp.getResult()[index].getMessage().getChat().getFirst_name() + resp.getResult()[index].getMessage().getChat().getLast_name();
                } else {
                    title_name = resp.getResult()[index].getMessage().getChat().getFirst_name();
                }
                break;
            case "group":
                title_name = resp.getResult()[index].getMessage().getChat().getTitle();
                break;
            case "supergroup":
                title_name = "supergroup";
                break;
            case "channel":
                title_name = "supergroup";
                break;
        }
        return title_name;
    }

    private boolean isAuthorized(long chat_id) {
        DataBase db = new DataBase();
        boolean is_authorized = db.checkChatRights(chat_id);
        db.closeConnection();
        return is_authorized;
    }

    private static boolean isInteger(String s) {
        boolean isValidInteger = false;
        try {
            Integer.parseInt(s);
            isValidInteger = true;
        } catch (NumberFormatException ex) {
            isValidInteger = false;
        }
        return isValidInteger;
    }
}
