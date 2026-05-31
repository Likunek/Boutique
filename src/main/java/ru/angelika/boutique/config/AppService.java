package ru.angelika.boutique.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.InitializingBean;

/**
 * Конфигурационный класс, который при запуске приложения выводит в консоль
 * информацию о названии и версии приложения, загруженные из property-файлов.
 */
@Configuration
public class AppService implements InitializingBean {

    /** Название приложения, читается из свойства ${app.name}. */
    @Value("${app.name}")
    private String appName;

    /** Версия приложения, читается из свойства ${app.version}. */
    @Value("${app.version}")
    private String appVersion;

    /**
     * Выполняется после того, как все свойства внедрены.
     * Выводит в консоль информацию о приложении.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("--- Информация о приложении ---");
        System.out.println("--- Имя: " + appName);
        System.out.println("--- Версия: " + appVersion);
    }
}