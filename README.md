# Bank Account Management API

## 概要

このアプリケーションは、銀行口座の開設・入出金・照会・解約などを行う RESTful API
です。管理者は口座一覧の取得も可能です。

## 機能一覧

- 管理者用の口座一覧取得

- 新規口座開設（7桁の連番で自動生成）

- 残高照会

- 入金・出金処理

- 口座解約（残高ゼロのみ可能）

- 取引履歴の取得（口座別、取引種類別）

- ログイン / ログアウト機能

- Swagger UI による API ドキュメント表示

## 認証・ログイン機能

本APIは **Spring Security** によりセッション（`JSESSIONID`）ベースで認証します。  
ユーザーは **管理者** と **口座保有者** の2種類です。

### 役割（Role）

- `ROLE_ADMIN` … 管理者機能にアクセス可能（例：口座一覧取得 `/admin/**` など）
- `ROLE_ACCOUNT_USER` … 口座保有者向け機能にアクセス可能

## 課題（実務を想定した考慮点）

今回は学習目的のためシンプルな実装にしていますが、
実際には以下のような課題が存在することを認識しています。

- 複数リクエスト同時処理に伴う排他制御（悲観的ロック）の必要性
- 処理中の失敗や例外発生時に備えたトランザクション管理
- 入出金の優先順位や整合性の担保
- データベースへのアクセス負荷を考慮したスケーラビリティ対応

## 開発環境

- Java 17

- Spring Boot 3.5.0

- Spring Web / Validation / Data JPA

- MySQL

- Gradle

- IntelliJ IDEA(開発IDE)

- JUnit + Mockito によるユニットテスト

- SpringDoc OpenAPI によるドキュメント生成

## 動作確認方法

1. MySQL を起動し、テーブルを準備する
2. `application.yml` に適切な DB 接続設定を記載する
3. プロジェクトをビルド・起動する

   ```bash
   ./gradlew bootRun
4. Swagger UI にアクセス：

http://localhost:8080/swagger-ui/index.html

5. アプリ起動後はまず「管理者登録」または「口座開設」を行ってください。  
   その後、登録した情報を使ってログインしてください。

## 動作イメージ

https://github.com/user-attachments/assets/5076a3a5-9ce4-4103-adb9-caa010919526


![口座開設→ログイン](https://github.com/user-attachments/assets/5076a3a5-9ce4-4103-adb9-caa010919526)

![入金・出金](https://github.com/user-attachments/assets/f92a6172-c15b-4c3a-b4ac-f5c6e24a80ab)

![取引履歴](https://github.com/user-attachments/assets/27a104d7-1242-4804-b31e-b4059de4664d)


## APIエンドポイント一覧

| HTTPメソッド | エンドポイント                         | 概要                      |
|----------|---------------------------------|-------------------------|
| POST     | `/login`                        | ログイン                    | |
| GET      | `/balance/{accountNumber}`      | 残高照会                    |
| POST     | `/deposit/{accountNumber}`      | 入金処理                    |
| POST     | `/withdraw/{accountNumber}`     | 出金処理                    |
| POST     | `/closeAccount/{accountNumber}` | 口座解約                    |
| GET      | `/accountLog/{accountNumber}`   | 指定口座の取引履歴取得（任意で取引種類指定可）|
| POST     | `/logout`                       | ログアウト                   |

## テスト

- サービス層、コントローラ層のユニットテストを実装済

- @Mock を使用して Repository をモック

## テスト実行

./gradlew test

## 工夫した点

- 残高表示は数値ではなくカンマを含んだ文字列にしたこと。
  銀行業務において金額にカンマがないことは考えられないため、文字列で返す仕様にしました。

- 残高不足、残高有りのまま解約等できないよう設計したこと。
  トランザクション管理に不足はあるものの、最低限矛盾が生じないようにしました。テストも重点的に作成しています。
 
 

  
