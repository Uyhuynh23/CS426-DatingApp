package com.example.dating.data.model.repository

import android.net.Uri
import com.example.dating.data.model.ImageTransform
import com.example.dating.data.model.Story
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class FirebaseStoryRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) : StoryRepository {

    private fun items(ownerUid: String) =
        db.collection("stories").document(ownerUid).collection("items")

    override fun observeMyStories(uid: String): Flow<List<Story>> = callbackFlow {
        val reg = items(uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                trySend(snap?.toObjects(Story::class.java).orEmpty())
            }
        awaitClose { reg.remove() }
    }

    override suspend fun postStories(caption: String?, media: List<Uri>, transforms: List<ImageTransform>): List<Story> {
        val me = auth.currentUser ?: error("Not signed in")
        val ownerUid = me.uid
        val now = System.currentTimeMillis()
        val exp = now + 24L * 60 * 60 * 1000

        val created = mutableListOf<Story>()

        for ((index, uri) in media.withIndex()) {
            val storyId = UUID.randomUUID().toString()
            val filename = uri.lastPathSegment?.substringAfterLast('/') ?: "media"
            val objectPath = "stories/$ownerUid/$storyId/$filename"

            // upload
            val ref = storage.reference.child(objectPath)
            ref.putFile(uri).await()
            val url = ref.downloadUrl.await().toString()

            // Get the corresponding transform for this image
            val transform = transforms.getOrNull(index)

            // doc
            val doc = items(ownerUid).document(storyId)
            val data = mutableMapOf<String, Any?>(
                "id" to storyId,
                "ownerUid" to ownerUid,
                "mediaUrl" to url,
                "caption" to caption,
                "createdAt" to FieldValue.serverTimestamp(),
                "expiresAt" to exp,
                "seenBy" to emptyList<String>()
            )

            // Add image transform data if available
            if (transform != null) {
                data["imageTransform"] = mapOf(
                    "scaleX" to transform.scaleX,
                    "scaleY" to transform.scaleY,
                    "translationX" to transform.translationX,
                    "translationY" to transform.translationY,
                    "rotationZ" to transform.rotationZ
                )
            }

            doc.set(data).await()

            // read back (optional) to include server ts
            val snap = doc.get().await()
            created += (snap.toObject(Story::class.java) ?: Story(
                id = storyId,
                ownerUid = ownerUid,
                mediaUrl = url,
                caption = caption,
                createdAt = Timestamp(now / 1000, 0),
                expiresAt = exp,
                imageTransform = transform
            ))
        }
        return created
    }

    override suspend fun markSeen(storyOwnerUid: String, storyId: String, viewerUid: String) {
        items(storyOwnerUid).document(storyId)
            .update("seenBy", FieldValue.arrayUnion(viewerUid)).await()
    }

    override suspend fun deleteMyStory(storyId: String) {
        val uid = auth.currentUser?.uid ?: return
        items(uid).document(storyId).delete().await()
        // best-effort delete storage folder
        val folderRef = storage.reference.child("stories/$uid/$storyId")
        runCatching { folderRef.listAll().await().items.forEach { it.delete().await() } }
    }
}
