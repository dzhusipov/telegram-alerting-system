package kz.dasm.telegramalertingsystem.handler;

public class TelegramGetter {

    public TelegramGetter() {

    }

    /**
     * void start(). Стартуем! У меня джип в Москве! Метод начинает получать
     * обновления в телеграме.
     */
    public void start() {
        TelegramAPI telegram = new TelegramAPI();
        String responseString = telegram.getUpdates();

        /**
         * Тут проверка на вшивость. Я хз почему, но иногда выходит ошибка. Хочу
         * поймать и понять почему так происходит. Нужно по идее тестами
         * покрывать.
         */
        if (responseString != null) {

            if (!responseString.substring(0, 1).equalsIgnoreCase("{")) {
                System.out.println("Response: " + responseString);
                return;
            }

            Response responseObject = JSON.parseObject(responseString, Response.class);
            if (responseObject != null) {
                DataBase db = new DataBase();

                SystemCommand systemComands = new SystemCommand();
                if (responseObject.getOk().equalsIgnoreCase("true")) {
                    /*
                     * Если все хорошо, то понеслась жара
                     */
                    int result_count = responseObject.getResult().length;
                    if (result_count > 0) {
                        for (int i = 0; i < result_count; i++) {
                            try {
                                System.out.println("responseString " + responseString);
                                if (responseObject.getResult()[i].getMessage() != null) {
                                    systemComands.analyzeText(responseObject.getResult()[i].getMessage().getText(), responseObject, i);
                                }
                            } catch (IOException ex) {
                                Logger.getLogger(TelegramGetter.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            db.updateOffset(String.valueOf(responseObject.getResult()[i].getUpdate_id() + 1));
                        }
                    } else {
                        //System.out.println("No updates");
                    }
                }
                db.closeConnection();
            }
        }
    }
}