# Bank Account Management API

## 概要

このアプリケーションは、銀行口座の開設・入出金・照会・解約などを行う RESTful API
です。管理者は口座一覧や取引履歴の取得も可能です。

## 主な機能

口座の開設（7桁の連番で自動生成）

口座情報の照会

入金・出金処理

口座解約（残高ゼロのみ可能）

取引履歴の取得（口座別、取引種類別）

Swagger UI による API ドキュメント表示

## 技術スタック

Java 17

Spring Boot 3.5.0

Spring Web / Validation / Data JPA

MySQL

JUnit + Mockito によるユニットテスト

SpringDoc OpenAPI によるドキュメント生成

## 動作確認方法

MySQL 起動＆テーブル準備

application.yml に適切な DB 接続設定を記載

プロジェクトをビルド・起動

./gradlew bootRun

Swagger UI にアクセス：

http://localhost:8080/swagger-ui/index.html

## APIエンドポイント一覧

| HTTPメソッド | エンドポイント                                | 概要                     |
|----------|----------------------------------------|------------------------|
| GET      | `/accountsForAdmin`                    | 管理者用の口座一覧取得            |
| POST     | `/createAccount`                       | 新規口座開設                 |
| GET      | `/account/{accountNumber}`             | 残高照会                   |
| POST     | `/deposit/{accountNumber}`             | 入金処理                   |
| POST     | `/withdraw/{accountNumber}`            | 出金処理                   |
| POST     | `/closeAccount/{accountNumber}`        | 口座の解約                  |
| GET      | `/allTransactions`                     | 全取引履歴の取得               |
| GET      | `/accountTransactions/{accountNumber}` | 指定口座の取引履歴取得（任意でタイプ指定可） |

## テスト

サービス層、コントローラ層のユニットテストを実装済

@Mock を使用して Repository をモック

## テスト実行

./gradlew test

## 注意事項

取引情報は Transaction エンティティに記録されます

口座番号は 0000001 からの7桁連番で自動生成

## 今後の課題

認証／認可（例：Spring Security）

DB統合テスト（TestContainers など）

