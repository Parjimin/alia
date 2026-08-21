package com.littleblueworld.alia.gallery

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.littleblueworld.alia.content.BirthdayContent
import com.littleblueworld.alia.databinding.ScreenGalleryBinding
import com.littleblueworld.alia.navigation.AppScreen
import com.littleblueworld.alia.navigation.ScreenId
import com.littleblueworld.alia.state.AppState

data class GalleryActions(
    val goBack: () -> Unit,
    val onFirstMeaningfulInteraction: () -> Unit,
)

class GalleryScreenFactory(
    context: Context,
    private val content: BirthdayContent,
) {
    private val inflater = LayoutInflater.from(context)

    fun create(
        state: AppState,
        actions: GalleryActions,
    ): AppScreen {
        val binding = ScreenGalleryBinding.inflate(inflater)
        return GalleryScreen(binding, content, state.galleryVisited, actions)
    }

    private class GalleryScreen(
        private val binding: ScreenGalleryBinding,
        content: BirthdayContent,
        alreadyDiscovered: Boolean,
        actions: GalleryActions,
    ) : AppScreen {
        override val id = ScreenId.GALLERY
        override val view: View = binding.root
        private var discoveryRecorded = alreadyDiscovered

        init {
            binding.galleryTitle.text = content.archiveTitle
            binding.gallerySubtitle.text = content.archiveSubtitle
            binding.galleryBack.setOnClickListener { actions.goBack() }
            binding.galleryPager.configure(
                count = content.archiveCaptions.size,
                labelForIndex = { index -> content.photoAccessibilityLabel(index + 1) },
                onIndexChanged = { index ->
                    renderPhotoMetadata(index, content.archiveCaptions)
                    if (!discoveryRecorded) {
                        discoveryRecorded = true
                        actions.onFirstMeaningfulInteraction()
                    }
                },
            )
            renderPhotoMetadata(0, content.archiveCaptions)
        }

        override fun render(state: AppState) = Unit

        override fun onBackgrounded() {
            binding.galleryPager.pauseMotion()
        }

        override fun onHidden() {
            binding.galleryBack.setOnClickListener(null)
            binding.galleryPager.clear()
        }

        private fun renderPhotoMetadata(index: Int, captions: List<String>) {
            binding.galleryIndex.text = binding.root.context.getString(
                com.littleblueworld.alia.R.string.gallery_index_format,
                index + 1,
                captions.size,
            )
            binding.galleryCaption.text = captions[index]
        }
    }
}
