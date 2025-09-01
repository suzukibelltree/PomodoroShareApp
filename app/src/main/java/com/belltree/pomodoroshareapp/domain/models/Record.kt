package com.belltree.pomodoroshareapp.domain.models

data class Record(
    val recordId: String, // RecordのID(Firestore側ではドキュメントID)
    // 部屋に参加した情報
    val roomId: String, // Firestore上のSpaceドキュメントIDと紐づける
    val roomName: String?,

    // セッション情報
    val startTIme: Long, // セッション開始時刻 (UnixTime/ミリ秒)
    val endTime: Long, // セッション終了時刻 (UnixTime/ミリ秒)
    val durationMinutes: Int, // 作業時間 (分)

    // 作業内容
    val taskDescription: List<String?>, // 「何をやっていたか」コメント（1セッションごとに記録）

    // メタ情報
    val createdAt: Long // レコード作成時刻（後から並び替えしやすい）
)
