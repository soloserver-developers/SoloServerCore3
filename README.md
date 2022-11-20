# SoloServerCore 3
[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fsoloserver-developers%2FSoloServerCore3.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2Fsoloserver-developers%2FSoloServerCore3?ref=badge_shield)


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
- スポーンワールドが変更された際に新ワールドに移動する機能
- (開発者向け) 受信可能なイベントが幾つか実装されました。 [CustomEvents](./CustomEvent.md)

### 廃止された機能

- プレイヤーの表示非表示を切り替える機能

## 動作要項

- PaperMC 1.18.x (**PaperMC 1.18-66 Tested**)
- MySQL or MariaDB 5.x ~

**重要なお知らせ**  
SoloServerCore3 v5.0.0よりPaperMCおよびそのフォークに最適化されています。    
通常のSpigotやPaperMCのフォークでないサーバーアプリケーションでは動作速度が低下する可能性があります。

### 前提プラグイン

- CoreProtect 21.2 ~
- ProtocolLib 4.8.x ~ (**This is optional since v4.2.0.**)

## Develpment [![](https://jitpack.io/v/soloserver-developers/SoloServerCore3.svg)](https://jitpack.io/#soloserver-developers/SoloServerCore3)

SoloServerCore3ではAPIやカスタムイベントが使用可能になっています。  
SoloServerCoreを依存関係に追加する場合は以下を使用して下さい。

**maven:**

```xml

<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

```xml

<dependencies>
    <dependency>
        <groupId>com.github.soloserver-developers</groupId>
        <artifactId>SoloServerCore3</artifactId>
        <version>Tag</version>
    </dependency>
</dependencies>
```

**gradle**

```Groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.soloserver-developers:SoloServerCore3:Tag'
}
```

### SoloServerApiを使う

SoloServerApiを使用する際にプラグインのインスタンスを呼び出す必要はありません。  
SoloServerApiから直接アクセスすることができます。  
但しSoloServerCoreを`plugin.yml`内で`depend`に設定することを忘れないでください。

```java
public final class SoloServerCoreExtensions extends JavaPlugin {
    @Override
    public void onEnable() {
        // SoloServerApi.getInstance() はシングルトンです。
        // 常に1つのインスタンスを返します。
        SoloServerApi soloServerApi = SoloServerApi.getInstance();

        // 例えばSSCのプレイヤーデータを取得する方法は以下のとおりです。
        Player player = Bukkit.getPlayer(uuid);
        SSCPlayer sscPlayer = soloServerApi.getSSCPlayer(player);
    }
}
```

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


[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fsoloserver-developers%2FSoloServerCore3.svg?type=large)](https://app.fossa.com/projects/git%2Bgithub.com%2Fsoloserver-developers%2FSoloServerCore3?ref=badge_large)