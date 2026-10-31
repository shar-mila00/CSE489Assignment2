package com.example.cse489assignment2

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.cse489assignment2.databinding.FragmentAudioBinding

/**
 * Drawer option D - Audio.
 *
 * Plays one audio file bundled in the app (res/raw/sample_audio.mp3) using
 * MediaPlayer with Play / Pause / Stop controls and a media-volume slider.
 */
class AudioFragment : Fragment() {

    private var _binding: FragmentAudioBinding? = null
    private val binding get() = _binding!!
    private var mediaPlayer: MediaPlayer? = null
    private var prepared = false
    private lateinit var audioManager: AudioManager

    // --- Player bar state ---
    private val handler = Handler(Looper.getMainLooper())
    private var userSeeking = false
    private var muted = false
    private val progressUpdater = object : Runnable {
        override fun run() {
            updateProgress()
            handler.postDelayed(this, 300)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAudioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // Hardware volume keys control the MEDIA volume while this screen is open.
        requireActivity().volumeControlStream = AudioManager.STREAM_MUSIC

        setupVolume()
        initMediaPlayer()
        setupControls()
        setupPlayerBar()
        handler.post(progressUpdater)
    }

    // ---------------------------------------------------------------------
    // Player bar (play/pause, time, seek bar, mute, more menu)
    // ---------------------------------------------------------------------

    private fun setupPlayerBar() {
        val mp = mediaPlayer
        binding.seekProgress.max = mp?.duration ?: 0
        updateProgress()
        updatePlayIcon()

        // Play / pause toggle.
        binding.btnBarPlay.setOnClickListener {
            val player = mediaPlayer ?: return@setOnClickListener
            if (!prepared) return@setOnClickListener
            if (player.isPlaying) {
                player.pause()
                binding.tvStatus.text = getString(R.string.audio_status_paused)
            } else {
                player.start()
                binding.tvStatus.text = getString(R.string.audio_status_playing)
            }
            updatePlayIcon()
        }

        // Seek bar: drag to jump to any position.
        binding.seekProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) updateTimeText(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                userSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                userSeeking = false
                if (prepared) mediaPlayer?.seekTo(seekBar?.progress ?: 0)
            }
        })

        // Volume icon: mute / unmute the player.
        binding.btnBarVolume.setOnClickListener {
            muted = !muted
            val v = if (muted) 0f else 1f
            mediaPlayer?.setVolume(v, v)
            binding.btnBarVolume.setImageResource(
                if (muted) R.drawable.ic_player_volume_off else R.drawable.ic_player_volume_up
            )
        }

        // Three-dots menu: playback speed.
        binding.btnBarMore.setOnClickListener { anchor ->
            val menu = PopupMenu(requireContext(), anchor)
            menu.menu.add(0, 0, 0, R.string.speed_0_5)
            menu.menu.add(0, 1, 1, R.string.speed_1)
            menu.menu.add(0, 2, 2, R.string.speed_1_5)
            menu.menu.add(0, 3, 3, R.string.speed_2)
            menu.setOnMenuItemClickListener { item ->
                val speed = when (item.itemId) {
                    0 -> 0.5f
                    2 -> 1.5f
                    3 -> 2f
                    else -> 1f
                }
                setSpeed(speed)
                true
            }
            menu.show()
        }
    }

    private fun setSpeed(speed: Float) {
        val mp = mediaPlayer ?: return
        if (!prepared) return
        try {
            val wasPlaying = mp.isPlaying
            mp.playbackParams = mp.playbackParams.setSpeed(speed)
            // Setting playback params can start playback on some devices; keep the old state.
            if (!wasPlaying && mp.isPlaying) mp.pause()
        } catch (e: Exception) {
            // Speed not supported on this device - ignore.
        }
        updatePlayIcon()
    }

    private fun updatePlayIcon() {
        val b = _binding ?: return
        val playing = mediaPlayer?.isPlaying == true
        b.btnBarPlay.setImageResource(
            if (playing) R.drawable.ic_player_pause else R.drawable.ic_player_play
        )
    }

    private fun updateProgress() {
        val b = _binding ?: return
        val mp = mediaPlayer
        if (!prepared || mp == null) return
        if (b.seekProgress.max != mp.duration) b.seekProgress.max = mp.duration
        if (!userSeeking) {
            val pos = mp.currentPosition
            b.seekProgress.progress = pos
            updateTimeText(pos)
        }
        updatePlayIcon()
    }

    private fun updateTimeText(position: Int) {
        val total = mediaPlayer?.duration ?: 0
        _binding?.tvTime?.text = "${formatTime(position)} / ${formatTime(total)}"
    }

    private fun formatTime(ms: Int): String {
        val totalSec = ms / 1000
        return "%d:%02d".format(totalSec / 60, totalSec % 60)
    }

    /** Volume slider bound to the device MEDIA volume (the one that makes sound audible). */
    private fun setupVolume() {
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        // If media volume is muted the user would hear nothing -> raise it to half.
        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max / 2, 0)
            Toast.makeText(requireContext(), R.string.volume_raised, Toast.LENGTH_LONG).show()
        }
        binding.seekVolume.max = max
        binding.seekVolume.progress = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        binding.seekVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun initMediaPlayer() {
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                // Audio file is bundled inside the app: res/raw/sample_audio.mp3
                resources.openRawResourceFd(R.raw.sample_audio).use { afd ->
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                }
                setVolume(1f, 1f)
                setOnCompletionListener {
                    _binding?.tvStatus?.text = getString(R.string.audio_status_completed)
                    updatePlayIcon()
                }
                prepare()
            }
            prepared = true
            binding.tvStatus.text = getString(R.string.audio_status_ready)
        } catch (e: Exception) {
            prepared = false
            binding.tvStatus.text = getString(R.string.audio_status_error)
        }
    }

    private fun setupControls() {
        binding.btnPlay.setOnClickListener {
            val mp = mediaPlayer ?: return@setOnClickListener
            if (prepared && !mp.isPlaying) {
                mp.start()
                binding.tvStatus.text = getString(R.string.audio_status_playing)
                updatePlayIcon()
            }
        }

        binding.btnPause.setOnClickListener {
            val mp = mediaPlayer ?: return@setOnClickListener
            if (prepared && mp.isPlaying) {
                mp.pause()
                binding.tvStatus.text = getString(R.string.audio_status_paused)
                updatePlayIcon()
            }
        }

        binding.btnStop.setOnClickListener {
            val mp = mediaPlayer ?: return@setOnClickListener
            if (prepared) {
                if (mp.isPlaying) mp.pause()
                mp.seekTo(0)
                binding.tvStatus.text = getString(R.string.audio_status_stopped)
                updateProgress()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Don't keep playing when the app goes to the background.
        val mp = mediaPlayer
        if (prepared && mp != null && mp.isPlaying) {
            mp.pause()
            _binding?.tvStatus?.text = getString(R.string.audio_status_paused)
            updatePlayIcon()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(progressUpdater)
        // Always release the MediaPlayer when the view goes away.
        mediaPlayer?.release()
        mediaPlayer = null
        prepared = false
        _binding = null
    }
}
