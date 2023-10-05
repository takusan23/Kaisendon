# かいせんどん　プライバシーポリシー

## アクセストークンの扱い
アクセストークン（ユーザーのデータを取得するときに必要な文字列）はアプリ内のみに保存します。  
またアプリの機能でアクセストークンを含むファイルをコピーする機能が搭載されていますが、ユーザーの操作なく勝手に動くことはありません。

## 利用する権限について
### android.permission.READ_MEDIA_IMAGES
画像の共有コレクションへアクセスする権限です。  
Android Q 以降で画像つき投稿をするときに利用します。
### android.permission.ACCESS_WIFI_STATE　　
Wi-Fiネットワークに関する情報へアクセスする権限です。  
Wi-Fi接続時のみ画像を表示する設定を利用するときに利用します。
### android.permission.INTERNET
インターネットへアクセスする権限です。  
Mastodon/Misskey API へアクセスするときに利用します。
### android.permission.ACCESS_NETWORK_STATE
ネットワークに関する情報へアクセスする権限です。  
Wi-Fi接続時のみ画像表示、ネットワーク切り替え通知時に利用しています。
### android.permission.VIBRATE
バイブレーターへアクセスする権限です。  
通知が来たとき等でバイブを鳴らすのでそのときに利用しています。
### android.permission.READ_EXTERNAL_STORAGE
ストレージへ読み込む権限です。
カスタムメニューのデータ、ブックマークのデータ、フォントデータ、ActivityPubのデータを他の場所から読み込むときに利用しています。
### android.permission.WRITE_EXTERNAL_STORAGE
ストレージへ書き込む権限です。
カスタムメニューのデータ、ブックマーク、のデータを他の場所にコピーするときに利用しています。

追記:ストレージアクセス系パーミッションはScoped Storageで要らなくなるのかもしれない。
