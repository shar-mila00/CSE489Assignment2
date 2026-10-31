package com.example.cse489assignment2

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.cse489assignment2.databinding.FragmentVideoBinding

/**
 * Drawer option C - Video.
 *
 * Plays one video (with sound) bundled in the app (res/raw/sample_video.mp4)
 * using a VideoView backed by MediaPlayer and the standard MediaController.
 */
class VideoFragment : Fragment() {

    private var _binding: FragmentVideoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Make sure the video sound is audible: hardware keys control MEDIA volume,
        // and a muted media volume is raised to half.
        val audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        requireActivity().volumeControlStream = AudioManager.STREAM_MUSIC
        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max / 2, 0)
            Toast.makeText(requireContext(), R.string.volume_raised, Toast.LENGTH_LONG).show()
        }

        val mediaController = MediaController(requireContext())
        mediaController.setAnchorView(binding.videoView)

        binding.videoView.setMediaController(mediaController)
        // Video is bundled inside the app: res/raw/sample_video.mp4
        val videoUri =
            Uri.parse("android.resource://${requireContext().packageName}/${R.raw.sample_video}")
        binding.videoView.setVideoURI(videoUri)
        binding.videoView.setOnPreparedListener { mp ->
            mp.setVolume(1f, 1f)
            mp.isLooping = true
            binding.videoView.start()
        }
        binding.videoView.setOnErrorListener { _, _, _ ->
            Toast.makeText(requireContext(), R.string.video_error, Toast.LENGTH_LONG).show()
            true
        }
    }

    override fun onPause() {
        super.onPause()
        _binding?.videoView?.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Release the MediaPlayer backing the VideoView.
        _binding?.videoView?.stopPlayback()
        _binding = null
    }
}
