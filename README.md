# Vikunja Bubble App

Android Bubbles API を使ったVikunjaタスク管理アプリ。

## 機能

- Android Bubbles APIによるフローティングバブルUI
- バブル内チャット風インターフェース
- Vikunja APIへの直接タスク追加
- チャット履歴の保持

## セットアップ

1. Android Studio でプロジェクトを開く
2. `app/build.gradle.kts` のminSdkが29以上であることを確認
3. アプリを起動し、以下を入力:
   - Vikunja URL (例: `https://sou56.servegame.com`)
   - API Token
   - Project ID
4. 「バブルを起動」ボタンをタップ

## 必要環境

- Android 11 (API 30) 以上
- Vikunja サーバー

## ビルド

```bash
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`
