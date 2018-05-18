package com.example.dongmj.assistiveapplianceknobm4;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by dongmj on 4/12/2018.
 */

public class KnobAdapterWithFB extends RecyclerView.Adapter<KnobAdapterWithFB.ViewHolder> {

    final static String NAME_KEY = "NAME_KEY";
    public static final String KEY_KEY = "KEY_KEY";
    final static String MAC_KEY = "MAC_KEY";
    final static String POS_KEY = "POS_KEY";
    final static String ALPHA_KEY = "ALPHA_KEY";
    final static String BETA_KEY = "BETA_KEY";
    final static String GAMMA_KEY = "GAMMA_KEY";
    final static String PHONE_KEY = "PHONE_KEY";
    final static int REQUEST_CODE = 1;

    final ArrayList<Knob> mKnobs = new ArrayList<Knob>();
    final Context mContext;
    public DatabaseReference mDBRef;

    @SuppressLint("MissingPermission")
    public KnobAdapterWithFB(Context context) {
        mContext = context;
        final TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        mDBRef = FirebaseDatabase.getInstance().getReference().child(tm.getDeviceId());
        mDBRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Knob knob = new Knob((String)dataSnapshot.child(NAME_KEY).getValue(), (String)dataSnapshot.child(MAC_KEY).getValue());
                knob.mKey = dataSnapshot.getKey();
                mKnobs.add(knob);
                notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                Knob updated = new Knob((String)dataSnapshot.child(NAME_KEY).getValue(), (String)dataSnapshot.child(MAC_KEY).getValue());;
                for(Knob knob : mKnobs){
                    if(knob.mKey.equals(key)){
                        knob.mName = updated.mName;
                        knob.mMACAddress = updated.mMACAddress;
                        notifyDataSetChanged();
                        return;
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                for(Knob knob : mKnobs){
                    if(knob.mKey.equals(key)){
                        mKnobs.remove(knob);
                        notifyDataSetChanged();
                        return;
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void addKnob(Knob knob){
//        mKnobs.add(knob);
        DatabaseReference knobRef = mDBRef.push();
        knobRef.child(NAME_KEY).setValue(knob.mName);
        knobRef.child(MAC_KEY).setValue(knob.mMACAddress);
        notifyDataSetChanged();
    }

    public void clearKnobs(){
        for(Knob knob : mKnobs){
            Log.d("K", knob.mKey);
            mDBRef.child(knob.mKey).removeValue();
        }
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