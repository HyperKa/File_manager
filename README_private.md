# File Manager

Модульный Spring Boot backend для хранения файлов.

## Архитектура

```text
React / Vite
    |
    v
Spring Boot API
    |
    |-- PostgreSQL / Neon -- метаданные файлов, владелец, статусы
    |
    |-- S3-compatible storage -- содержимое файлов
          MinIO локально
          Backblaze B2 в production
```

Backend разделён на:

- `file/domain` — модель файла, статусы и порты;
- `file/application` — сценарии загрузки, скачивания, удаления и списка файлов;
- `file/infrastructure` — JPA и S3-адаптеры;
- `file/api` — REST DTO и обработка ошибок;
- `security` — stateless API-token authentication и CORS.

## Локальный запуск

### 1. Поднять PostgreSQL и MinIO

```bash
docker compose up postgres minio
```

### 2. Запустить backend

```bash
mvn -pl backend spring-boot:run
```

По умолчанию используется профиль `local`.

Для локальной разработки можно задать токен:

```bash
API_TOKEN=local-dev-token mvn -pl backend spring-boot:run
```

## Backblaze B2

Создайте bucket в Backblaze B2 и создайте Application Key с правами только на этот bucket.

Для production-профиля задайте переменные:

```env
SPRING_PROFILES_ACTIVE=prod

DB_URL=jdbc:postgresql://host:5432/database
DB_USERNAME=...
DB_PASSWORD=...

B2_S3_ENDPOINT=https://s3.<region>.backblazeb2.com
B2_BUCKET=your-bucket
B2_REGION=auto
B2_ACCESS_KEY_ID=...
B2_APPLICATION_KEY=...

API_TOKEN=...
CORS_ALLOWED_ORIGINS=https://your-frontend.example
```

B2 подключается через S3-compatible API, поэтому доменное ядро приложения не зависит от Backblaze.

## API

Все файловые endpoints требуют заголовок:

```http
Authorization: Bearer <API_TOKEN>
```

### Загрузить файл

```http
POST /api/v1/files
Content-Type: multipart/form-data

file: <binary>
```

Ответ: `201 Created`.

### Список файлов

```http
GET /api/v1/files?page=0&size=50
```

### Скачать файл

```http
GET /api/v1/files/{fileId}/content
```

### Удалить файл

```http
DELETE /api/v1/files/{fileId}
```

Ответ: `204 No Content`.

## Состояния файла

```text
PENDING -> READY
PENDING -> FAILED

READY -> DELETING -> DELETED
```

Object storage и PostgreSQL не имеют общей транзакции. Поэтому загрузка сначала создаёт metadata со статусом `PENDING`, затем пишет объект, после чего переводит запись в `READY`. При ошибке запись переводится в `FAILED`.

Для production рекомендуется добавить background reconciler, который:

- удаляет старые `PENDING` и `FAILED` объекты;
- исправляет зависшие `DELETING` записи;
- проверяет, что объект действительно существует.

## Безопасность

- Bucket должен быть private.
- Не коммитить `.env`, ключи и runtime-данные.
- Не использовать master key облачного аккаунта.
- Для production заменить demo API-token на OIDC/JWT или другой полноценный auth provider.
- Не использовать `@CrossOrigin("*")`.
- Для больших файлов лучше выдавать browser short-lived presigned URL, а не проксировать файл через backend.

## Миграции БД

Схема управляется Liquibase:

```text
backend/src/main/resources/db/changelog/db.changelog.sql
```

Hibernate не создаёт схему автоматически:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Если в базе уже есть таблица, созданная старым `ddl-auto=update`, перед подключением Liquibase нужно сделать baseline существующей схемы.

## Что не входит в текущий MVP

- полноценный OAuth2/OIDC login;
- presigned multipart upload;
- antivirus scanning;
- CDN;
- background reconciler;
- audit log;
- rate limiting;
- пользовательские quotas.

Эти функции можно добавлять поверх текущих портов без переписывания domain/application слоя.
