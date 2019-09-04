package io.github.takusan23.Kaisendon.FloatingTL

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import io.github.takusan23.Kaisendon.APIJSONParse.CustomMenuJSONParse
import io.github.takusan23.Kaisendon.APIJSONParse.MastodonTLAPIJSONParse
import io.github.takusan23.Kaisendon.PicassoImageGetter
import io.github.takusan23.Kaisendon.R
import okhttp3.*
import java.io.IOException

class KaisendonMiniRecyclerViewAdapter(private val arrayListArrayAdapter: ArrayList<ArrayList<*>>) :
        RecyclerView.Adapter<KaisendonMiniRecyclerViewAdapter.ViewHolder>() {

    var instance = ""
    var accessToken = ""

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //textview
        var userTextView: TextView
        var tootTextView: TextView
        var usetImageView: ImageView
        //fab/bt
        var favImageButton: ImageButton
        var btImageButton: ImageButton

        init {
            userTextView = itemView.findViewById(R.id.adapter_kaisendonmini_account_textview)
            tootTextView = itemView.findViewById(R.id.adapter_kaisendonmini_toot_textview)
            usetImageView = itemView.findViewById(R.id.adapter_kaisendonmini_account_imageview)

            favImageButton = itemView.findViewById(R.id.adapter_kaisendonmini_fav_button)
            btImageButton = itemView.findViewById(R.id.adapter_kaisendonmini_bt_button)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_kaisendonmini_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: ArrayList<String> = arrayListArrayAdapter[position] as ArrayList<String>
        val setting = CustomMenuJSONParse(item[9])

        accessToken = item[8]
        instance = item[7]

        val context = holder.tootTextView.context
        val api = MastodonTLAPIJSONParse(holder.userTextView.getContext(), item[3], setting, 20)

        //トゥート、名前
        val imageGetter = PicassoImageGetter(holder.tootTextView)
        holder.tootTextView.text = Html.fromHtml(api.toot_text, Html.FROM_HTML_MODE_COMPACT, imageGetter, null)
        holder.userTextView.text = "${api.display_name} @${api.user_ID}"

        //画像
        if (isWifiConnection(context)) {
            Picasso.get().load(api.avatarUrl).into(holder.usetImageView)
        } else {
            (holder.usetImageView.parent as LinearLayout).removeView(holder.usetImageView)
        }

        //いいね、ぶーすと！
        holder.favImageButton.setOnClickListener {
            postTootAction(api.toot_ID.toString(), accessToken, "favourite", holder)
        }
        holder.btImageButton.setOnClickListener {
            postTootAction(api.toot_ID.toString(), accessToken, "reblog", holder)
        }

        //ダークモードでImageViewにinitがかかるので修正
        holder.usetImageView.imageTintList=null
        holder.favImageButton.imageTintList=null
        holder.btImageButton.imageTintList=null

    }

    override fun getItemCount(): Int {
        return arrayListArrayAdapter.size
    }

    fun postTootAction(id: String, token: String, api: String, holder: ViewHolder) {

        val context = holder.tootTextView.context
        val url = "https://${instance}/api/v1/statuses/$id/$api"

        val formBody = FormBody.Builder()
                .addEncoded("access_token", token)
                .build()
        val request = Request.Builder()
                .url(url)
                .post(formBody)
                .build()
        val okHttpClient = OkHttpClient()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                //失敗時
                holder.tootTextView.post {
                    Toast.makeText(context, context.getString(R.string.error), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    var message = context.getString(R.string.boost_ok)
                    if (api.contains("favourite")) {
                        message = context.getString(R.string.favourite_add)
                    }
                    Toast.makeText(context, "$message : $id", Toast.LENGTH_SHORT).show()
                } else {
                    //失敗時
                    holder.tootTextView.post {
                        Toast.makeText(context, context.getString(R.string.error) + "\n" + response.code(), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    fun isWifiConnection(context: Context): Boolean {
        //画像
        val pref_setting = PreferenceManager.getDefaultSharedPreferences(context)
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (pref_setting.getBoolean("pref_avater_wifi", true)) {
            return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } else {
            return true
        }
        return false
    }

}