# CustomEvent
SoloServerCore3は外部プラグインから受信可能なイベントが用意されています。

### PlayersTeamEvent
- PlayersTeamEvent: チームに変更が行われた場合に呼び出されます。
    - **PlayersTeamJoinEvent**: プレイヤーがチームに参加した場合に呼び出されます。
    - **PlayersTeamLeaveEvent**: プレイヤーがチームから退出した場合に呼び出されます。
    - PlayersTeamDisappearanceEvent: オーナーがチームから退出したことによりチームが消滅した場合に呼び出されます。
    - PlayersTeamStatusUpdateEvent: チームの情報が更新された際に呼び出されます。

### Others
- PlayerMoveToNewWorldEvent: プレイヤーが新しいワールドに移動した場合に呼び出されます。

## 注意事項
SoloServerCore3のイベントはPriority＝MONITORで変更を加えないでください。  
内部処理をPriority＝MONITOR実行しているため不整合が発生する可能性があります。

**太字で書かれたイベントはキャンセル可能なイベントです。**