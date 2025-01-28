# ShareIt

**ShareIt** — это приложение для обмена вещами между пользователями. Оно позволяет пользователям создавать запросы на аренду вещей, бронировать их, оставлять отзывы и управлять своими вещами.

---

## Основные функции

- **Управление пользователями**: создание, обновление, удаление и получение информации о пользователях.
- **Управление вещами**: добавление, обновление, удаление и поиск вещей.
- **Бронирование вещей**: создание, подтверждение и отмена бронирования.
- **Запросы на аренду**: создание и просмотр запросов на аренду вещей.
- **Отзывы**: возможность оставлять отзывы на арендованные вещи.

---

## Технологии

- **Java 21**
- **Spring Boot**
- **RestTemplate** для взаимодействия с основным сервисом
- **Maven** для сборки проекта
- **Логирование** с использованием Slf4j

---

## API

### 1. **Пользователи (`/users`)**
- **Создание пользователя**:  
  `POST /users`  
  Пример тела запроса:
  ```json
  {
    "name": "John Doe",
    "email": "john.doe@example.com"
  }
  ```

- **Получение пользователя по ID**:  
  `GET /users/{user-id}`

- **Получение всех пользователей**:  
  `GET /users`

- **Обновление пользователя**:  
  `PATCH /users/{user-id}`  
  Пример тела запроса:
  ```json
  {
    "name": "Jane Doe"
  }
  ```

- **Удаление пользователя**:  
  `DELETE /users/{user-id}`

---

### 2. **Вещи (`/items`)**
- **Создание вещи**:  
  `POST /items`  
  Пример тела запроса:
  ```json
  {
    "name": "Дрель",
    "description": "Мощная дрель для ремонта",
    "available": true
  }
  ```

- **Получение вещи по ID**:  
  `GET /items/{item-id}`

- **Получение всех вещей пользователя**:  
  `GET /items`

- **Обновление вещи**:  
  `PATCH /items/{item-id}`  
  Пример тела запроса:
  ```json
  {
    "description": "Мощная дрель с аккумулятором"
  }
  ```

- **Удаление вещи**:  
  `DELETE /items/{item-id}`

- **Поиск вещей**:  
  `GET /items/search?text={query}`

- **Добавление отзыва**:  
  `POST /items/{item-id}/comment`  
  Пример тела запроса:
  ```json
  {
    "text": "Отличная дрель, все работает!"
  }
  ```

---

### 3. **Бронирование (`/bookings`)**
- **Создание бронирования**:  
  `POST /bookings`  
  Пример тела запроса:
  ```json
  {
    "itemId": 1,
    "start": "2023-10-10T10:00:00",
    "end": "2023-10-15T10:00:00"
  }
  ```

- **Подтверждение бронирования**:  
  `PATCH /bookings/{booking-id}?approved=true`

- **Получение бронирования по ID**:  
  `GET /bookings/{booking-id}`

- **Получение всех бронирований пользователя**:  
  `GET /bookings?state={state}`

- **Получение бронирований владельца**:  
  `GET /bookings/owner?state={state}`

---

### 4. **Запросы на аренду (`/requests`)**
- **Создание запроса**:  
  `POST /requests`  
  Пример тела запроса:
  ```json
  {
    "description": "Нужна дрель для ремонта"
  }
  ```

- **Получение запроса по ID**:  
  `GET /requests/{request-id}`

- **Получение всех запросов**:  
  `GET /requests/all`

- **Получение собственных запросов**:  
  `GET /requests`

---

## Запуск приложения

## Запуск с использованием Docker

### 1. **Запуск с использованием Docker Compose**

1. Убедитесь, что у вас установлены:
   - Docker
   - Docker Compose

2. Клонируйте репозиторий:
   ```bash
   git clone https://github.com/your-repo/shareit.git
   cd shareit
   ```

3. Запустите приложение с помощью Docker Compose:
   ```bash
   docker-compose up --build
   ```

4. После запуска приложение будет доступно:
  - **Gateway**: `http://localhost:8080`
  - **Server**: `http://localhost:9090`

5. Остановка приложения:
   ```bash
   docker-compose down
   ```

---

### 2. **Конфигурация Docker Compose**

Конфигурация `docker-compose.yml` включает следующие сервисы:

- **PostgreSQL**: База данных для хранения данных приложения.
- **Gateway**: Шлюз для обработки запросов и взаимодействия с основным сервисом.
- **Server**: Основной сервис приложения.

Пример конфигурации `docker-compose.yml`:
```yaml
version: '3.8'

services:
  db:
    image: postgres:16.1
    container_name: postgres
    ports:
      - "6541:5432"
    environment:
      - POSTGRES_PASSWORD=shareit
      - POSTGRES_USER=shareit
      - POSTGRES_DB=shareit
    volumes:
      - ./server/src/main/resources/schema.sql:/docker-entrypoint-initdb.d/initDB.sql
    healthcheck:
      test: pg_isready -q -d $$POSTGRES_DB -U $$POSTGRES_USER
      timeout: 5s
      interval: 5s
      retries: 10

  gateway:
    build: gateway
    image: shareit-gateway
    container_name: shareit-gateway
    ports:
      - "8080:8080"
    depends_on:
      - server
    environment:
      - SHAREIT_SERVER_URL=http://server:9090

  server:
    build: server
    image: shareit-server
    container_name: shareit-server
    ports:
      - "9090:9090"
    depends_on:
      db:
        condition: service_healthy
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/shareit
      - SPRING_DATASOURCE_USERNAME=shareit
      - SPRING_DATASOURCE_PASSWORD=shareit
    volumes:
      - ./server/src/main/resources/application.properties:/app/application.properties
```

---

### 3. **Сборка и запуск отдельных контейнеров**

Если вы хотите собрать и запустить контейнеры вручную:

1. Соберите образы:
   ```bash
   docker build -t shareit-gateway -f gateway/Dockerfile .
   docker build -t shareit-server -f server/Dockerfile .
   ```

2. Запустите контейнеры:
   ```bash
   docker run -d --name postgres -p 6541:5432 \
     -e POSTGRES_PASSWORD=shareit \
     -e POSTGRES_USER=shareit \
     -e POSTGRES_DB=shareit \
     postgres:16.1

   docker run -d --name shareit-server -p 9090:9090 \
     -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/shareit \
     -e SPRING_DATASOURCE_USERNAME=shareit \
     -e SPRING_DATASOURCE_PASSWORD=shareit \
     shareit-server

   docker run -d --name shareit-gateway -p 8080:8080 \
     -e SHAREIT_SERVER_URL=http://server:9090 \
     shareit-gateway
   ```

3. Остановка контейнеров:
   ```bash
   docker stop shareit-gateway shareit-server postgres
   docker rm shareit-gateway shareit-server postgres
   ```

---

## Запуск с использованием Maven
1. Убедитесь, что у вас установлены:
    - Java 21
    - Maven

2. Клонируйте репозиторий:
   ```bash
   git clone https://github.com/yiqes/java-shareit
   cd java-shareit
   ```

3. Соберите проект:
   ```bash
   mvn clean install
   ```

4. Запустите приложение:
   ```bash
   mvn spring-boot:run
   ```

5. Приложение будет доступно по адресу:  
   `http://localhost:8080`

---

## Примеры использования

### Создание пользователя
```bash
curl -X POST -H "Content-Type: application/json" -d '{"name": "John Doe", "email": "john.doe@example.com"}' http://localhost:8080/users
```

### Создание вещи
```bash
curl -X POST -H "Content-Type: application/json" -H "X-Sharer-User-Id: 1" -d '{"name": "Дрель", "description": "Мощная дрель", "available": true}' http://localhost:8080/items
```

### Создание бронирования
```bash
curl -X POST -H "Content-Type: application/json" -H "X-Sharer-User-Id: 2" -d '{"itemId": 1, "start": "2023-10-10T10:00:00", "end": "2023-10-15T10:00:00"}' http://localhost:8080/bookings
```

---

## Логирование

Логирование настроено с использованием Slf4j. Логи можно найти в консоли или в файле `logs/app.log`.

---
