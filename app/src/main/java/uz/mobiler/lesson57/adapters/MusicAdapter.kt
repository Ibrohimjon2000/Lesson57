package uz.mobiler.lesson57.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import uz.mobiler.lesson57.R
import uz.mobiler.lesson57.database.entity.MusicModel

class MusicAdapter(
    val context: Context,
    var musicList: ArrayList<MusicModel>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<MusicAdapter.Vh>() {

    inner class Vh(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.name)
        val author: TextView = itemView.findViewById(R.id.author)
        val image: ImageView = itemView.findViewById(R.id.img)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(LayoutInflater.from(context).inflate(R.layout.music_item, parent, false))
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        val musicModel = musicList[position]
        holder.name.text = musicModel.name
        holder.author.text = musicModel.author
        Glide.with(context)
            .load(musicList[position].imagePath)
            .apply(RequestOptions().placeholder(R.drawable.mobiler).centerCrop())
            .into(holder.image)
        holder.itemView.setOnClickListener {
            listener.onItemClick(musicModel, position)
        }
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    interface OnItemClickListener {
        fun onItemClick(musicModel: MusicModel, position: Int)
    }
}
