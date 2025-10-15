# Cognito OIDC Spring Boot アプリ

Amazon Cognito を OIDC の認可コードフローで利用し、Spring Boot アプリからユーザ情報（ID トークン／アクセストークン）を確認するサンプルです。アクセストークンを用いて AWS SDK for Java (v2) の `GetUser` API を呼び出し、Cognito ユーザ属性を取得します。

## プロジェクト構成

- `pom.xml` : Spring Boot 3.3 / Java 22 対応の Maven 設定（AWS SDK v2 を利用）
- `CognitoClientConfig` : AWS リージョンを元に `CognitoIdentityProviderClient` を Bean として生成
- `CognitoUserService` : ログイン済みユーザのアクセストークンで `GetUser` を呼び出し属性を取得
- `UserController` / `templates/*.html` : ログインとユーザ情報表示 UI

## 前提条件

- Amazon Cognito User Pool（ホストドメイン発行済み）
- User Pool に Web アプリクライアント（認可コードグラント、client secret 有り or なし）
- コールバック URL に `http://localhost:8080/login/oauth2/code/cognito` を登録
- `openid`, `profile`, `email` のスコープを許可
- Java 22 (Amazon Corretto 22 など)
- Maven 3.9+（`mvn` コマンドが利用可能なこと）

## 設定

環境変数または `application.yml` を編集し、以下の値を設定します（値をセットしない場合はプレースホルダのままでもアプリは起動しますが、実際のログイン処理は行えません）。

```bash
export COGNITO_CLIENT_ID=xxxxxxxxxxxxxxxxxxxx
export COGNITO_CLIENT_SECRET=yyyyyyyyyyyyyyyyyyyy
export COGNITO_USER_POOL_ID=ap-northeast-1_XXXXXXXXX
export COGNITO_DOMAIN=https://your-domain.auth.ap-northeast-1.amazoncognito.com
export AWS_REGION=ap-northeast-1  # AWS SDK を利用するリージョン
```

- それぞれの URI を個別に上書きしたい場合は `COGNITO_AUTHORIZATION_URI` / `COGNITO_TOKEN_URI` / `COGNITO_USER_INFO_URI` / `COGNITO_JWK_SET_URI` を指定してください。
- User Pool ID から自動生成される JWK 取得 URL は `https://cognito-idp.{region}.amazonaws.com/{userPoolId}/.well-known/jwks.json` です。

## 実行方法

```bash
mvn spring-boot:run
```

ブラウザで <http://localhost:8080/> を開き、`サインイン` ボタンをクリックして Cognito のログイン画面に遷移します。ログイン後は `/me` 画面で AWS SDK 経由で取得したユーザ属性を確認できます。

## テスト

```bash
mvn test
```

## Moto サーバーモードでのローカル検証（制限あり）

`CognitoUserService` では AWS SDK の `GetUser` API を呼び出しているため、moto で Cognito IDP エンドポイントをエミュレートし、SDK レベルの動作確認をローカルで行うことができます。ただし moto は Cognito Hosted UI や OIDC トークンエンドポイントを実装していないため、ブラウザによるログイン～リダイレクトは再現できません（OIDC の部分は本番環境で確認してください）。

1. moto をインストールし、Cognito のサーバーモードを起動します。
   ```bash
   pip install "moto[server]"
   AWS_DEFAULT_REGION=us-east-1 moto_server cognito-idp -H 0.0.0.0 -p 5000
   ```
2. 別ターミナルでダミー認証情報とエンドポイントを設定し、必要な User Pool / ユーザを CLI などで作成します（moto は `aws` CLI からの操作をサポートしています）。
   ```bash
   export AWS_ACCESS_KEY_ID=dummy
   export AWS_SECRET_ACCESS_KEY=dummy
   export AWS_REGION=us-east-1
   export COGNITO_ENDPOINT_OVERRIDE=http://localhost:5000
   aws cognito-idp create-user-pool --pool-name local-pool --endpoint-url "$COGNITO_ENDPOINT_OVERRIDE"
   # 必要に応じてユーザやアプリクライアントを作成
   ```
3. アプリケーションを起動すると、`CognitoIdentityProviderClient` が `COGNITO_ENDPOINT_OVERRIDE` を読み取り、moto 側へ `GetUser` を発行します。OIDC でのログインは実施できないため、`/me` 画面の表示テストは Spring Security のモックや RestAssured などでアクセストークンを差し込む形で行ってください。

## 補足

- AWS SDK は環境変数や `~/.aws/credentials` など標準の認証情報プロバイダーチェーンを利用します。`GetUser` API のみを呼び出すため、通常は追加の IAM 権限は不要ですが、必要に応じて `cognito-idp:GetUser` を許可してください。
- ローカルで moto を利用する場合は `COGNITO_ENDPOINT_OVERRIDE` でエンドポイントを上書きしてください。
- Cognito ドメインやクライアント ID/シークレットを未設定の状態でもアプリは起動できますが、その場合ログインリンクは利用できません。
- プロダクションで利用する際は HTTPS・セッション管理・CSRF 等のセキュリティ強化を検討してください。
