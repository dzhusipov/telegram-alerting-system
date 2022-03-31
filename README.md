<div align="center">
    <h1>Telegram alerting system</h1>
</div>

To run application you can
```sh
./gradlew clean && ./gradlew bootRun
```

To build jar and run it you need to  type
```sh
gradlew.bat clean && gradlew.bat build && java -jar build/libs/telegram-alerting-system-0.1.0.jar
```

this proxies if you need to use them. Just run with
```
java -jar build/libs/telegram-alerting-system-0.0.1-SNAPSHOT.jar  
```
## What a hell this for?
You can create telegram groups and add bot to group.  
Then you need to subscribe to events.  
For example, you need to get all of zabbix error messages.  
You simply creating event named as you want such as "zabbix".  
Then, you alerting to telegram via SOAP or REST with 2 params - event name and message.  
All who was subscribed to event will get message. Message can get in telegram group or directly from bot.

## Proxy settings
-Dhttp.proxyHost=webcache.mydomain.com  
-Dhttp.proxyPort=8080  
-Dhttp.noProxyHosts=”localhost|host.mydomain.com” GetURL  
-Dhttp.proxyUser=USERNAME  
-Dhttp.proxyPassword=PASSWORD  

## HowTo
- Создаем канал в телеграме
- Добавляем бота
- Авторизовываемся в нем с помощью команды /email
- Вводим код, который пришел на почту с помощью команды /code (Код действителен в течении 30 минут)
- Прописываем название события, чтобы подписаться на него и получать уведомления командой /event - (Название события должно быть уникальным)
- Создаем тестовый алерт для проверки работы бота

## TODO
- SOAP interface for old enterprise systems
- Проверить в целом приложение полностью


http://localhost:8080/ws/telegramAlertingSystem.wsdl


gradlew.bat clean && gradlew.bat build && java -jar build/libs/telegram-alerting-system-0.1.0.jar



./gradlew bootRun --args='--spring.profiles.active=dev'
