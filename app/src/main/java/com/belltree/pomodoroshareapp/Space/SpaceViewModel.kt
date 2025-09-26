package com.belltree.pomodoroshareapp.Space

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belltree.pomodoroshareapp.domain.models.Comment
import com.belltree.pomodoroshareapp.domain.models.Record
import com.belltree.pomodoroshareapp.domain.models.RewardState
import com.belltree.pomodoroshareapp.domain.models.Space
import com.belltree.pomodoroshareapp.domain.models.SpaceState
import com.belltree.pomodoroshareapp.domain.models.User
import com.belltree.pomodoroshareapp.domain.models.UserSpaceState
import com.belltree.pomodoroshareapp.domain.repository.CommentRepository
import com.belltree.pomodoroshareapp.domain.repository.RecordRepository
import com.belltree.pomodoroshareapp.domain.repository.SpaceRepository
import com.belltree.pomodoroshareapp.domain.repository.UserRepository
import com.belltree.pomodoroshareapp.home.RecentlyLeftSpaceManager
import com.belltree.pomodoroshareapp.notification.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class SpaceViewModel
@Inject
constructor(
    private val recordRepository: RecordRepository,
    private val commentRepository: CommentRepository,
    private val spaceRepository: SpaceRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth,
    private val notificationHelper: NotificationHelper,
    private val recentlyLeftSpaceManager: RecentlyLeftSpaceManager
) : ViewModel() {
    val userId: String = auth.currentUser?.uid ?: "Unknown"
    private val _records = MutableStateFlow<List<Record>>(emptyList())
    val records: StateFlow<List<Record>> = _records
    private val _spaces = MutableStateFlow<List<Space>>(emptyList())
    val spaces: StateFlow<List<Space>> = _spaces

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    private val _myComments = MutableStateFlow<List<Comment?>>(emptyList())
    val myComments: StateFlow<List<Comment?>> = _myComments

    private val _space = MutableStateFlow<Space?>(null)
    val space: StateFlow<Space?> = _space

    private val _userNames = MutableStateFlow<List<String>>(emptyList())
    val userNames: StateFlow<List<String>> = _userNames

    private val _participants = MutableStateFlow<List<User>>(emptyList())
    val participants: StateFlow<List<User>> = _participants

    private val _ownerName = MutableStateFlow<String>("")
    val ownerName: StateFlow<String> = _ownerName

    // タイマーの進行状況(Max=1.0, Min=0.0)
    private val _progress = MutableStateFlow(1f)
    val progress: StateFlow<Float> = _progress

    // タイマーが動作中かどうか
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    // 残り時間(ミリ秒)
    private val _remainingTimeMillis = MutableStateFlow(0L)
    val remainingTimeMillis: StateFlow<Long> = _remainingTimeMillis

    // 開始前のカウントダウン表示用
    private val _timeUntilStartMillis = MutableStateFlow<Long>(0L)
    val timeUntilStartMillis: StateFlow<Long> = _timeUntilStartMillis

    private val _currentSessionCount = MutableStateFlow(0)
    val currentSessionCount: StateFlow<Int> = _currentSessionCount

    // スペースの状態 (待機中、作業中、休憩中、終了)
    private val _spaceState = MutableStateFlow(SpaceState.WAITING)
    val spaceState: StateFlow<SpaceState> = _spaceState

    private var timerJob: Job? = null
    private var backgroundNotificationJob: Job? = null

    // セッションの数
    private var sessionCount = 0

    // 作業時間と休憩時間 (ミリ秒)
    private var workDuration = 25 * 60 * 1000L
    private var breakDuration = 5 * 60 * 1000L

    // スペースの開始時間 (ミリ秒)
    var startTime: Long = 0L

    // 2セクション目以降終了時のRecord更新に使用
    private var currentRecordId: String? = null
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private fun setUserSpaceState(state: UserSpaceState) {
        viewModelScope.launch {
            userRepository.updateUserToFirestore(userId, mapOf("userSpaceState" to state))
        }
    }

    suspend fun getCurrentUserById(): User {
        val u = userRepository.getUserById(userId)
        return User(
            userId = u?.userId ?: "Unknown",
            userName = u?.userName ?: "Unknown",
            photoUrl = u?.photoUrl ?: ""
        )
    }

    fun addMyUserInfoToFirestore(spaceId: String) {
        viewModelScope.launch {
            spaceRepository.addMyUserInfoToSpace(
                spaceId = spaceId,
                userId = userId
            )
        }
    }

    fun fetchUserNames(userIds: List<String>) {
        viewModelScope.launch {
            val names =
                userIds.map { userId ->
                    val u = userRepository.getUserById(userId)
                    u?.userName ?: "Unknown"
                }
            _userNames.value = names
        }
    }

    fun fetchParticipants(userIds: List<String>) {
        viewModelScope.launch {
            val users =
                userIds.map { userId ->
                    val u = userRepository.getUserById(userId)
                    User(
                        userId = u?.userId ?: userId,
                        userName = u?.userName ?: "Unknown",
                        photoUrl = u?.photoUrl ?: ""
                    )
                }
            _participants.value = users
        }
    }

    fun fetchOwnerName(ownerId: String) {
        viewModelScope.launch {
            val owner = userRepository.getUserById(ownerId)
            _ownerName.value = owner?.userName ?: "Unknown"
        }
    }

    fun getSpaceById(spaceId: String) {
        viewModelScope.launch {
            val space = spaceRepository.getSpaceById(spaceId)
            _space.value = space
            space?.let { setSpace(it) }
        }
    }

    //ユーザー情報を更新
    fun updateUserToFirebase(spaceId: String, updates: Map<String, Any?>) {
        viewModelScope.launch {
            userRepository.updateUserToFirestore(spaceId, updates)
        }
    }

    // タイマーのUI表示のために情報をセットする関数(部屋入室時に1回だけ呼ぶ)
    fun setSpace(space: Space) {
        sessionCount = space.sessionCount
        startTime = space.startTime
        _currentSessionCount.value = space.currentSessionCount
        _remainingTimeMillis.value = workDuration

        // ユーザーが部屋に参加中のため、状態を Use に設定（Firestore 更新）
        setUserSpaceState(UserSpaceState.Use)

        timerJob?.cancel()
        timerJob =
            viewModelScope.launch {
                var previousState: SpaceState? = null
                while (true) {
                    val now = System.currentTimeMillis()
                    val timeUntilStart = startTime - now
                    // 部屋開始前の処理
                    if (timeUntilStart > 0) {
                        _timeUntilStartMillis.value = timeUntilStart
                        _spaceState.value = SpaceState.WAITING
                        _progress.value = 1f
                    } else { // 部屋開始後の処理
                        _timeUntilStartMillis.value = 0L
                        val elapsed = (now - startTime).coerceAtLeast(0L)
                        val cycleLength = workDuration + breakDuration
                        val currentCycle = (elapsed / cycleLength).toInt()
                        // 全セッション終了後の処理
                        if (currentCycle >= sessionCount) {
                            _spaceState.value = SpaceState.FINISHED
                            _isRunning.value = false
                            _progress.value = 0f
                            _remainingTimeMillis.value = 0L
                            stopTimer()
                            break
                        }
                        val cyclePosition = elapsed % cycleLength
                        // 作業時間中の処理
                        if (cyclePosition < workDuration) {
                            _currentSessionCount.value = currentCycle + 1
                            _spaceState.value = SpaceState.WORKING
                            val remaining = workDuration - cyclePosition
                            _remainingTimeMillis.value = remaining
                            _progress.value = remaining.toFloat() / workDuration
                        } else {
                            // 休憩時間中の処理
                            _spaceState.value = SpaceState.BREAK
                            val breakElapsed = cyclePosition - workDuration
                            val remaining = breakDuration - breakElapsed
                            _remainingTimeMillis.value = remaining
                            _progress.value = remaining.toFloat() / breakDuration
                        }
                    }
                    if (previousState == SpaceState.WORKING &&
                        _spaceState.value == SpaceState.BREAK
                    ) {
                        val finishedSession = _currentSessionCount.value
                        if (finishedSession == 1) {
                            // 1セッション目終了
                            val newRecord =
                                Record(
                                    userId = userId,
                                    roomId = space.spaceId,
                                    roomName = space.spaceName,
                                    startTime = space.startTime,
                                    endTime = System.currentTimeMillis(),
                                    durationMinutes = 25,
                                    createdAt = space.createdAt
                                )
                            viewModelScope.launch {
                                val myCommentList = getMyCommentsOnce(space.spaceId)
                                _myComments.value = myCommentList
                                addRecord(newRecord, commentList = myCommentList)
                                // セッション完了時にポイント加算
                                viewModelScope.launch {
                                    val me = userRepository.getUserById(userId)
                                    val newPoints = (me?.totalStudyPoint ?: 0) + 10
                                    userRepository.updateUserToFirestore(
                                        userId,
                                        mapOf("totalStudyPoint" to newPoints)
                                    )
                                    updateRewardState(newPoints)
                                }
                            }
                        } else {
                            // 2セッション目以降終了
                            viewModelScope.launch {
                                val myCommentList = getMyCommentsOnce(space.spaceId)
                                _myComments.value = myCommentList
                                upDateRecord(newCommentList = myCommentList)
                                // セッション完了時にポイント加算
                                viewModelScope.launch {
                                    val me = userRepository.getUserById(userId)
                                    val newPoints = (me?.totalStudyPoint ?: 0) + 10
                                    userRepository.updateUserToFirestore(
                                        userId,
                                        mapOf("totalStudyPoint" to newPoints)
                                    )
                                    updateRewardState(newPoints)
                                }
                                Log.d("hoge", "Record updated")
                            }
                        }
                    }

                    previousState = _spaceState.value
                    delay(1000) // 1秒ごとに更新する
                }
            }
    }

    fun stopTimer() {
        timerJob?.cancel()
        _progress.value = 1f
        _remainingTimeMillis.value = 0L
        _timeUntilStartMillis.value = 0L
    }

    fun addComment(spaceId: String, comment: Comment) {
        viewModelScope.launch {
            commentRepository.addComment(spaceId, comment)
        }
    }

    fun getComments(spaceId: String) {
        viewModelScope.launch {
            commentRepository.getCommentsFlow(spaceId).collect { commentList ->
                _comments.value = commentList
            }
        }
    }

    // そのスペースで自分が投稿したコメントの一覧を取得
    suspend fun getMyCommentsOnce(spaceId: String): List<Comment> {
        return commentRepository
            .getMyCommentsFlow(spaceId, userId)
            .first() // 常に監視するのではないのでfirst()で取得
    }

    fun observeSpace(spaceId: String) {
        // 既存の監視をキャンセルしてから新しい Flow を収集
        viewModelScope.launch {
            spaceRepository.observeSpace(spaceId).collect { latest ->
                _space.value = latest
                // 参加者リストが変更された場合、ユーザー名と参加者情報を再取得
                latest.let { space ->
                    fetchUserNames(space.participantsId)
                    fetchParticipants(space.participantsId)
                    fetchOwnerName(space.ownerId)
                }
            }
        }
    }

    fun addRecord(record: Record, commentList: List<Comment?>? = null) {
        viewModelScope.launch {
            val finalRecord =
                if (commentList != null) {
                    record.copy(taskDescription = commentsToTaskDescription(commentList))
                } else {
                    record
                }

            val docRef = recordRepository.addRecordReturnDocRef(finalRecord)
            currentRecordId = docRef.id
        }
    }

    fun upDateRecord(newCommentList: List<Comment?>) {
        val recordId = currentRecordId ?: return

        val updatedRecord =
            Record(
                endTime = System.currentTimeMillis(),
                durationMinutes = currentSessionCount.value * 25,
                taskDescription = commentsToTaskDescription(newCommentList)
            )

        viewModelScope.launch { recordRepository.updateRecord(recordId, updatedRecord) }
    }

    private fun commentsToTaskDescription(commentList: List<Comment?>): List<String> {
        return commentList.sortedBy { it?.postedAt }.map { it?.content ?: "" }
    }

    fun onScreenBackgrounded() {
        backgroundNotificationJob?.cancel()
        backgroundNotificationJob =
            viewModelScope.launch {
                delay(30_000) // 30秒後に通知
                notificationHelper.showNotification("ポモドーロタイマー", "作業に戻ってください！")
                setUserSpaceState(UserSpaceState.Exit)
            }
    }

    fun onScreenForegrounded() {
        backgroundNotificationJob?.cancel()
        setUserSpaceState(UserSpaceState.Use)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        backgroundNotificationJob?.cancel()
        // 画面・ViewModel が破棄される＝部屋から退出とみなし、状態を Exit に設定
        setUserSpaceState(UserSpaceState.Exit)
    }

    fun markRecentlyLeft(spaceId: String) {
        Log.w("SpaceViewModel", "markRecentlyLeft called with id=$spaceId")
        // 明示的に退出したため、状態を Exit に設定
        setUserSpaceState(UserSpaceState.Exit)
        recentlyLeftSpaceManager.mark(spaceId)
    }

    fun updateRewardState(totalStudyPoint: Long) {
        viewModelScope.launch {
            val user = userRepository.getUserById(userId)
            val currentState = user?.rewardState ?: return@launch
            val newState = when {
                totalStudyPoint >= 150 -> RewardState.Diamond.toString()
                totalStudyPoint >= 100 -> RewardState.Gold.toString()
                totalStudyPoint >= 50 -> RewardState.Sliver.toString()
                else -> RewardState.Bronze.toString()
            }
            if (newState != currentState) {
                userRepository.updateUserToFirestore(userId, mapOf("rewardState" to newState))
            }
        }
    }
}
