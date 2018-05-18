package com.example.dongmj.assistiveapplianceknobm4;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

public class KnobListWithFBActivity extends AppCompatActivity {

    public static final String NAMES = "NAMES";
    public static final String ADDRESSES = "ADDRESSES";

    private KnobAdapterWithFB mAdapter;
    private DatabaseReference mUserRef;
    private String mID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_knob_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_knob_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addKnob();
            }
        });

        mAdapter = new KnobAdapterWithFB(this);
        RecyclerView recycleView = (RecyclerView)findViewById(R.id.recycle_view);
        recycleView.setLayoutManager(new LinearLayoutManager(this));
        recycleView.setHasFixedSize(true);
        recycleView.setAdapter(mAdapter);

//        SharedPreferences sp = getSharedPreferences("PREFS", MODE_PRIVATE);
//        ArrayList<Knob> knobs = new ArrayList<Knob>();
//        SharedPreferences.Editor edot = sp.edit();
//        JSONArray jsonNames = null;
//        JSONArray jsonAddresses = null;
//        try {
//            jsonNames = new JSONArray(sp.getString(NAMES, "[]"));
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        try {
//            jsonAddresses = new JSONArray(sp.getString(ADDRESSES, "[]"));
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        for (int i = 0; i < jsonNames.length(); i++) {
//            try {
//                mAdapter.addKnob(new Knob((String) jsonNames.get(i), (String)jsonAddresses.get(i)));
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
    }

    public void addKnob(){
        AlertDialog.Builder builder = new AlertDialog.Builder(KnobListWithFBActivity.this);
        final View view = getLayoutInflater().inflate(R.layout.new_knob_dialog, null);
        builder.setView(view);

        final EditText nameEditText = (EditText)view.findViewById(R.id.edit_name);
        final EditText macEditText = (EditText)view.findViewById(R.id.edit_mac);
        macEditText.setText(R.string.default_mac);

        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //SAVE
                Toast.makeText(getBaseContext(), getString(R.string.save_message) + " " + nameEditText.getText().toString(), Toast.LENGTH_SHORT).show();
                mAdapter.addKnob(new Knob(nameEditText.getText().toString(), macEditText.getText().toString()));
            }
        });
        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_knob_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_clear) {
            AlertDialog.Builder builder = new AlertDialog.Builder(KnobListWithFBActivity.this);
            builder.setTitle(R.string.action_clear);
            builder.setMessage(R.string.confirm_clear_message);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    clearAll();
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.create().show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void clearAll(){
//        SharedPreferences sp = getSharedPreferences("PREFS", MODE_PRIVATE);
//        sp.edit().clear();
//        sp.edit().commit();
//        mAdapter.clearKnobs();
        mAdapter.mKnobs.clear();
        mAdapter.mDBRef.removeValue();
        mAdapter.notifyDataSetChanged();
    }


}
