package kz.dasm.telegramalertingsystem.db;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author DAsm Класс БД. Используем мааааааааааленькую sqlite БД =)
 * Архитектура БД
 * 
 * Таблица установки и генерации кодов для авторизации
 *      chat_code  
 *          chat_id         - id чата. Ссылка на чат с которым работает.
 *          code            - Сгенерированный код который действует 30 минут
 *          start_time      - Дата генерации кода
 *          finish_time     - Дата прекращения действия кода
 * 
 * Таблица проверки прав пользователя
 *      chat_rights
 *          chat_id         - id чата. Ссылка на чат с которым работает.
 *          authorized      - Авторизован ли пользователь получать события и посылать комманды
 *
 * Таблица связывающая события и пользователей/группы
 *      chat_events
 *          chat_id         - id чата. Ссылка на чат с которым работает.
 *          name            - Название события
 *          chat_name       - Название группового чата или пользователя если он подписался на событие в индивидуальном порядке
 * 
 * Таблица конфигурации
 *      config
 *          name            - Название конфигурации
 *          value           - Значение конфигурации
 * 
 * Таблица пользователей. На данный момент не используется.
 *      user
 *          
 * Таблица с сохранением row_id из зависших джобов
 *      siebel_apk_wf
 *          row_id          - ROW_ID записи из БД
 * 
 * Таблица с сохранением row_id из зависших джобов
 *      chat_emails
 *          chat_id         - id чата. Ссылка на чат с которым работает.
 *          email_subject_name            - Название события
 *          chat_name       - Название группового чата или пользователя если он подписался на событие в индивидуальном порядке
 */
public class DataBase {

    private static final String DB_NAME = "PATH/TO/SQLITEDB/telegram_bot.db";
    private static final String CLASS4NAME = "org.sqlite.JDBC";
    private Connection connection;
    private static final String TBL_CONFIG = "config";
    private static final String TBL_CHAT_EVENTS = "chat_events";
    private static final String TBL_CHAT_RIGHTS = "chat_rights";
    private static final String TBL_CHAT_CODE = "chat_code";
    private static final String TBL_CHAT_EMAILS = "chat_emails";

    /*
    *   Инициализируем подключение к БД сразу.
     */
    public DataBase() {
        this.connect();
    }

    /**
     * connect(). Коннектимся к БД.
     */
    private void connect() {

        try {
            Class.forName(CLASS4NAME);
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_NAME);

        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("connect - " + e.getClass().getName() + ": " + e.getMessage());
            this.closeConnection();
            /**
             * Не знаю, на сколько я тут прав, но выходим сразу в случае
             * критической ситуации.
             */
            System.exit(0);
        }

    }

    /**
     * getOffset(). Наверное главный метод, который берет OFFSET и по этому
     * оффсету начинает забирать обновления (читай сообщения пользователя) из
     * телеграма. Если интересно, читай документацию на офф сайте. Если тебе,
     * конечно, безопасники доступ дадут... Муахахахаха!
     * @return 
     */
    public String getOffset() {
        String offset = "-1";
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT value FROM " + TBL_CONFIG + " WHERE name='offset';");

            while (rs.next()) {
                offset = rs.getString("value");
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            System.err.println("getOffset - " + e.getClass().getName() + ": " + e.getMessage());
        }
        return offset;
    }

    /**
     * updateOffset(String offset). После получения обновлений, мы обновляем
     * offset, чтобы знать, какие сообщения мы уже забрали в будущем.
     */
    public void updateOffset(String offset) {
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            String sql = "UPDATE " + TBL_CONFIG + " set value ='" + offset + "' where name='offset';";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            System.err.println("updateOffset - " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * updateOrInsertChatTitle(int chat_id, String title). chat_id - ID чата.
     * Уникальный идентификатор чата title - это те же event_name. Почему я так
     * не назвал? а я неИмею возможности знать?
     *
     * Метод обвновляет или добавляет связку чата и ивента. то есть получается
     * так, что каждый чат имеет подписку на event. Не важно он групповой
     * или персональный. По идее, если рассуждать, механизм получается уникальный.
     * Захотел - написал скрипт на сервере, с посылом сообщения и своим ивентом.
     * Например message - "ахахха, какой ты смищной", event_name - "ilovemyself"
     * а в телеграме подписался на ивент ilovemysef. И вуаля!
     *
     * @param chat_id
     * @param title
     * @param chat_name
     * @param isEmail 
     */
    public void updateOrInsertChatEvents(long chat_id, String title, String chat_name, boolean isEmail) {
        String table = "";
        if (isEmail){
            table = TBL_CHAT_EMAILS;
        }else{
            table = TBL_CHAT_EVENTS;
        }
        
        try {
            Statement stmt = connection.createStatement();
            String sql = "SELECT COUNT(*) as cnt FROM " + table
                    + " WHERE chat_id=" + chat_id + " AND name='"
                    + title.toUpperCase() + "';";
            ResultSet rs = stmt.executeQuery(sql);
            int cnt = 0;
            while (rs.next()) {
                cnt = rs.getInt("cnt");
            }

            if (cnt > 0) {
                updateChatEvents(chat_id, title, chat_name, isEmail);
            } else {
                insertIntoChatEvents(chat_id, title, chat_name, isEmail);
            }

            rs.close();
            stmt.close();
        } catch (Exception e) {
            System.err.println("updateOrInsertChatTitle - "
                    + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * insertIntoChatTitle(long chat_id, String title). Добавляем в БД новый
     * ивент и привязываем его к чату
     */
    private void insertIntoChatEvents(long chat_id, String title, String chat_name, boolean isEmail) {
        String table = "";
        if (isEmail){
            table = TBL_CHAT_EMAILS;
        }else{
            table = TBL_CHAT_EVENTS;
        }
        try {
            Statement stmt = connection.createStatement();
            String sql = "insert into " + table + " values (" + chat_id + ",'" + title.toUpperCase() + "', '" + chat_name + "');";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            System.err.println("insertIntoChatTitle - " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * getSignetEvents метод смотрит подписан ли чел на канал. Eсли да отправляет 0
     * Eсли нет отправляет 1
     *
     * @param chat_id
     * @param chat_title
     * @param isEmail 
     * @return
     */
    public int getSignetEvents(long chat_id, String chat_title, boolean isEmail) {
        String table = "";
        
        if (isEmail){
            table = TBL_CHAT_EMAILS;
        }else{
            table = TBL_CHAT_EVENTS;
        }
        
        int cnt = 0;
        try {
            Statement stmt = connection.createStatement();
            String sql = "select count(*) as cnt  from " + table + "  where chat_id=" + chat_id + " and name = upper('" + chat_title + "');";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                cnt = rs.getInt("cnt");
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            System.err.println("getSignetEvents() - " + e.getClass().getName() + ": " + e.getMessage());
        }
        if (cnt == 0) {
            return 1; //не подписан
        } else {
            return 0; //подписан
        }
    }

    /**
     * updateChatTitle(long chat_id, String title). Обновляем подписку на событие
     * для чата. То есть получается меняем получаемые сообщения. 
     */
    private void updateChatEvents(long chat_id, String title, String chat_name, boolean isEmail) {
        String table = "";
        if (isEmail){
            table = TBL_CHAT_EMAILS;
        }else{
            table = TBL_CHAT_EVENTS;
        }
        
        try {
            Statement stmt = connection.createStatement();
            String sql = "update " + table + " set name='" + title.toUpperCase() + "', chat_name='" + chat_name + "' where chat_id=" + chat_id;
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            System.err.println("updateChatTitle - " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * updateChatTitle(long chat_id, String title). Обновляем подписку на событие
     * для чата. То есть получается меняем получаемые сообщения. 
     */
    public void updateChatID(long old_chat_id, long new_chat_id, boolean isEmail) {
        String table = "";
        if (isEmail){
            table = TBL_CHAT_EMAILS;
        }else{
            table = TBL_CHAT_EVENTS;
        }
        
        try {
            Statement stmt = connection.createStatement();
            String sql = "update " + table + " set chat_id=" + new_chat_id + " where chat_id=" + old_chat_id;
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            System.err.println("updateChatTitle - " + e.getClass().getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Метод получения уникальных ивентов без условий.
     * Не трогать, все работает
     * @param isEmail 
     * @return
     */
    public String getEvenstList(boolean isEmail) {
        String table = "";
        if (isEmail){
            table = TBL_CHAT_EMAILS;
        }else{
            table = TBL_CHAT_EVENTS;
        }
        String chats = "";
        try {
            Statement stmt = connection.createStatement();
            String sql = "select distinct(name) from " + table;  
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                chats += rs.getString("name") + "\n";
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            System.err.println("getEvenstList - " + e.getClass().getName() + ": " + e.getMessage());
        }
        return chats;
    }
    
    /**
     * getAllChatsToBroadcastMessage. Метод получения уникальных ивентов без условий.
     * Не трогать, все работает
     * @return
     */
    public List<Long> getAllChatsToBroadcastMessage() {
        List<Long> chats = new ArrayList<>();
        
        try {
            Statement stmt = connection.createStatement();
            String sql = "select distinct(chat_id) from ( select distinct (chat_id) from " + TBL_CHAT_EMAILS + " union select distinct (chat_id) from " + TBL_CHAT_EVENTS + ") t";  
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                chats.add(rs.getLong("chat_id"));
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            System.err.println("getAllChatsToBroadcastMessage - " + e.getClass().getName() + ": " + e.getMessage());
        }
        return chats;
    }

    /**
     * Метод получения уникальных ивентов на которые подписан пользователь или группа.
     * Не трогать, все работает
     * @param chat_id 
     * @return
     */
    public String getMyEventsList(long chat_id) {
        String chats = "";
        try {
            Statement stmt = connection.createStatement();
            String sql = "select name from " + TBL_CHAT_EVENTS + " where chat_id = " + chat_id
                    + " union select name from " + TBL_CHAT_EMAILS + " where chat_id = " + chat_id;  
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                chats += rs.getString("name") + "\n";
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            System.err.println("getMyEventList - " + e.getClass().getName() + ": " + e.getMessage());
        }
        return chats;
    }

    
    /**
     * Метод получения количества людей на событии для удаления канала
     *
     * @param title название события
     * @return
     */
    public boolean countEvents4Delete(String title) {
        int cnt = 0;
        try {
            Statement stmt = connection.createStatement();
            String sql = "select count(*) as cnt from " + TBL_CHAT_EVENTS + " where name = upper('" + title + "');";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                cnt = rs.getInt("cnt");
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            System.err.println("countEvents() " + e.getClass().getName() + ": " + e.getMessage());
        }

        /**
         * Не совсем ясно что за равно 1? А если больше одного? А если меньше?
         */
        if (cnt == 1) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * Метод удаления события.
     *
     * @deletedEvents у нас будет удалять событие.
     * @param title
     */
    public void deleteEvent(String title) {
        try {
            Statement stmt = connection.createStatement();
            String sql = "delete from " + TBL_CHAT_EVENTS + " where name = upper('" + title + "');";
            ResultSet rs = stmt.executeQuery(sql);
            rs.close();
            stmt.close();
        } catch (Exception e) {
            System.err.println("deletedEvents() " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * *
     * здесь считает сколько людей подписано на событие если число равен 1 то он
     * отправляет к удаленю если больше одно то событие не удалиться
     *
     * @param title
     * @param chat_id
     * @return
     */
    public boolean isFollowEvent(String title, long chat_id, boolean isEmail) {
        String table = "";
        if (isEmail){
            table = TBL_CHAT_EMAILS;
        }else{
            table = TBL_CHAT_EVENTS;
        }
        
        int cnt = 0;
        try {
            Statement stmt = connection.createStatement();
            String sql = "select count(*) as cnt from " + table + " where name = upper('" + title + "') and chat_id = " + chat_id + ";";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                cnt = rs.getInt("cnt");
            }
            rs.close();
            stmt.close();

        } catch (Exception e) {
            System.err.println("isFollowEvent() " + e.getClass().getName() + ": " + e.getMessage());
        }
        // Тоже вопрос А если более одного? А если меньше?
        if (cnt == 1) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Чел отписывается от события.
     *
     * @param title
     * @param chat_id
     * @param isEmail 
     */
    public void unfollowFromEvent(String title, long chat_id, boolean isEmail) {
        String table = "";
        if (isEmail){
            table = TBL_CHAT_EMAILS;
        }else{
            table = TBL_CHAT_EVENTS;
        }
        
        try {
            Statement stmt = connection.createStatement();
            String sql = "delete from " + table + " where name = upper('" + title + "') and chat_id = " + chat_id + ";";
            ResultSet rs = stmt.executeQuery(sql);
            rs.close();
            stmt.close();
        } catch (Exception e) {
            System.err.println("unfollowEvents() " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * closeConnection(). На всяяяяякий случай, я добавляю закрытие коннекта к
     * БД А то бывает всякое. Тормоза, лаги, и падения.
     */
    public void closeConnection() {
        try {
            if (this.connection != null) {
                this.connection.close();
            }
        } catch (SQLException ex) {
            System.out.println("closeConnection");
            Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * getChatID(String name). Получаем ID чата, по названию ивента. Так как в
     * принципе никто не знает чат айди, а только инвент нэйм и мне шлют нэймы
     * ивентов (какой я русский, да?) я определяю по этому кретерию айди чата и
     * шлю туда твое мерзкое сообщение.
     *
     * @param name
     * @return
     */
    public List<String> getEventsChatID(String name, boolean isEmail) {

        String sql = "";
        if (isEmail){
            sql = "SELECT chat_id FROM " + TBL_CHAT_EMAILS + " WHERE '" + name.toUpperCase().replaceAll("[']", "''").replace("  ", " ") + "' like '%' || name || '%';";
            System.out.println("sql: " + sql);
        }else{
            sql = "SELECT chat_id FROM " + TBL_CHAT_EVENTS + " WHERE name = '" + name.toUpperCase() + "';";
        }
        List<String> chat_id = new ArrayList<String>();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                chat_id.add(rs.getString("chat_id"));
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            System.err.println("getEventsChatID - " + e.getClass().getName() + ": " + e.getMessage());
        }
        return chat_id;
    }

    /**
     * Метод какого-то лешего обновляет имя чата. Что за дичь? Почему я это не
     * откомментировал? Если найдется псих который зашарит, по-братски прошу -
     * оставь коммент норм
     */
    public void updateEventsChatName(long chat_id, String chat_name) {
        try {
            Statement stmt = connection.createStatement();
            String sql = "update " + TBL_CHAT_EVENTS + " set chat_name='" + chat_name + "' where chat_id=" + chat_id;
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            System.err.println("updateChatName - " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Метод проверки прав пользователя. 
     * Нужно вместо тру и фолса ставить 0 или 1. А то индусский код получается
     * @param chat_id айди чата как обычно
     * @return
     */
    public boolean checkChatRights(long chat_id) {
        String authorized = "false";
        try {
            Statement stmt = connection.createStatement();
            String sql = "SELECT chat_id, authorized FROM " + TBL_CHAT_RIGHTS + " WHERE chat_id='" + chat_id + "';";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                authorized = rs.getString("authorized");
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            System.err.println("check_chat_rights() - " + e.getClass().getName() + ": " + e.getMessage());
        }

        if (authorized.equalsIgnoreCase("false")) {
            return false;
        } else {
            return true;
        }
    }

    /**
     *
     * @param code Введенный код
     * @param chat_id Айди чата
     * @return Значение return 
     * 0 - Success return 
     * 1 - Code is wrong return 
     * 2 - Code is expired
     * ПЕРЕДЕЛАТЬ
     */
    public int checkCode(int code, long chat_id) {
        int code_db = 0;
        ResultSet rs;
        try {
            Statement stmt = connection.createStatement();
            String sql = "SELECT code FROM " + TBL_CHAT_CODE + " WHERE datetime('now','6 hours') between start_time and finish_time and chat_id='" + String.valueOf(chat_id) + "';";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                code_db = rs.getInt("code");
            }
            rs.close();
            stmt.close();
            if (code_db == 0) {
                stmt = connection.createStatement();
                rs = stmt.executeQuery("SELECT code FROM " + TBL_CHAT_CODE + " WHERE  chat_id='" + String.valueOf(chat_id) + "';");
                while (rs.next()) {
                    code_db = rs.getInt("code");
                }
                rs.close();
                stmt.close();
                if (code_db == code) {
                    code_db = 2;
                }
            }
        } catch (Exception e) {
            System.err.println("check_code() - " + e.getClass().getName() + ": " + e.getMessage());
        }

        if (code == code_db) {
            this.setAuthorization(chat_id);
            return 0;
        }
        
        if (code_db == 2) {
            return 2;
        } else {
            return 1;
        }
    }
    
    /**
     * updateAuthorization(long chat_id).
     * Метод обновления прав.
     * разрешает пользоваться ботом
     */
    private void updateAuthorization(long chat_id) {
        try {
            Statement stmt = connection.createStatement();
            String sql = "update " + TBL_CHAT_RIGHTS + " SET authorized='true' WHERE chat_id='" + chat_id + "';";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            System.err.println("updateAuthorization() - " + e.getClass().getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * insertAuthorization(long chat_id).
     * Метод добавления строки авторизации. ??
     */
    private void insertAuthorization(long chat_id) {
        try {
            Statement stmt = connection.createStatement();
            String sql = "insert into " + TBL_CHAT_RIGHTS + " values ('" + chat_id + "', 'true');";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            System.err.println("updateAuthorization() - " + e.getClass().getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Метод создания нового кода авторизации.
     * 
     */
    public void createNewAuthCode(long chat_id, int code) {
        if (isCodeExist(chat_id)) {
            this.updateCode(chat_id, code);
        } else {
            this.insertCode(chat_id, code);
        }
    }

    /**
     * добавляет в таблицу chat_code код который отправился
     *
     * @param chat_id
     * @param code
     */
    private void insertCode(long chat_id, int code) {
        try {
            Statement stmt = connection.createStatement();
            String sql = "insert into " + TBL_CHAT_CODE + " values ('" + chat_id + "', '" + code + "', datetime('now', '6 hours'), datetime('now', '6 hours', '30 minutes'));";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            System.err.println("insertCode() - " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * обновляет код в таблице chat_code
     *
     * @param chat_id
     * @param code
     */
    private void updateCode(long chat_id, int code) {
        try {
            Statement stmt = connection.createStatement();
            String sql = "update " + TBL_CHAT_CODE + " set code='" + code + "', start_time = datetime('now', '6 hours'), finish_time = datetime('now', '6 hours', '30 minutes')  where chat_id='" + chat_id + "';";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            System.err.println("updateCode() - " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Проверка существования кода.
     * 
     */
    private boolean isCodeExist(long chat_id) {
        int cnt = 0;
        try {
            Statement stmt = connection.createStatement();
            String sql = "SELECT count(*) as cnt FROM " + TBL_CHAT_CODE + " WHERE chat_id='" + String.valueOf(chat_id) + "';";
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                cnt = rs.getInt("cnt");
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            System.err.println("check_chat_rights() - " + e.getClass().getName() + ": " + e.getMessage());
        }

        if (cnt > 0) {
            return true;
        } else {
            return false;
        }

    }
    
    /**
     * isRightExist.
     * Проверка на существование прав в таблице
     */
    private boolean isRightExist(long chat_id) {
        int cnt = 0;

        try {
            Statement stmt = connection.createStatement();
            String sql = "SELECT count(*) as cnt FROM " + TBL_CHAT_RIGHTS + " WHERE chat_id='" + String.valueOf(chat_id) + "';";
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                cnt = rs.getInt("cnt");
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            System.err.println("check_chat_rights() - " + e.getClass().getName() + ": " + e.getMessage());
        }

        if (cnt > 0) {
            return true;
        } else {
            return false;
        }

    }
    /**
     * Метод инсерта или апдейта прав в таблице
     */
    private void setAuthorization(long chat_id) {
        if (this.isRightExist(chat_id)) {
            this.updateAuthorization(chat_id);
        } else {
            this.insertAuthorization(chat_id);
        }

    }
}