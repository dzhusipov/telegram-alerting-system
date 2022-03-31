package kz.dasm.telegramalertingsystem.api;

import org.springframework.web.client.RestTemplate;

import kz.dasm.telegramalertingsystem.db.DataBase;
import kz.dasm.telegramalertingsystem.models.GetUpdates;
import kz.dasm.telegramalertingsystem.models.SendMessage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author DAsm
 *         Наверное это основной враппер для отправки в телеграм.
 *         - Почему ты такой неуверенный в своих описаниях? - спросишь меня ты.
 *         - Потому, что гладиолус - отвечу я.
 *         Капитан логика, да?
 * 
 */

public class TelegramApi {
    // Logger
    private static Logger log = Logger.getLogger(TelegramApi.class.getName());
    // корпоративщиков
    private static final String TELEGRAM_API_HOSTNAME = "https://api.telegram.org/bot";
    /*------------------------------------------------------------------------------------------*
     * Токен бота. Токен тестовый, так шо ни сы
     */
    private static final String BOT_TOKEN = "5262848674:AAENA77QSDpZl5xslRcR4fO9ngEhuAmjSoY";
    /*------------------------------------------------------------------------------------------*/
    private static final String URL = TELEGRAM_API_HOSTNAME + BOT_TOKEN + "/";
    private static final String CHARSET = "utf-8";
    private static final String ERROR = "Shit happens TelegramAPI.java!";

    private static final String GET_UPDATES = "getUpdates";
    private static final String SEND_MESSAGE = "sendMessage";
    private static final String SEND_STICKER = "sendSticker";

    private static final String GRAFANA_TOKEN = "GRAFANA_TOKEN";
    private static final String AUTH_TYPE = "Bearer ";
    private static final String SEND_PHOTO = "sendPhoto";

    private RestTemplate restTemplate;
    /**
     * Конструктор.
     * Прям тут, сразу выставляем проксю от ИБ - Привет Марат =)
     */
    public TelegramApi(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        // this.setProxy();
    }

    /**
     * String sendMessage(long chat_id, String text_message).
     * Голый метод отправки сообщения.
     * Смысла описывать не вижу. Взял чат айди и пульнул туда мессагу в формате json
     * через библиотеку алибабы, того самого с алиэкспресса.
     * 
     * @param chat_id
     * @param text_message
     * @return
     */
    public String sendMessage(long chat_id, String text_message) {

        return this.sendMessage(chat_id, text_message, "");
    }

    public String sendMessage(long chat_id, String text_message, String parse_mode) {

        if (parse_mode.equalsIgnoreCase("?") || parse_mode.length() < 1) {
            parse_mode = "None";
        }

        SendMessage sendMessageObj = new SendMessage();
        parse_mode = parse_mode.equalsIgnoreCase("") || parse_mode.isEmpty() ? "None" : parse_mode;

        sendMessageObj.setChat_id(chat_id);
        sendMessageObj.setText(text_message);
       
        String response = this.restTemplate.postForObject(URL + SEND_MESSAGE, sendMessageObj, String.class);

        return response;
    }

    /**
     * String getUpdates().
     * Метод получения обновлений.
     * По идее простой как автомат калашникова.
     * Все объекты описаны. Бери, заполняй и отправляй.
     * Тут так же. Взял оффсет, сформировал request и вуаля.
     * 
     * @return
     */
    public String getUpdates() {

        GetUpdates getUpdClass = new GetUpdates();
        DataBase db = new DataBase();
        getUpdClass.setOffset(db.getOffset());

        String response = this.restTemplate.postForObject(URL + GET_UPDATES, getUpdClass, String.class);
        db.closeConnection();
        return response;
    }

    /**
     * InputStream getPhotoToSendGrafana().
     * Метод получения картинки графика из Графаны.
     * На вход получает УРЛ запроса, на выходе выплевывает поток.
     * 
     * @return
     */
    public InputStream getPhotoToSendGrafana(String url_path) {
        InputStream inputSrtm;
        try {
            URL url = new URL(url_path);
            URLConnection uc = url.openConnection();
            String basicAuth = AUTH_TYPE + GRAFANA_TOKEN;
            HttpURLConnection httpConn = (HttpURLConnection) uc;
            httpConn.setRequestProperty("Authorization", basicAuth);
            httpConn.setRequestProperty("Content-Type", "multipart/form-data");
            httpConn.setRequestMethod("GET");
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);
            httpConn.connect();
            // int responseCode= httpConn.getResponseCode();
            // log.info("Response code:-" + responseCode);
            inputSrtm = httpConn.getInputStream();
        } catch (Exception e) {
            log.info("Rava: Error in TelegramAPI getPhotoToSendGrafana: "
                    + System.getProperty("http.proxyHost") + System.getProperty("https.proxyHost"));
            log.info(ERROR);
            inputSrtm = null;
        }
        return inputSrtm;

    }

    public String sendPhoto(long chat_id, String photo, String type) {
        return this.sendPhoto(chat_id, photo, "", type);
    }

    /**
     * String sendPhoto().
     * Метод отправки картинки, фотки в Телегу.
     * Взял чат айди и пульнул туда фотку с подписью в виде URL или файла
     * Не забываем указывать тип источника картинки:
     * URLreq - УРЛ запрос (в нашем случае в Графану)
     * URL - УРЛ общедоступной картинки из Ынтырнэта
     * File - непосредственно физический файл
     * 
     * @param chat_id
     * @param photo
     * @param caption
     * @param type
     */
    public String sendPhoto(long chat_id, String photo, String caption, String type) {
        String result = "";

        try {

            UploadSentPhoto uploadPhoto = new UploadSentPhoto(URL + SEND_PHOTO, CHARSET);
            uploadPhoto.addHeaderField("User-Agent", "CodeJavaTelegramBot");
            uploadPhoto.addHeaderField("Telegram-Header", "Header-Value");
            uploadPhoto.addFormField("chat_id", String.valueOf(chat_id));
            if (!caption.isEmpty())
                uploadPhoto.addFormField("caption", caption);
            switch (type) {
                case "URLreq":

                    uploadPhoto.addFilePart("photo", this.getPhotoToSendGrafana(photo));
                    break;
                case "URL":
                    uploadPhoto.addFormField("photo", photo);
                    break;
                case "File":

                    uploadPhoto.addFilePart("photo", new File(photo));
                    break;

                default:
                    uploadPhoto.addFormField("photo", photo);
                    break;
            }

            List<String> response = uploadPhoto.finish();
            // log.info("TELEGRAM SERVER REPLIED:");
            for (String line : response) {
                // log.info(line);
                result.concat(line);
            }

        } catch (IOException e) {
            log.info("Error in TelegramAPI sendPhoto: " + System.getProperty("http.proxyHost")
                    + System.getProperty("https.proxyHost"));
            log.info(ERROR);
            result = null;
        }
        return result;
    }

    // TODO need to fix
    public String sendSticker(long chat_id, String sticker_id) {
        String result = "";
        /*
         * 
         * SendSticker sendStickerObj = new SendSticker();
         * 
         * sendStickerObj.setChat_id(chat_id);
         * sendStickerObj.setSticker(sticker_id);
         * 
         * RestTemplate restTemplate = new RestTemplate();
         * String response = restTemplate.postForObject(URL + SEND_STICKER, getUpdClass,
         * String.class);
         * return response;
         * 
         * try {
         * URL url = null;
         * URLConnection connection = null;
         * HttpURLConnection httpConn = null;
         * 
         * BytZ
         * StringBuilder sb = new StringBuilder();
         * bout = new ByteArrayOutputStream();
         * 
         * SendSticker sendStickerObj = new SendSticker();
         * 
         * sendStickerObj.setChat_id(chat_id);
         * sendStickerObj.setSticker(sticker_id);
         * 
         * String json_text = JSON.toJSONString(sendStickerObj);
         * byte[] buffer = new byte[json_text.length()];
         * buffer = json_text.getBytes();
         * bout.write(buffer);
         * byte[] b = bout.toByteArray();
         * 
         * url = new URL(URL + SEND_STICKER);
         * //connection = url.openConnection(PROXY);
         * connection = url.openConnection();
         * httpConn = (HttpURLConnection) connection;
         * 
         * httpConn.setRequestProperty("Content-Type", CONTENT_TYPE);
         * httpConn.setRequestProperty("Accept-Encoding", ACCEPT_ENCODING);
         * httpConn.setRequestMethod(POST);
         * httpConn.setDoOutput(true);
         * httpConn.setDoInput(true);
         * //this.setProxy();
         * httpConn.connect();
         * out = httpConn.getOutputStream();
         * out.write(b);
         * out.close();
         * 
         * InputStreamReader inputStreamReader = new
         * InputStreamReader(httpConn.getInputStream(), CHARSET);
         * bufferReader = new BufferedReader(inputStreamReader);
         * 
         * while ((output = bufferReader.readLine()) != null) {
         * sb.append(output);
         * }
         * result = sb.toString();
         * 
         * } catch (IOException e) {
         * log.info("DAsm: Error in TelegramApi sendSticker: " +
         * System.getProperty("http.proxyHost") +
         * System.getProperty("https.proxyHost"));
         * log.info(ERROR);
         * log.info(e);
         * result = null;
         * }
         */
        return result;
    }

    /**
     * void setProxy().
     * Ну, ставим проксю. Это наш билет во внешний мир. Без него мы как
     * владельцы грин карт, не иммем права голосовать.
     * 
     * private void setProxy(){
     * System.setProperty("http.proxyHost", PROXY_HOST);
     * System.setProperty("http.proxyPort", PROXY_PORT);
     * System.setProperty("https.proxyHost", PROXY_HOST);
     * System.setProperty("https.proxyPort", PROXY_PORT);
     * }
     * 
     * private void disableProxy(){
     * System.setProperty("http.proxyHost", "");
     * System.setProperty("http.proxyPort", "");
     * System.setProperty("https.proxyHost", "");
     * System.setProperty("https.proxyPort", "");
     * }
     */
}
