<div align="center">
    <h1>Telegram alerting system</h1>
</div>

## What a hell this for?
You can create telegram groups and add bot to group.  
Then you need to subscribe to events.  
For example, you need to get all of zabbix error messages.  
You simply creating event named as you want such as "zabbix".  
Then, you alerting to telegram via SOAP or REST with 2 params - event name and message.  
All who was subscribed to event will get message. Message can get in telegram group or directly from bot.

## HowTo
- Create bot with BotFather in Telegram messeenger
- Add bot to Telegram GRoup or DM to bot
- Authorize with /email command
- Write code that you get in email /code (Code will expire in 30 minutes)
- Write event name to subcribe for it /event - (Event name must be unique or you will subncribe to existing one)
- Test alerts

## TODO
- Need to check it full

## SOAP endpoint
http://localhost:8080/ws/telegramAlertingSystem.wsdl

## REST endpoints
http://localhost:8080/telegram/send-message-to-event/{event_name}/{event_message}  
http://localhost:8080/telegram/send-message-to-chat/{chat_id}/{text}  

## Proxy settings
You need to create new application.properties file or write in default 
```
### Proxy settings ###
proxy.host = none | proxy.com
proxy.port = 0 | 80 | 8080

# Logging settings
logging.level.web = INFO | DEBUG
```

## Run it
Run application you can
```sh
./gradlew clean && ./gradlew bootRun
```

Build jar and run it you need to  type
```sh
gradlew.bat clean && gradlew.bat build && java -jar build/libs/telegram-alerting-system-0.1.0.jar
```
Run jar
```
java -jar build/libs/telegram-alerting-system-0.0.1-SNAPSHOT.jar  
```
Run with cusom profile
```
./gradlew bootRun --args='--spring.profiles.active=dev'
```
