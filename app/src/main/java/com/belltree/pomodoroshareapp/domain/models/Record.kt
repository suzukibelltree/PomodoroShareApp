package com.belltree.pomodoroshareapp.domain.models

data class Record(
    // 部屋に参加した情報（任意）
    val roomId: String?,          // Firestoreの部屋IDと紐付けるなら
    val roomName: String?,        // 部屋名（部屋が削除されても履歴に残せるようにコピー）

    // セッション情報
    val startTime: Long,          // セッション開始時刻 (UnixTime/ミリ秒)
    val endTime: Long,            // セッション終了時刻 (UnixTime/ミリ秒)
    val durationMinutes: Int,     // 作業時間（分）

    // 作業内容
    val taskDescription: String?, // 「何をやっていたか」コメント（ユーザーが入力）

    // メタ情報
    val createdAt: Long           // レコード作成時刻（後から並び替えしやすい）
)
