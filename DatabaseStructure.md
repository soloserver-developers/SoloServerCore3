## PlayersTable

プレイヤーに関する情報を保存するテーブルです。  
`| id (VARCHAR[36]) | spawn_location (TEXT) | joined_team (VARCHAR[36]) | fixed_home (TEXT) | peaceful_mode (BOOL) |`

## TeamsTable

チームに関する情報を保存するテーブルです。  
`| id (VARCHAR[36]) | owner (VARCHAR[36]) | members (LONGTEXT) |`

## ChestsTable

チームチェストに関する情報を保存するテーブルです。  
`| id (VARCHAR[36]) | items (VARBINARY[1024]) |`

## MessagesTable

チームメッセージボードに関する情報を保存するテーブルです。  
`| id (VARCHAR[36]) | sender_id (VARCHAR[36]) | target_team (VARCHAR[36]) | send_date (TIMESTAMP) | subject (TINYTEXT) | message (LONGTEXT) |`

## PluginSettingsTable

プラグインの設定を保存するテーブルです。  
`| settings_name | settings_value |`
