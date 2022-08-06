package uz.mobiler.lesson57.fragment

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import uz.mobiler.lesson57.R
import uz.mobiler.lesson57.database.AppDatabase
import uz.mobiler.lesson57.database.entity.MusicModel
import uz.mobiler.lesson57.databinding.FragmentMp3Binding
import java.util.concurrent.TimeUnit

private const val ARG_PARAM1 = "music"
private const val ARG_PARAM2 = "position"

class Mp3Fragment : Fragment(), MediaPlayer.OnPreparedListener {
    private var param1: MusicModel? = null
    private var param2: Int? = null
    val appDatabase: AppDatabase by lazy {

        AppDatabase.getInstance(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getSerializable(ARG_PARAM1) as MusicModel?
            param2 = it.getInt(ARG_PARAM2)
        }
    }

    private lateinit var binding: FragmentMp3Binding
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var handler: Handler
    private lateinit var musicList: ArrayList<MusicModel>
    private var index: Int = 0
    private lateinit var musicModel: MusicModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMp3Binding.inflate(inflater, container, false)
        binding.apply {
            musicModel = param1 as MusicModel
            index = param2 as Int
            musicList = ArrayList(appDatabase.musicDao().getAllMusics())
            work()
            play.setOnClickListener {
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.pause()
                    play.setImageResource(R.drawable.play)
                    imgCard.layoutParams.width = 500
                    imgCard.layoutParams.height = 500
                } else {
                    mediaPlayer?.start()
                    play.setImageResource(R.drawable.pause)
                    imgCard.layoutParams.width = 650
                    imgCard.layoutParams.height = 650
                    val duration = mediaPlayer?.duration
                    if (duration != null) {
                        seekbar.max = duration
                        handler.postDelayed(runnable, 1000)
                    }
                }
            }

            next.setOnClickListener {
                if (musicList.size == index) index = 1
                else index++
                releaseMp()
                work()
                imgCard.layoutParams.width = 650
                imgCard.layoutParams.height = 650
            }

            previous.setOnClickListener {
                if (index == 1) index = musicList.size
                else index--
                releaseMp()
                work()
                imgCard.layoutParams.width = 650
                imgCard.layoutParams.height = 650
            }

            forward.setOnClickListener {
                mediaPlayer?.seekTo(mediaPlayer?.currentPosition?.plus(10000) ?: 0)
            }

            replay.setOnClickListener {
                mediaPlayer?.seekTo(mediaPlayer?.currentPosition?.minus(10000) ?: 0)
            }

            seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    if (p2) {
                        mediaPlayer?.seekTo(p1)
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                override fun onStopTrackingTouch(p0: SeekBar?) {

                }

            })
        }
        return binding.root
    }

    private fun work() {
        binding.play.setImageResource(R.drawable.play)
        musicModel = appDatabase.musicDao().getMusicById(index)
        handler = Handler(Looper.getMainLooper())
        Glide.with(requireContext())
            .load(musicModel.imagePath)
            .apply(RequestOptions().placeholder(R.drawable.mobiler).centerCrop())
            .into(binding.image)
        binding.name.text = musicModel.name
        binding.author.text = musicModel.author
        binding.allCount.text = musicList.size.toString()
        binding.currentCount.text = index.toString()
        val duration = musicModel.duration

        val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
        val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) -
                minutes * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
        binding.time.text = String.format("%02d:%02d", minutes, seconds)
        binding.list.setOnClickListener {
            Navigation.findNavController(binding.root).popBackStack()
        }
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setDataSource(musicModel.musicPath)
        mediaPlayer?.setOnPreparedListener(this@Mp3Fragment)
        mediaPlayer?.prepareAsync()
    }

    private val runnable = object : Runnable {
        override fun run() {
            val currentPosition = mediaPlayer?.currentPosition
            if (currentPosition != null) {
                binding.seekbar.progress = currentPosition
                handler.postDelayed(this, 1000)
            }
            val second = currentPosition?.div(1000)?.rem(60)
            val minute = currentPosition?.div(1000)?.rem(3600)?.div(60)
            binding.currentTime.text = String.format("%02d:%02d", minute, second)
            if (binding.seekbar.progress / 1000 == binding.seekbar.max / 1000) {
                binding.play.setImageResource(R.drawable.play)
                binding.currentTime.text = "00:00"
                binding.seekbar.progress = 0
                binding.imgCard.layoutParams.width = 500
                binding.imgCard.layoutParams.height = 500
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMp()
    }

    fun releaseMp() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer?.release()
                mediaPlayer = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: MusicModel, param2: Int) =
            Mp3Fragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PARAM1, param1)
                    putInt(ARG_PARAM2, param2)
                }
            }
    }

    override fun onPrepared(p0: MediaPlayer?) {
        p0?.start()
        binding.play.setImageResource(R.drawable.pause)
        binding.imgCard.layoutParams.width = 650
        binding.imgCard.layoutParams.height = 650
        val duration = mediaPlayer?.duration
        if (duration != null) {
            binding.seekbar.max = duration
            handler.postDelayed(runnable, 1000)
        }
    }
}