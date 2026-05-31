# 🛍️ Boutique

Интернет-магазин с ролями: покупатель (**USER**), продавец (**SELLER**) и администратор (**ADMIN**).

## ⚙️ Технологии

- **Java 21.0.10**
- **Spring Boot 3.5.12** (Web, Security, Data JPA, Validation, Actuator)
- **Spring Security** (BCrypt)
- **Thymeleaf**
- **PostgreSQL**, H2 (тесты)
- **Maven**
- **Docker**, Docker Compose
- **JavaMelody**, **OpenAPI (Swagger)**

## 🚀 Основные возможности

### 👤 Покупатель (USER)
- Регистрация по номеру телефона
- Просмотр каталога товарных карточек (поиск, пагинация)
- Корзина (добавление / удаление товаров)
- Оформление заказа с выбором пункта выдачи – деньги списываются с баланса
- История заказов, отзывы на купленные товары
- Редактирование профиля

### 🏪 Продавец (SELLER)
- Добавление товаров 
- Создание товарных карточек
- Отправка товаров на склады
- Просмотр остатков товаров на складах
- Редактирование / удаление своих товаров и карточек
- Редактирование профиля

### 🔧 Администратор (ADMIN)
- Управление пользователями и продавцами
- Верификация товаров
- Управление складами и пунктами выдачи (CRUD)
- Просмотр всех заказов, поставок, остатков
- Мониторинг через `/monitoring` и `/actuator`

### ⏱️ Автоматические поставки
Каждый день в **23:00** сервис `SupplyService`:
- собирает заказы со статусом `NEW`
- группирует их по пунктам выдачи
- проверяет наличие товаров на складах
- списывает остатки
- создаёт поставку и меняет статус заказов на `WAY`

При получении заказа (`RECEIVED`) товары добавляются в историю покупок.

## 🔐 Права доступа (Spring Security)

| URL-шаблон                                      | Доступ                              |
|------------------------------------------------|-------------------------------------|
| `/login`, `/registration` | Открытый                           |
| `/welcome``/public-seller/**`, `/item-card/**`, `/access-denied | Требуется аутентификация           |
| `/user/**`                        | Только **USER**                    |
|  `/users/**`                      | **USER** и **ADMIN**               |
| `/seller/**`                      | Только **USER**                    |
| `/sellers/**`, `/items/**`, `/cards/**`, `/send-to-storage/**` | **SELLER** и **ADMIN**             |
| `/admin/**`, `/swagger-ui/**`, `/v3/api-docs/**`, `/monitoring/**`, /actuator/** | Только **ADMIN**                   |

## 🛠️ Запуск через Docker Compose
Выполнить команду в git Bash:  
 git clone https://github.com/Likunek/Boutique.git
 перейти в корень проекта 
# В корне проекта выполните:
        docker compose up --build
#Приложение будет доступно на http://localhost:8080.
#Остановка:
             docker compose down
#Удаление тома с БД:
             docker compose down -v
#Чтобы зайти в приложение, необходимо зарегистрироваться (роль USER И SELLER) и позже войти по своему логину и паролю 
Для роли ADMIN, необходимо снять ограничения в файле ru.angelika.boutique.config.SpringSecurityConfig, изменив метод filterChain. 
Замените метода на : 
@Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((authz) -> authz
                        .anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }
запустите приложение и перейдите по пути: http://localhost:8080/swagger-ui/index.html
Отправьте данные на post authentication-entity-controller
нажмите Try it out
вставьте данные без id: 
{
  "number": "ваш номер",
  "password": "ваш пароль",
  "role": "ADMIN"
}
Нажмите Execute 
Вам должен прийти код 201, значит данные успешно сохранились.
Поменяйте обратно метод filterChain, запускайте приложение и авторизируйтесь под ролью администратора.
