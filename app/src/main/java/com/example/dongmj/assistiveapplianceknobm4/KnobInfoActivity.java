package com.example.dongmj.assistiveapplianceknobm4;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.speech.tts.Voice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

public class KnobInfoActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private final String DEVICE_ADDRESS="98:D3:31:FD:6F:90";
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    Button startButton, sendButton, saveButton, stopButton;
    TextView angleText;
    TextView nameText;
    TextView positionText;
    EditText editPosition;
    boolean deviceConnected=false;
    Thread thread;
    byte buffer[];
    int bufferPosition;
    boolean stopThread;
    StringBuilder mSBuilder;
    String mText;
    Knob mKnob;
    String positionString = "Unregistered Position";
    TextToSpeech mTTS;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSBuilder = new StringBuilder();
        setContentView(R.layout.activity_knob_info);
        startButton = (Button) findViewById(R.id.buttonStart);
        sendButton = (Button) findViewById(R.id.buttonSend);
        saveButton = (Button) findViewById(R.id.button_save);
        stopButton = (Button) findViewById(R.id.buttonStop);
        nameText = (TextView) findViewById(R.id.name_text);
        angleText = (TextView) findViewById(R.id.angle_text);
        positionText = (TextView) findViewById(R.id.position_text);
        editPosition = (EditText) findViewById(R.id.edit_name);
        setUiEnabled(false);
        Intent intent = getIntent();
        String name = intent.getStringExtra(KnobAdapterWithFB.NAME_KEY);
        String key = intent.getStringExtra(KnobAdapterWithFB.KEY_KEY);
        mKnob = new Knob(name, "");
        mKnob.mKey = key;
        mKnob.setAllFields(intent.getStringExtra(KnobAdapterWithFB.PHONE_KEY));
        if(mKnob == null){
            Log.e("E", "Knob " + name + " not found");
            mKnob = new Knob("Temp Knob", DEVICE_ADDRESS);
        }
        nameText.setText(mKnob.getName());
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, 0);
    }

    public void setUiEnabled(boolean bool)
    {
        startButton.setEnabled(!bool);
        sendButton.setEnabled(bool);
        stopButton.setEnabled(bool);
        angleText.setEnabled(bool);

    }

    public boolean BTinit()
    {
        boolean found=false;
        BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"Device doesnt Support Bluetooth",Toast.LENGTH_SHORT).show();
        }
        if(!bluetoothAdapter.isEnabled())
        {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if(bondedDevices.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"Please Pair the Device first",Toast.LENGTH_SHORT).show();
        }
        else
        {
            for (BluetoothDevice iterator : bondedDevices)
            {
                if(iterator.getAddress().equals(mKnob.getMacAddress()))
                {
                    device=iterator;
                    found=true;
                    break;
                }
            }
        }
        return found;
    }

    @Override
    public void onBackPressed() {
        closeActivity();
        stopThread = true;
        try {
            if(outputStream != null) {
                outputStream.close();
            }
            if(inputStream != null) {
                inputStream.close();
            }
            if(socket != null) {
                socket.close();
            }
        }
        catch (IOException e){
            Log.e("E", e.getMessage());
        }
        setUiEnabled(false);
        deviceConnected=false;
        angleText.setText("\nConnection Closed!\n");
        mTTS.shutdown();
        super.onBackPressed();
    }

    public void closeActivity(){
        //DONE: Pass the knob back
        Intent returnIntent = new Intent();
//        addKnobToIntent(returnIntent, mKnob);
        setResult(KnobAdapter.REQUEST_CODE, returnIntent);
        finish();

    }

    public static void addKnobToIntent(Intent intent, Knob knob, String deviceId){
        intent.putExtra(KnobAdapterWithFB.NAME_KEY, knob.mName);
        intent.putExtra(KnobAdapterWithFB.KEY_KEY, knob.mKey);
        intent.putExtra(KnobAdapterWithFB.PHONE_KEY, deviceId);
//        intent.putExtra(KnobAdapter.MAC_KEY, knob.mMACAddress);
//        intent.putExtra(KnobAdapter.POS_KEY, knob.mPositions.values().toArray());
//        ArrayList<Double> alpha = new ArrayList<>();
//        ArrayList<Double> beta = new ArrayList<>();
//        ArrayList<Double> gamma = new ArrayList<>();
//        Iterator<Double[]> iter = knob.mPositions.keySet().iterator();
//        while(iter.hasNext()){
//            Double[] angs = iter.next();
//            alpha.add(angs[0]);
//            beta.add(angs[0]);
//            gamma.add(angs[0]);
//        }
//        intent.putExtra(KnobAdapter.ALPHA_KEY, alpha.toArray());
//        intent.putExtra(KnobAdapter.BETA_KEY, beta.toArray());
//        intent.putExtra(KnobAdapter.GAMMA_KEY, gamma.toArray());
    }

    public boolean BTconnect()
    {
        boolean connected=true;
        try {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            connected=false;
        }
        if(connected)
        {
            try {
                outputStream=socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream=socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return connected;
    }

    public void onClickStart(View view) {
        Log.d("B", "StartButton");
        boolean btinit = BTinit();
        Log.d("B", "BTinit(): " + btinit);
        if(btinit)
        {
            if(BTconnect())
            {
                setUiEnabled(true);
                deviceConnected=true;
                beginListenForData();
                angleText.setText("\nConnection Opened!\n");
            }

        }
    }

    void beginListenForData()
    {
        final Handler handler = new Handler();
        stopThread = false;
        buffer = new byte[1024];
        Thread thread  = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopThread)
                {
                    try
                    {
                        int byteCount = inputStream.available();
                        if(byteCount > 0)
                        {
                            byte[] rawBytes = new byte[byteCount];
                            inputStream.read(rawBytes);
                            final String string=new String(rawBytes,"UTF-8");
                            mSBuilder.append(string);
                            if(mSBuilder.length() > 42){
                                int delimInd = mSBuilder.indexOf("r");
                                if(mSBuilder.length() - delimInd > 25) {
                                    if(mSBuilder.toString().contains("ing") || mSBuilder.toString().contains("rupt")){
                                        mSBuilder.setLength(0);
                                    }else {
                                        try {
                                            mText = mSBuilder.substring(delimInd + 2, delimInd + 21);
                                        }
                                        catch (StringIndexOutOfBoundsException e){
                                            mSBuilder.setLength(0);
                                        }
                                        mSBuilder.setLength(0);
                                    }
                                }

                            }
                            handler.post(new Runnable() {
                                public void run()
                                {
//                                    textView.append(string);
                                    if(mText != null) {
//                                        Log.d("R", "value recieved");
                                        mKnob.parseString(mText);
                                        angleText.setText("alpha: " + mKnob.getAlpha() + " beta: " + mKnob.getBeta() + " gamma: " + mKnob.getGamma());
                                        positionText.setText(positionString);
                                    }
                                    if(mSBuilder.length() > 43){
                                        Log.e("R", "delimiter not found");
                                        Log.e("R", "DATA: " + mSBuilder.toString());
//                                        textView.setText("Delimiter Not Found");
                                        mSBuilder.setLength(0);
                                    }
                                }
                            });
                            positionString = mKnob.getPosition();
//                            positionText.setText(mKnob.getPosition());
//                            checkPosition();
                        }
                    }
                    catch (IOException ex)
                    {
                        stopThread = true;
                    }
                }
            }
        });

        thread.start();
    }

    public void checkPosition(){
        positionText.setText(mKnob.getPosition());
    }

    public void onClickSend(View view) {
        String s = "START";
        try{
            outputStream.write(s.getBytes());
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void onClickStop(View view) throws IOException {
        stopThread = true;
        outputStream.close();
        inputStream.close();
        socket.close();
        setUiEnabled(false);
        deviceConnected=false;
        angleText.setText("\nConnection Closed!\n");
    }

    public void onClickSave(View view) {
        mKnob.saveCurrentPosition(editPosition.getText().toString());
        editPosition.setText("");
        return;
    }

    public void onClickPlay(View view){
        Log.d("C", "Play Button Clicked");
        if(mTTS != null) {
            mTTS.speak(positionString, TextToSpeech.QUEUE_FLUSH, null);
        }else{
            Log.e("E", "No TTS object");
        }
        return;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                mTTS = new TextToSpeech(this, this);
            }
            else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }

    @Override
    public void onInit(int i) {
        if(i == TextToSpeech.SUCCESS){
            mTTS.setLanguage(Locale.US);
        }
        else if (i == TextToSpeech.ERROR) {
            Toast.makeText(this, "TTS is not supported", Toast.LENGTH_LONG).show();
        }
    }
}
