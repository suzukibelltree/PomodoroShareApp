package com.belltree.pomodoroshareapp.domain.models

data class User(
    val userId: String = "",
    val userName: String = "",
    val photoUrl: String = "",
    val goalStudyTime: Long = 0,
    val currentStudyTime: Long = 0,
    //セッションを繰り返すほどポイントが上がる
    /*
    1セッション10P
    < 50 ブロンズ
    < 100 シルバー
    < 150 ゴールド
    < 200 ダイアモンド
    */
    val totalStudyPoint: Long = 0,
    //フレームの種類
    val rewardState: String = RewardState.Bronze.toString(),
    val userSpaceState: UserSpaceState = UserSpaceState.Exit
)

enum class RewardState {
    Bronze,
    Sliver,
    Gold,
    Diamond,
}

enum class UserSpaceState {
    Use,
    Exit,
}