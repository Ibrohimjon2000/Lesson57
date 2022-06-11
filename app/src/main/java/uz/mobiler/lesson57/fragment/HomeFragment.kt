package uz.mobiler.lesson57.fragment

import android.Manifest
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import uz.mobiler.lesson57.R
import uz.mobiler.lesson57.adapters.MusicAdapter
import uz.mobiler.lesson57.database.AppDatabase
import uz.mobiler.lesson57.database.entity.MusicModel
import uz.mobiler.lesson57.databinding.FragmentHomeBinding


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    val appDatabase: AppDatabase by lazy {

        AppDatabase.getInstance(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private lateinit var binding: FragmentHomeBinding
    private lateinit var musicList: ArrayList<MusicModel>
    private lateinit var musicAdapter: MusicAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.apply {
            musicList = ArrayList(appDatabase.musicDao().getAllMusics())
            musicAdapter = MusicAdapter(
                requireContext(),
                musicList,
                object : MusicAdapter.OnItemClickListener {
                    override fun onItemClick(musicModel: MusicModel, position: Int) {
                        val bundle = Bundle()
                        bundle.putSerializable("music", musicModel)
                        bundle.putInt("position", position + 1)
                        Navigation.findNavController(root).navigate(R.id.mp3Fragment, bundle)
                    }
                })
            rv.adapter = musicAdapter
            Dexter.withActivity(requireActivity())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                        if (response?.permissionName.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            if (musicList.isEmpty()) getContacts()
                        }
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                        requestPermissionForRecordAudio()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest?,
                        token: PermissionToken?
                    ) {
                        token?.continuePermissionRequest()
                    }
                }).check()
        }
        return binding.root
    }

    private fun getContacts() {
        val musicResolver: ContentResolver? = context?.contentResolver
        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val musicCursor: Cursor? = musicResolver?.query(musicUri, null, null, null, null)

        if (musicCursor != null && musicCursor.moveToFirst()) {
            val titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val musicPath = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val musicPhoto = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
            val duration = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
            do {
                val thisTitle = musicCursor.getString(titleColumn)
                val thisArtist = musicCursor.getString(artistColumn)
                val thisPath = musicCursor.getString(musicPath)
                val image = musicCursor.getLong(musicPhoto).toString()
                val thisDuration = musicCursor.getLong(duration)
                val uri = Uri.parse("content://media/external/audio/albumart")
                val thisImage = Uri.withAppendedPath(uri, image).toString()
                val model = MusicModel(
                    author = thisArtist,
                    name = thisTitle,
                    duration = thisDuration,
                    imagePath = thisImage,
                    musicPath = thisPath
                )
                appDatabase.musicDao().addModel(model)
                musicList.add(model)
                musicAdapter.notifyDataSetChanged()
            } while (musicCursor.moveToNext())
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun requestPermissionForRecordAudio() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            val dialog = AlertDialog.Builder(requireActivity())
            dialog.setTitle("Musiqalarni o'qib olish")
            dialog.setMessage("Musiqa tinglash uchun dasturga ruxsat berish kerak")
            dialog.setPositiveButton(
                "Ruxsat berish"
            ) { p0, p1 ->
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    2
                )
            }
            dialog.setNegativeButton("Ruxsat bermaslik") { p0, p1 -> p0?.dismiss() }
            dialog.show()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (musicList.isEmpty()) getContacts()
        } else if (requestCode == 2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (musicList.isEmpty()) getContacts()
            } else {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        }
    }
}