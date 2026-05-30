package ru.angelika.boutique.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.InitializingBean;

@Configuration
public class AppService implements InitializingBean {

    @Value("${app.name}")
    private String appName;

    @Value("${app.version}")
    private String appVersion;

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("--- Информация о приложении ---");
        System.out.println("--- Имя: " + appName);
        System.out.println("--- Версия: " + appVersion);
    }
}