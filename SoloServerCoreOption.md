# SoloServerCoreOption
SoloServerCore3ではゲーム内コマンドでプラグインの設定を変更できます。

| Option | Type | Description | Default |
| :--- | :---: | :--- | :--- |
| checkBlock | bool | ブロックのアクティビティをチェックします。 | true |
| protectionPeriod | int | ブロックを保護する期間 (分)  | 259200 |
| teamSpawnCollect | bool | チームメンバーのスポーンポイントを集合します。 | true |
| stockSpawnPoint | int | 初期生成するランダム座標の数 | 100 |
| broadcastBedCount | bool | ベッドに寝ていない人の数を告知 | true |
| useAfkCount | bool | AFKプレイヤーの除外機能を有効にします。 | false |
| afkTimeThreshold | int | AFK判定を行うまでの時間 (分) | 30 |

## Optionの説明
### checkBlock
プレイヤーがブロックの設置や破壊を行おうとした場合にCoreProtectの設置アクティビティをチェックします。  
protectionPeriodの値に基づき保護期間内に他ユーザーによる設置アクティビティがあった場合はブロックの変更をキャンセルします。

### protectionPeriod
ブロックの変更を保護する期間を分単位で指定できます。  
0を指定すると永久的に保護します。

### teamSpawnCollect
チームに所属するメンバーのスポーンポイントをチームオーナーのポイントに集合させます。  
このときチームメンバーのスポーンポイントは変更されません。  
チームメンバーがチームを脱退すると本来のスポーンポイントに戻ります。

### stockSpawnPoint
起動時に生成するランダムスポーンポイント座標のストック数を指定できます。

### broadcastBedCount
だれかがベッドに寝ると寝ていない人の数をカウントしてブロードキャストします。

### useAfkCount
AFKプレイヤーの除外機能を有効にします。
AFK中の以外のプレイヤーが就寝すると夜をスキップします。

### afkTimeThreshold
AFKとして判定するまでの時間を分単位で指定できます。
