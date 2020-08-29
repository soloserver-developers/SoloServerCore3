# SoloServerCore 3
SoloServerCoreはマルチプレイなのにシングルプレイのようなあべこべ体験ができる、Spigot向けプラグインです。  
より強力で魅力的になって帰ってきたSoloServerCore3はプレイヤーに新たな体験を提供します。

## SoloServerCore 2との違い
**SoloServerCore3は完全に1から開発されておりSoloServerCore2との互換性はありません。  
そのため既存のデータの引き継ぎはできません。**

### 新たに追加されたもの
- サーバーリストのログインプレイヤーを表示しない機能
- 世界のどこかで眠りにつくのを待っている人が居ることをお知らせする機能
- 簡易的なチームプレイ機能
- ブロックの変更をチェックして他プレイヤーの設置物だった場合に変更をキャンセルする機能
- ランダムスポーンポイントの座標を事前に生成しログイン時の待ち時間を短縮
- (開発者向け) 受信可能なイベントが幾つか実装されました。 [CustomEvents](./CustomEvent.md)

### 今後のアップデートで実装予定のもの
- 任意のタイミングでランダムスポーンポイントにテレポート

### 廃止された機能
- プレイヤーの表示非表示を切り替える機能

## 動作要項
- Spigot 1.15.x
- MySQL or MariaDB 5.x ~

### 前提プラグイン
- ProtocolLib 4.5.x ~
- CoreProtect 19.1 ~

## License
This plugin is published under Apache License 2.0.
```
Copyright 2020 NAFU_at.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

