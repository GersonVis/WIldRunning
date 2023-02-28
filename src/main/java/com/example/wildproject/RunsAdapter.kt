package com.example.wildproject

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.recyclerview.widget.RecyclerView
import com.example.wildproject.Utility.setHeightLinearLayout

class RunsAdapter(private val runsList: ArrayList<Runs>) :
    RecyclerView.Adapter<RunsAdapter.MyViewHolder>() {
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunsAdapter.MyViewHolder {
        context = parent.context
        val itemView = LayoutInflater.from(context).inflate(R.layout.card_run, parent, false)
     return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RunsAdapter.MyViewHolder, position: Int) {
        val run: Runs = runsList[position]

        setHeightLinearLayout(holder.lyDataRunBody, 0)
    }

    override fun getItemCount(): Int {
        return runsList.size
    }
    public class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
     val lyDataRunHeader: LinearLayout = itemView.findViewById(R.id.lyDataRunHeader)
     val tvHeaderDate: TextView = itemView.findViewById(R.id.tvHeaderDate)
     val tvHeaderDuration: TextView = itemView.findViewById(R.id.tvHeaderDuration)
     val tvHeaderDistance: TextView = itemView.findViewById(R.id.tvHeaderDistance)
     val tvHeaderAvgSpeed: TextView = itemView.findViewById(R.id.tvHeaderAvgSpeed)
     val ivHeaderMedalDistance: ImageView = itemView.findViewById(R.id.ivHeaderMedalDistance)
     val ivHeaderMedalAvgSpeed: ImageView = itemView.findViewById(R.id.ivHeaderMedalAvgSpeed)
     val ivHeaderMedalMaxSpeed: ImageView = itemView.findViewById(R.id.ivHeaderMedalMaxSpeed)
     val ivHeaderOpenClose: ImageView = itemView.findViewById(R.id.ivHeaderOpenClose)

     val lyDataRunBody: LinearLayout = itemView.findViewById(R.id.lyDataRunBody)
     val lyDataRunBodyContainer: LinearLayout = itemView.findViewById(R.id.lyDataRunBodyContainer)

     val tvDate: TextView = itemView.findViewById(R.id.tvDate)
     val tvStartTime: TextView = itemView.findViewById(R.id.tvStartTime)


     val tvDurationRun: TextView = itemView.findViewById(R.id.tvDurationRun)
     val lyChallengeDurationRun: LinearLayout = itemView.findViewById(R.id.lyChallengeDurationRun)
     val tvChallengeDurationRun: TextView = itemView.findViewById(R.id.tvChallengeDurationRun)
     val lyIntervalRun: LinearLayout = itemView.findViewById(R.id.lyIntervalRun)
     val tvIntervalRun: TextView = itemView.findViewById(R.id.tvIntervalRun)


     val tvDistanceRun: TextView = itemView.findViewById(R.id.tvDistanceRun)
     val lyChallengeDistance: LinearLayout = itemView.findViewById(R.id.lyChallengeDistance)
     val tvChallengeDistanceRun: TextView = itemView.findViewById(R.id.tvChallengeDistanceRun)
     val lyUnevennessRun: LinearLayout = itemView.findViewById(R.id.lyUnevennessRun)
     val tvMaxUnevennessRun: TextView = itemView.findViewById(R.id.tvMaxUnevennessRun)
     val tvMinUnevennessRun: TextView = itemView.findViewById(R.id.tvMinUnevennessRun)


     val tvAvgSpeedRun: TextView = itemView.findViewById(R.id.tvAvgSpeedRun)
     val tvMaxSpeedRun: TextView = itemView.findViewById(R.id.tvMaxSpeedRun)

     val ivMedalDistance: ImageView = itemView.findViewById(R.id.ivMedalDistance)
     val tvMedalDistanceTitle: TextView = itemView.findViewById(R.id.tvMedalDistanceTitle)
     val ivMedalAvgSpeed: ImageView = itemView.findViewById(R.id.ivMedalAvgSpeed)
     val tvMedalAvgSpeedTitle: TextView = itemView.findViewById(R.id.tvMedalAvgSpeedTitle)
     val ivMedalMaxSpeed: ImageView = itemView.findViewById(R.id.ivMedalMaxSpeed)
     val tvMedalMaxSpeedTitle: TextView = itemView.findViewById(R.id.tvMedalMaxSpeedTitle)


     val ivPicture: ImageView = itemView.findViewById(R.id.ivPicture)

     val lyPicture: LinearLayout = itemView.findViewById(R.id.lyPicture)
     val tvPlay: TextView = itemView.findViewById(R.id.tvPlay)
     val tvDelete: TextView = itemView.findViewById(R.id.tvDelete)
    }

}