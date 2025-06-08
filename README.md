# 書籍管理システムのバックエンドAPI

書籍と著者を管理するAPIです。レイヤードアーキテクチャを採用しています。

## 🛠 使用技術スタック

- Kotlin
- Spring Boot
- JOOQ
- Flyway
- PostgreSQL (Docker)
- Gradle

## 📦 セットアップ手順

1. **PostgreSQLコンテナ起動**
docker-compose up -d

2. **Flywayでマイグレーション実行**
./gradlew flywayMigrate

3. **JOOQコード自動生成**
./gradlew generateJooq

4. **アプリ起動**
./gradlew bootRun