package com.example.dating.data.model.repository

import android.net.Uri
import com.example.dating.data.model.Story
import kotlinx.coroutines.flow.Flow

interface StoryRepository {
    fun observeMyStories(uid: String): Flow<List<Story>>
    suspend fun postStories(caption: String?, media: List<Uri>): List<Story>
    suspend fun markSeen(storyOwnerUid: String, storyId: String, viewerUid: String)
    suspend fun deleteMyStory(storyId: String)
}
