package com.belltree.pomodoroshareapp.domain.models

import java.time.LocalDate

/**
 * 日ごとの学習時間の合計を表すデータクラス
 * 履歴画面においてGeminiに投げるプロンプトやグラフ表示などで使用する
 */
data class DailyStudySummary(
    val date: LocalDate,
    val totalMinutes: Int
)
