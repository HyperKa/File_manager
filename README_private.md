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

## Сохранение и использование бекапов:

> ⚠️ **КРИТИЧЕСКИ ВАЖНО для Windows/PowerShell:**  
> Никогда не используйте оператор перенаправления `>` напрямую в PowerShell для сохранения вывода `pg_dump` (например: `docker exec ... > backup.sql`).  
> **Почему:** PowerShell по умолчанию сохраняет файлы в кодировке UTF-16 или UTF-8 с BOM (Byte Order Mark). PostgreSQL строго ожидает чистый UTF-8. При попытке восстановить такой файл вы получите фатальную ошибку: `invalid byte sequence for encoding "UTF8": 0xff`.  
> **Решение:** Создавать файл дампа **внутри** Linux-контейнера, а затем копировать его на хост-машину.

---

### 📥 Этап 1: Создание резервной копии (Export)

Выполните эти две команды по очереди в терминале (находясь в корне проекта):

```bash
# 1. Создаем дамп внутри контейнера
docker exec file-manager-postgres sh -c "pg_dump -U file_manager file_manager > /tmp/backup.sql"

# 2. Копируем файл из контейнера на локальный компьютер
docker cp file-manager-postgres:/tmp/backup.sql ./backup.sql
```

#### 🔍 Разбор аргументов:
* `docker exec` — выполняет команду внутри запущенного Docker-контейнера.
* `file-manager-postgres` — имя целевого контейнера (указано в `docker-compose.yml`).
* `sh -c "..."` — запускает оболочку (shell) внутри контейнера и передает ей строку команды. **Это ключевой момент:** он заставляет оператор `>` работать внутри Linux, а не в Windows PowerShell, гарантируя чистую кодировку UTF-8.
* `pg_dump` — стандартная утилита PostgreSQL для логического резервного копирования базы данных.
* `-U file_manager` — (`--username`) указывает имя пользователя базы данных для аутентификации.
* `file_manager` (второй раз) — имя самой базы данных, которую нужно скопировать.
* `> /tmp/backup.sql` — оператор перенаправления вывода Linux, который сохраняет результат работы `pg_dump` во временный файл внутри файловой системы контейнера.
* `docker cp` — утилита Docker для копирования файлов между контейнером и хост-машиной.
* `file-manager-postgres:/tmp/backup.sql` — путь-источник (формат: `имя_контейнера:путь_внутри`).
* `./backup.sql` — путь-назначение на вашем компьютере (текущая директория).

---

### 📤 Этап 2: Восстановление резервной копии (Import)

> 💡 **Совет:** Для безопасного тестирования рекомендуется сначала создать пустую тестовую базу:  
> `docker exec -it file-manager-postgres psql -U file_manager -c "CREATE DATABASE file_manager_test;"`  
> Затем замените имя базы в команде ниже на `file_manager_test`.

```bash
# 1. Копируем файл бэкапа с компьютера обратно в контейнер
docker cp ./backup.sql file-manager-postgres:/tmp/backup.sql

# 2. Восстанавливаем данные из файла внутри контейнера
docker exec -it file-manager-postgres psql -U file_manager -d file_manager -f /tmp/backup.sql
```

#### 🔍 Разбор аргументов:
* `docker cp` — копирование файлов (в обратном направлении: с хоста в контейнер).
* `./backup.sql` — путь-источник на вашем компьютере.
* `file-manager-postgres:/tmp/backup.sql` — путь-назначение внутри контейнера.
* `docker exec -it` — выполнение команды. Флаги `-it` (`--interactive` + `--tty`) выделяют псевдо-терминал и оставляют `stdin` открытым, что позволяет `psql` корректно отображать прогресс и ошибки.
* `psql` — интерактивный терминал PostgreSQL.
* `-U file_manager` — имя пользователя для подключения.
* `-d file_manager` — (`--dbname`) имя целевой базы данных, в которую будут загружены данные (замените на `file_manager_test`, если восстанавливаете в тестовую среду).
* `-f /tmp/backup.sql` — (`--file`) указывает `psql` прочитать и выполнить SQL-команды из указанного файла, а не ждать ручного ввода пользователя.

---

### 🧹 Этап 3: Очистка временных файлов (Рекомендуется)

После успешного восстановления временные файлы внутри контейнера можно удалить, чтобы не засорять дисковое пространство:

```bash
docker exec file-manager-postgres rm /tmp/backup.sql
```
* `rm` — стандартная команда Linux для удаления файлов.

---

### ⚡ Шпаргалка для быстрого копирования (Local Dev)

Если вам нужно быстро сделать бэкап и восстановить его в ту же базу во время разработки:

```bash
# 0. Просмотр текущих файлов в tmp/
docker exec -it file-manager-postgres ls -lh /tmp/

# 1. Бэкап
docker exec file-manager-postgres sh -c "pg_dump -U file_manager file_manager > /tmp/b.sql"
docker cp file-manager-postgres:/tmp/b.sql ./backup.sql

# 2. Восстановление
docker cp ./backup.sql file-manager-postgres:/tmp/b.sql
docker exec -it file-manager-postgres psql -U file_manager -d file_manager -f /tmp/b.sql

# 3. Очистка
docker exec file-manager-postgres rm /tmp/b.sql
```

### Важно:
```
Get-Content .\backend\src\main\resources\db\changelog\backups\backup_clean.sql
```
Это команда для просмотра содержимого бекапа, тут в \backups\ они и будут расположены. 


### Полный алгоритм выгрузки данных из БД (бекап), внесения изменений в схему и загрузки данных в новую БД:

### 1. Выгрузка дампа
```bash
# 0. Просмотр текущих файлов в tmp/
docker exec -it file-manager-postgres ls -lh /tmp/

# 1. Создаем дамп ТОЛЬКО данных (-a или --data-only) ВНУТРИ контейнера (избегаем проблем с кодировкой PowerShell)
docker exec file-manager-postgres sh -c "pg_dump -a -U file_manager file_manager > /tmp/data_only_backup.sql"

# 2. Копируем файл на твой компьютер с меткой времени
docker cp file-manager-postgres:/tmp/data_only_backup.sql ./data_backup_$(Get-Date -Format 'yyyyMMdd_HHmmss').sql

# 3. Очищаем временный файл в контейнере (гигиена)
docker exec file-manager-postgres rm /tmp/data_only_backup.sql
```

### 2. Внесение изменений ВСЕГДА в новый файл миграции, с добавлением его в changelog-master.yaml
### 3. Применение миграции и проверка:

```bash
# 1. Применяем изменения
mvn liquibase:update "-Dliquibase.changeLogFile=src/main/resources/db/changelog/changelog-master.yaml" "-Dliquibase.url=jdbc:postgresql://localhost:5432/file_manager" "-Dliquibase.username=file_manager" "-Dliquibase.password=file_manager" "-Dliquibase.driver=org.postgresql.Driver"

# 2. Проверяем логи: ищем "Update has been successful" и сами данные:
docker exec -it file-manager-postgres psql -U file_manager -d file_manager -c "\d file_metadata"
```

### 4. Загрузка данных в новую схему

```bash
# 1. Копируем файл бэкапа ВНУТРЬ контейнера
docker cp .\data_backup_20261103_184500.sql file-manager-postgres:/tmp/restore.sql

# 2. Восстанавливаем данные. 
# Так как это ТОЛЬКО данные (COPY), PostgreSQL просто вставит их в существующую таблицу.
# Индексы автоматически обновятся, а CHECK-ограничения проверят данные на валидность.
docker exec -it file-manager-postgres psql -U file_manager -d file_manager -f /tmp/restore.sql

# 3. Удаляем временный файл
docker exec file-manager-postgres rm /tmp/restore.sql
```

### 5. При возникновении критической ситуации:

```bash
Откат схемы на 1 коммит:
mvn liquibase:rollback "-Dliquibase.changeLogFile=src/main/resources/db/changelog/changelog-master.yaml" 
"-Dliquibase.rollbackCount=1" "-Dliquibase.url=jdbc:postgresql://localhost:5432/file_manager" "-Dliquibase.username=file_manager" 
"-Dliquibase.password=file_manager" "-Dliquibase.driver=org.postgresql.Driver"
```