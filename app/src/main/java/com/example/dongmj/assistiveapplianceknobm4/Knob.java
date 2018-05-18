package com.example.dongmj.assistiveapplianceknobm4;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by dongmj on 4/12/2018.
 */

public class Knob {

    public String mName;
    public String mMACAddress;
    public HashMap<Double[], String> mPositions;
    public Double[] eulerAngles;
    public String mKey;
    private final double angThresh = 10;
    private DatabaseReference mKnobRef;

    public Knob(String name, String macAddress){
        this.mMACAddress = macAddress;
        this.mName = name;
        this.mPositions = new HashMap<Double[], String>();
        this.eulerAngles = new Double[] {0.0, 0.0, 0.0};
        mKey = "0";
    }

    public void newPosition(double alpha, double beta, double gamma, String name){
        Double[] positionList = new Double[3];
        positionList[0] = alpha;
        positionList[1] = beta;
        positionList[2] = gamma;
        this.mPositions.put(positionList, name);
        return;
    }

    public void saveCurrentPosition(String name){
        Double[] currentPos = new Double[] {eulerAngles[0], eulerAngles[1], eulerAngles[2]};
        DatabaseReference posRef = mKnobRef.child(KnobAdapterWithFB.POS_KEY).child(name);
        posRef.setValue(eulerAngles[0] + " " + eulerAngles[1] + " " + eulerAngles[2]);
        this.mPositions.put(currentPos, name);
        return;
    }

    public void parseStringTest(String s){
        try{
            eulerAngles[0] = Double.parseDouble(s.substring(0, s.indexOf(".")));
            s = s.substring(s.indexOf(" ") + 1);
            eulerAngles[1] = Double.parseDouble(s.substring(0, s.indexOf(".")));
            s = s.substring(s.indexOf(" ") + 1);
            eulerAngles[2] = Double.parseDouble(s.substring(0, s.indexOf(".")));
            return;
        }
        catch (StringIndexOutOfBoundsException e){
            Log.e("E", e.getMessage());
            eulerAngles = new Double[]{0.0, 0.0, 0.0};
            return;
        }
    }

    public void parseString(String s){
        try {
            eulerAngles[0] = Double.parseDouble(s.substring(0, s.indexOf(".")));
            s = s.substring(s.indexOf(".") + 3);
            eulerAngles[1] = Double.parseDouble(s.substring(0, s.indexOf(".")));
            s = s.substring(s.indexOf(".") + 3);
            eulerAngles[2] = Double.parseDouble(s.substring(0, s.indexOf(".")));
        }
        catch (java.lang.StringIndexOutOfBoundsException e){
            Log.e("E", e.toString());
        }
        return;
    }

    public String getName(){
        return this.mName;
    }

    public String getMacAddress(){
        return this.mMACAddress;
    }

    public double getAlpha(){
        return eulerAngles[0];
    }

    public double getBeta(){
        return eulerAngles[1];
    }

    public double getGamma(){
        return eulerAngles[2];
    }

    @Override
    public String toString(){
        return this.mName;
    }

    public String getPosition() {
        Set<Double[]> posList = this.mPositions.keySet();
        for(Double[] pos : posList){
            //Log.d(mPositions.get(pos), "alpha: " + pos[0] + " beta: " + pos[1] + " gamma: " + pos[2]);
//            if(Math.abs(Math.abs(eulerAngles[0]) - Math.abs(pos[0])) < this.angThresh && Math.abs(Math.abs(eulerAngles[1]) - Math.abs(pos[1])) < this.angThresh && Math.abs(Math.abs(eulerAngles[2]) - Math.abs(pos[2])) < this.angThresh){

            if(Math.abs(eulerAngles[0]- pos[0]) < this.angThresh && Math.abs(eulerAngles[1] - pos[1]) < this.angThresh && Math.abs(eulerAngles[2] - pos[2]) < this.angThresh){
                return this.mPositions.get(pos);
            }
        }
        return "Unregistered Position";
    }

    public boolean addPosition(String name, Double[] pos){
        if(mPositions.containsKey(pos)){
            return false;
        }
        mPositions.put(pos, name);
        return true;
    }

    public void setAllFields(String phoneId){
        mKnobRef = FirebaseDatabase.getInstance().getReference().child(phoneId).child(mKey);
        mKnobRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("K", "Looking for key: " + dataSnapshot.getKey());
                switch(dataSnapshot.getKey()){
                    case KnobAdapterWithFB.NAME_KEY:
                        mName = (String) dataSnapshot.getValue();
                        return;
                    case KnobAdapterWithFB.MAC_KEY:
                        mMACAddress = (String) dataSnapshot.getValue();
                        return;
                    case KnobAdapterWithFB.POS_KEY:
                        //TODO: SET POSITIONS
                        Iterable<DataSnapshot> positions = dataSnapshot.getChildren();
                        Log.d("K", "Loading positions");
                        for(DataSnapshot position : positions){
                            String posString = (String) position.getValue() + " ";
                            Double[] pos = new Double[3];
                            for(int i = 0; i < 3; i++){
                                Log.d("K", "i = " + i);
                                Log.d("K", posString);
                                Log.d("K", "indexOf() " + posString.indexOf(" "));
                                pos[i] = Double.parseDouble(posString.substring(0, posString.indexOf(" ")));
                                posString = posString.substring(posString.indexOf(" ") + 1);
                            }
                            mPositions.put(pos, position.getKey());
                            Log.d("K", "Loading: " + position.getKey() + ", " + pos.toString());
                        }
                        Log.d("K", "Loaded " + mPositions.size() + " position(s)");
                        return;
                    default:
                        Log.e("K", "Unrecognized key: " + dataSnapshot.getKey());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Log.d("K", "Loading knob " + mName);
    }
}
