# Remind me, Doc

Remind me, Doc is a bot in Messenger (Facebook) that helps people to stay healthy. 
This bot has access to your medical card in DrChrono and can send you information about the medication that your doctor has prescribed for you, including timing, dosage information and pharmacy note such as when you should take this pill after/before or with meals. You can set up a time and the bot will remind you to take medicine in this time by sending you a message on Facebook. Also, Remind me, Doc bot will help you to measure your vitals daily (temperature, blood pressure, pulse, pain level) and will add this data to your medical card in DrChrono. In case you have dangerous health indicators the bot will propose you to visit your doctor and will help you immediately to schedule an appointment via Messenger. Also, if your doctor recommends you to do some compulsory checkups during this year the bot will also remind you about them and will help you to schedule them. 

## Technology stack & other Open-source libraries

### Data

* 	[PostGreQSL](https://www.postgresql.org/) - Open-Source Relational Database Management System

### Client - Frontend/UI

*   [Messenger](https://www.messenger.com/) - Facebook messenger for communication and bot integration
* 	[Thymeleaf](https://www.thymeleaf.org/) - Modern server-side Java template engine for both web and standalone environments.

### Server - Backend

* 	[JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) - Java™ Platform, Standard Edition Development Kit
* 	[Spring Boot](https://spring.io/projects/spring-boot) - Framework to ease the bootstrapping and development of new Spring Applications
* 	[Maven](https://maven.apache.org/) - Dependency Management

###  Libraries and Plugins

* 	[Lombok](https://projectlombok.org/) - Never write another getter or equals method again, with one annotation your class has a fully featured builder, Automate your logging variables, and much more.
*   [Messenger4j](https://github.com/messenger4j/messenger4j) - A Java library for building Chatbots on the Facebook Messenger Platform.

### Others 

* 	[git](https://git-scm.com/) - Free and Open-Source distributed version control syste
* 	[Postman](https://www.getpostman.com/) - API Development Environment (Testing Docmentation)



### Configuration
In order to get your chatbot working you have to provide the following settings:

Facebook App Properties :
Get this properties from `https://developers.facebook.com/apps/`
```
appSecret = ${MESSENGER_APP_SECRET} # App Secret of Your Messeger Apps
verifyToken = ${MESSENGER_VERIFY_TOKEN} # Token From Your Server Page
pageAccessToken = ${MESSENGER_PAGE_ACCESS_TOKEN} # Token From Your Facebook Page
botId=${BOT_ID} # Id of Your Bot
```
Dr Chrono Properties:

Get this properties from `https://app.drchrono.com/api-docs/#section/Authorization` 
and Page API from Personal Page  `https://XXX.drchrono.com/api-management/`
```
redirect.uri=${URL_BASE} # Registered as API Redirect URI
drchrono.clientId=${DR_CLIENTID} # Client ID from API Management
drchrono.clientSecret=${DR_CLIENT_SECRET} # Client Secret from API Management
refresh.token=${REFRESH_TOKEN} # Refresh token after Dr Chronos Authorization
```

Database Properties:
```
spring.datasource.url=${DATABASE_URL} 
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}
```
The configuration is located in `src/resources/application.properties`.
