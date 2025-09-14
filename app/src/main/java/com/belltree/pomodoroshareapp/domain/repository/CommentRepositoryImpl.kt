package com.belltree.pomodoroshareapp.domain.repository

import android.util.Log
import com.belltree.pomodoroshareapp.domain.models.Comment
import com.google.firebase.firestore.FirebaseFirestore
import jakarta.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class CommentRepositoryImpl @Inject constructor(
    val db: FirebaseFirestore
) : CommentRepository {

    // 自分が参加している部屋にコメントを追加する(SpaceViewModelで使用する)
    override fun addComment(spaceId: String, comment: Comment) {
        db.collection("spaces")
            .document(spaceId)
            .collection("comments")
            .add(comment)
            .addOnSuccessListener {
                Log.d("piya", "Comment added successfully")
            }
            .addOnFailureListener {
                Log.d("piya", "Error adding comment: ${it.message}")
            }
    }

    // コメントリストのリアルタイム更新を受け取る(SpaceViewModelで使用する)
    override fun getCommentsFlow(spaceId: String): Flow<List<Comment>> =
        callbackFlow {
            val listener = db.collection("spaces")
                .document(spaceId)
                .collection("comments")
                .orderBy("postedAt") //コメントを投稿日時でソート
                .addSnapshotListener { snapshot, e ->
                    if (snapshot != null) {
                        trySend(snapshot.toObjects(Comment::class.java))
                    }
                }
            awaitClose { listener.remove() }
        }

    // その部屋で自分が投稿したコメントの一覧を取得する(SpaceViewModelで使用する)
    override fun getMyCommentsFlow(spaceId: String, userId: String): Flow<List<Comment>> =
        callbackFlow {
            val listener = db.collection("spaces")
                .document(spaceId)
                .collection("comments")
                .whereEqualTo("userId", userId)
                .orderBy("postedAt")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        close(e)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val comments = snapshot.toObjects(Comment::class.java)
                        trySend(comments).isSuccess
                    }
                }
            awaitClose { listener.remove() }
        }
}