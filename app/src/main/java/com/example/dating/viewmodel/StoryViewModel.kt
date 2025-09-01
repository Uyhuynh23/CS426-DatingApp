package com.example.dating.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dating.data.model.Resource
import com.example.dating.data.model.Story
import com.example.dating.data.model.repository.StoryRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoryViewModel @Inject constructor(
    private val repo: StoryRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _postState = MutableStateFlow<Resource<List<Story>>?>(null)
    val postState: StateFlow<Resource<List<Story>>?> = _postState

    val myStories: StateFlow<List<Story>> =
        (auth.currentUser?.uid?.let { repo.observeMyStories(it) } ?: flowOf(emptyList()))
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    /**
     * Observe stories for any user as StateFlow.
     */
    fun observeUserStories(uid: String): StateFlow<List<Story>> {
        val state = MutableStateFlow<List<Story>>(emptyList())
        viewModelScope.launch {
            repo.observeMyStories(uid).collectLatest { stories ->
                state.value = stories
            }
        }
        return state
    }

    fun postStories(caption: String?, uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _postState.value = Resource.Loading
            try {
                val res = repo.postStories(caption, uris)
                _postState.value = Resource.Success(res)
            } catch (e: Exception) {
                _postState.value = Resource.Failure(e)
            }
        }
    }

    fun clearPostState() { _postState.value = null }

    fun markSeen(ownerUid: String, storyId: String) {
        val viewer = auth.currentUser?.uid ?: return
        viewModelScope.launch { repo.markSeen(ownerUid, storyId, viewer) }
    }

    fun deleteMyStory(storyId: String) {
        viewModelScope.launch { repo.deleteMyStory(storyId) }
    }
}
