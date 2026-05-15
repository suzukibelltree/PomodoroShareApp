# PomodoroShare — CLAUDE.md

## プロジェクト概要

人と一緒に作業することで集中力とモチベーションを上げる **ポモドーロタイマーアプリ**。
ユーザーが「部屋（スペース）」を作成または参加し、25分作業＋5分休憩のサイクルを共有する。

- **言語**: Kotlin
- **UI**: Jetpack Compose (Material3)
- **最小SDK**: 26 / ターゲットSDK: 36
- **versionName**: 1.8 / versionCode: 9

---

## アーキテクチャ

MVVM + Clean Architecture (単一モジュール `:app`)

```
com.belltree.pomodoroshareapp/
├── domain/
│   ├── models/          # ドメインモデル (User, Space, Record, Comment, DailySummary)
│   └── repository/      # Repository インターフェース & Impl
├── infra/
│   ├── api/             # OkHttp HTTP クライアント (API.kt)
│   └── datastore/       # DataStore (UserPreferences, IdHistoryStore)
├── auth/                # TokenManager (Firebase IDトークンをDataStoreに保存)
├── di/                  # Hilt モジュール (FirebaseModule, RepositoryModule, SupabaseModule, NotificationModule)
├── notification/        # 通知 & 正午アラーム (WorkManager + BroadcastReceiver)
├── ui/theme/            # Material3 テーマ・カラー・タイポグラフィ
├── ui/components/       # 共通コンポーネント (AppTopBar)
│
├── login/               # ログイン画面 (Google / 匿名)
├── home/                # ホーム画面 (部屋一覧・フィルター)
├── makeSpace/           # 部屋作成画面
├── Space/               # タイマー・コメント画面 (部屋内)
├── record/              # 履歴・統計画面
└── Setting/             # 設定画面
```

### データフロー

```
UI (Composable) → ViewModel → Repository Interface → Repository Impl → Firebase / Supabase
```

### 主要な設計ポイント

- **タイマー同期**: 開始時刻とセッション数をFirestoreに保存し、各端末がローカル時間差分で現在フェーズを計算する
- **認証**: Firebase Authentication (Google / 匿名)。IDトークンは `TokenManager` (DataStore) でキャッシュ
- **プロフィール画像**: Firebase Auth + Firestore (URL参照) + Supabase Storage (実体) に分離
- **バックグラウンド通知**: アプリ切り替え時に WorkManager で通知を送信、集中を促す

---

## バックエンド

| 用途 | サービス |
|------|---------|
| 認証 | Firebase Authentication |
| リアルタイムDB / コレクション | Cloud Firestore |
| プロフィール画像ストレージ | Supabase Storage |
| 画像アップロードAPI | Supabase Edge Function (`upload-profile`) |

### Firestore コレクション

- `users` — ユーザープロフィール
- `spaces` — 部屋情報（タイマー開始時刻・セッション数・公開フラグなど）
- `records` — 作業記録
- `comments` — 部屋内チャット

### Supabase Edge Function (Deno/TypeScript)

`supabase/functions/upload-profile/index.ts`

- Firebase IDトークンを検証し、Base64 or URLの画像を `pomodoro/users/{uid}/profile.jpg` にアップロード
- レスポンス: `{ "publicUrl": "..." }`

---

## ビルドコマンド

```bash
# デバッグビルド
./gradlew assembleDebug

# リリースビルド
./gradlew assembleRelease

# デバッグ用インストール (接続済みデバイス/エミュレーター)
./gradlew installDebug

# 全テスト実行
./gradlew test

# lint チェック
./gradlew lint

# クリーン
./gradlew clean
```

---

## 環境変数 / local.properties

`local.properties` に以下を設定する（git管理外）:

```properties
# Gemini API
API_KEY=<gemini-api-key>

# Supabase
SUPABASE_URL=https://<project-ref>.supabase.co
SUPABASE_ANON_KEY=<anon-key>
SUPABASE_EDGE_FUNCTION_URL=https://<project-ref>.functions.supabase.co/upload-profile

# Google OAuth (Firebase)
WEB_CLIENT_ID=<web-client-id>
```

これらは `app/build.gradle.kts` で `buildConfigField` として注入される。

---

## 主要な依存関係

| カテゴリ | ライブラリ | バージョン |
|---------|-----------|-----------|
| DI | Hilt | 2.57.1 |
| Firebase | BOM | 34.2.0 |
| Supabase Kotlin | BOM | 2.6.1 |
| Compose | BOM | 2024.09.00 |
| Navigation Compose | — | 2.9.3 |
| Lifecycle / ViewModel | — | 2.9.3 |
| WorkManager | — | 2.10.4 |
| DataStore | — | 1.1.1 |
| Ktor Client (Android) | — | 2.3.12 |
| OkHttp | — | 5.1.0 |
| Coil (画像ロード) | — | 2.6.0 |
| Vico (グラフ) | — | 2.1.3 |
| Google Generative AI (Gemini) | — | 0.9.0 |
| Image Cropper | — | 4.6.0 |

---

## 権限 (AndroidManifest)

- `INTERNET` — ネットワーク通信
- `POST_NOTIFICATIONS` — プッシュ通知
- `RECEIVE_BOOT_COMPLETED` — 起動時アラーム復元
- `SCHEDULE_EXACT_ALARM` — 正確なアラーム
- `READ_MEDIA_IMAGES` — ギャラリーアクセス
- `CAMERA` — カメラ (optional)

---

## コーディング規約

- PRレビューコメントは **日本語** で記述する
- Kotlin / Jetpack Compose / Clean Architecture / Android ベストプラクティスに従う
- ステート管理とコルーチンの誤用に注意する
- コメントは WHY が非自明な場合のみ記述する（WHAT は不要）