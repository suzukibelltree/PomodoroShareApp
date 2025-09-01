package com.belltree.pomodoroshareapp.domain.models

data class Record(
    val recordId: String = "", // RecordのID(Firestore側ではドキュメントID)

    val userId: String = "", // ユーザーID(Firebase AuthenticationのUID)
    // 部屋に参加した情報
    val roomId: String = "", // Firestore上のSpaceドキュメントIDと紐づける
    val roomName: String? = null, // 参加した部屋の名前

    // セッション情報
    val startTime: Long = 0L, // セッション開始時刻 (UnixTime/ミリ秒)
    val endTime: Long = 0L, // セッション終了時刻 (UnixTime/ミリ秒)
    val durationMinutes: Int = 0, // 作業時間 (分)
    val taskDescription: List<String?> = emptyList(), //作業内容（1セッションごとに記録）

    // メタ情報
    val createdAt: Long = 0L // レコード作成時刻（後から並び替えしやすい）
)
