package com.example.dongmj.assistiveapplianceknobm4;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by dongmj on 4/12/2018.
 */

public class KnobAdapter extends RecyclerView.Adapter<KnobAdapter.ViewHolder> {

    final static String NAME_KEY = "NAME_KEY";
    final static String MAC_KEY = "MAC_KEY";
    final static String POS_KEY = "POS_KEY";
    final static String ALPHA_KEY = "ALPHA_KEY";
    final static String BETA_KEY = "BETA_KEY";
    final static String GAMMA_KEY = "GAMMA_KEY";
    final static int REQUEST_CODE = 1;

    final ArrayList<Knob> mKnobs = new ArrayList<Knob>();
    final Context mContext;
    public RecyclerView mRecyclerView;

    public KnobAdapter(Context context){
        mContext = context;
    }

    public void addKnob(Knob knob){
        mKnobs.add(knob);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.knob_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mName.setText(mKnobs.get(position).mName);
        holder.mMacAddress.setText(mKnobs.get(position).mMACAddress);
        holder.mCard.setCardBackgroundColor(mContext.getResources().getColor(R.color.colorPrimaryLight));
    }

    @Override
    public int getItemCount() {
        return mKnobs.size();
    }

    @SuppressLint("MissingPermission")
    public void openActivity(Knob knob){
        Intent intent = new Intent(mContext, KnobInfoActivity.class);
        final TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        KnobInfoActivity.addKnobToIntent(intent, knob, tm.getDeviceId());
        ((Activity) mContext).startActivityForResult(intent, REQUEST_CODE);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView mName;
        public TextView mMacAddress;
        public CardView mCard;

        public ViewHolder(View itemView) {
            super(itemView);
            mName = itemView.findViewById(R.id.knob_name);
            mMacAddress = itemView.findViewById(R.id.mac_address);
            mCard = itemView.findViewById(R.id.knob_card);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openActivity(mKnobs.get(getAdapterPosition()));
                }
            });
        }
    }
}
