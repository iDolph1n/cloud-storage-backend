# Дипломная работа «Облачное хранилище» — BACKEND

REST-сервис для фронтенда [netology-diplom-frontend](https://github.com/netology-code/jd-homeworks/tree/master/diploma/netology-diplom-frontend).
Реализует все методы, описанные в [YAML-спецификации](https://github.com/netology-code/jd-homeworks/blob/master/diploma/CloudServiceSpecification.yaml):
авторизацию, загрузку/скачивание/переименование/удаление файлов и вывод списка файлов.
Авторизация выполняется через заголовок `auth-token`.

---

## Технологии

| Категория | Стек |
|---|---|
| Язык / фреймворк | Java 17, Spring Boot |
| Web / Security | Spring Web, Spring Validation, кастомный фильтр `auth-token` |
| Данные | PostgreSQL, Spring Data JPA, Flyway |
| Тестирование | JUnit 5, Mockito (unit), Testcontainers + MockMvc (интеграция) |
| Инфраструктура | Docker, docker-compose, Maven |

---

## Архитектура приложения

```
┌──────────────┐       ┌──────────────────────────────────────────┐
│   FRONT      │       │                BACKEND                   │
│  (Vue.js)    │──────>│                                          │
│  :8080/8081  │ HTTP  │  ┌─────────────────────────────────────┐ │
└──────────────┘       │  │  AuthTokenFilter                    │ │
                       │  │  (проверка auth-token в заголовке)  │ │
                       │  └────────────┬────────────────────────┘ │
                       │               │                          │
                       │  ┌────────────v────────────────────────┐ │
                       │  │  Controllers                        │ │
                       │  │  AuthController  FileController     │ │
                       │  └────────────┬────────────────────────┘ │
                       │               │                          │
                       │  ┌────────────v────────────────────────┐ │
                       │  │  Services                           │ │
                       │  │  AuthService  FileService           │ │
                       │  │  TokenService                       │ │
                       │  └──┬─────────────────────┬────────────┘ │
                       │     │                     │              │
                       │  ┌──v──────────┐  ┌───────v───────────┐  │
                       │  │ Repositories│  │ FileStorage        │  │
                       │  │ (JPA)       │  │ (LocalFileStorage) │  │
                       │  └──┬──────────┘  └───────┬───────────┘  │
                       │     │                     │              │
                       └─────┼─────────────────────┼──────────────┘
                             │                     │
                       ┌─────v─────┐        ┌──────v──────┐
                       │ PostgreSQL│        │  Файловая   │
                       │  (users,  │        │  система /  │
                       │  tokens,  │        │  volume     │
                       │  files)   │        └─────────────┘
                       └───────────┘
```

### Пакеты проекта

```
ru.netology.cloudstorage
├── api/                    — контроллеры и DTO
│   ├── dto/                — LoginRequest, LoginResponse, FileInfoResponse, RenameFileRequest, ErrorResponse
│   ├── AuthController      — POST /login, POST /logout
│   ├── FileController      — GET /list, POST/GET/PUT/DELETE /file
│   └── ApiExceptionHandler — единый @RestControllerAdvice
├── bootstrap/              — BootstrapUsers (создание тестового пользователя при старте)
├── config/                 — AppProperties, SecurityConfig, WebCorsConfig
├── domain/                 — JPA-сущности: UserEntity, TokenEntity, FileEntity
├── exception/              — ApiException, BadRequestException, ConflictException, NotFoundException, UnauthorizedException, StorageException
├── repo/                   — Spring Data JPA: UserRepository, TokenRepository, FileRepository
├── security/               — AuthTokenFilter, AuthenticatedUser
├── service/                — AuthService, FileService, TokenService
├── storage/                — FileStorage (интерфейс), LocalFileStorage (реализация)
├── util/                   — FilenameSanitizer
└── CloudStorageApplication — точка входа
```

---

## Структура базы данных

Схема создаётся Flyway-миграцией `V1__init.sql`.

### Таблица `users`

| Колонка | Тип | Описание |
|---|---|---|
| `id` | `bigserial PK` | Идентификатор |
| `login` | `varchar(255)` | Логин (уникальный) |
| `password_hash` | `varchar(255)` | Хеш пароля (BCrypt) |

### Таблица `tokens`

| Колонка | Тип | Описание |
|---|---|---|
| `token` | `varchar(255) PK` | Значение токена |
| `user_id` | `bigint FK` | Ссылка на `users.id` (cascade) |
| `created_at` | `timestamp` | Дата создания |
| `expires_at` | `timestamp` | Дата истечения |

### Таблица `files`

| Колонка | Тип | Описание |
|---|---|---|
| `id` | `bigserial PK` | Идентификатор |
| `user_id` | `bigint FK` | Ссылка на `users.id` (cascade) |
| `filename` | `varchar(255)` | Имя файла (уникально в связке с user) |
| `storage_key` | `varchar(255)` | Ключ файла в хранилище |
| `size_bytes` | `bigint` | Размер файла в байтах |
| `created_at` | `timestamp` | Дата загрузки |

Файлы хранятся на файловой системе (volume `/data/storage` в Docker), путь задаётся через `STORAGE_ROOT`.

---

## Быстрый старт (Docker)

```bash
docker-compose up -d --build
```

Бэкенд будет доступен по адресу: **http://localhost:8080**

---

## Запуск FRONT

1. Скачайте фронтенд:
   https://github.com/netology-code/jd-homeworks/tree/master/diploma/netology-diplom-frontend
2. В файле `.env` укажите URL бэкенда:
   ```
   VUE_APP_BASE_URL=http://localhost:8080
   ```
3. Установите зависимости и запустите:
   ```bash
   npm install
   npm run serve
   ```

---

## Тестовые пользователи

При старте приложения автоматически создаётся пользователь из `application.yml` (секция `app.bootstrap.users`):

| Логин | Пароль |
|---|---|
| `user` | `password` |

Изменить можно в `src/main/resources/application.yml` или через переменные окружения.

---

## API

Все эндпоинты (кроме `/login`) требуют заголовок `auth-token`.

### POST /login

Тело запроса:
```json
{
  "login": "user",
  "password": "password"
}
```

Ответ `200`:
```json
{
  "auth-token": "<token>"
}
```

### POST /logout

Заголовок: `auth-token: <token>`

Ответ: `200`

### GET /list?limit=N

Заголовок: `auth-token: <token>`

Ответ `200`:
```json
[
  { "filename": "a.txt", "size": 123 }
]
```

### POST /file?filename=a.txt

Заголовок: `auth-token: <token>`
Тело: `multipart/form-data`, part **file**

Ответ: `200`

### GET /file?filename=a.txt

Заголовок: `auth-token: <token>`

Ответ: `200` — бинарное содержимое файла

### PUT /file?filename=a.txt

Заголовок: `auth-token: <token>`
Тело:
```json
{
  "filename": "b.txt"
}
```

Ответ: `200`

### DELETE /file?filename=a.txt

Заголовок: `auth-token: <token>`

Ответ: `200`

### Коды ошибок

| Код | Описание |
|---|---|
| 400 | Ошибка входных данных |
| 401 | Неавторизован (токен отсутствует/невалиден) |
| 404 | Файл не найден |
| 409 | Файл с таким именем уже существует |
| 500 | Внутренняя ошибка сервера |

---

## Настройки

Все параметры читаются из `application.yml` и могут быть переопределены переменными окружения:

| Переменная | Описание | По умолчанию                                  |
|---|---|-----------------------------------------------|
| `SERVER_PORT` | Порт сервера | `8080`                                        |
| `DB_URL` | JDBC URL базы данных | `jdbc:postgresql://localhost:5439/cloud`      |
| `DB_USERNAME` | Пользователь БД | `cloud`                                       |
| `DB_PASSWORD` | Пароль БД | `cloud`                                       |
| `STORAGE_ROOT` | Директория хранения файлов | `.local-storage`                              |
| `CORS_ALLOWED_ORIGINS` | Разрешённые origins для CORS | `http://localhost:8080,http://localhost:8081` |
| `TOKEN_TTL` | Время жизни токена | `24h`                                         |

---

## Запуск тестов

```bash
mvn test
```

- **Unit-тесты** (`service/`): `AuthServiceTest`, `FileServiceTest` — JUnit 5 + Mockito
- **Интеграционные тесты** (`it/`): `CloudStorageIntegrationTest` — Testcontainers (PostgreSQL) + MockMvc

---

## Лицензия

Проект распространяется под лицензией [MIT](LICENSE.txt).

---

## Автор

Работу выполнил студент группы **JD-92** — **Михайлов Виталий**.
