package eeg.useit.today.eegtoolkit.sampleapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.choosemuse.libmuse.Eeg;
import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseListener;
import com.choosemuse.libmuse.MuseManagerAndroid;
import com.mongodb.stitch.android.StitchClient;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import eeg.useit.today.eegtoolkit.common.FrequencyBands;
import eeg.useit.today.eegtoolkit.sampleapp.databinding.ActivityDeviceDetailsBinding;
import eeg.useit.today.eegtoolkit.vm.ConnectionStrengthViewModel;
import org.bson.Document;

import eeg.useit.today.eegtoolkit.vm.FrequencyBandViewModel;
import eeg.useit.today.eegtoolkit.vm.SensorGoodViewModel;
import eeg.useit.today.eegtoolkit.vm.StreamingDeviceViewModel;
import com.mongodb.stitch.android.services.mongodb.MongoClient;

import static eeg.useit.today.eegtoolkit.sampleapp.DeviceDetailsActivity.getEngagement;
import static java.lang.Double.NaN;

public class RunningActivity extends AppCompatActivity implements StitchClientListener {
    SharedPreferences sharedpref;
    public static final String mypreference = "mypref";

    public static int DURATION_SEC = 5;

    /** The live device VM backing this view. */
    private final StreamingDeviceViewModel deviceVM = new StreamingDeviceViewModel();
    FrequencyBandViewModel deviceTHETA;
    FrequencyBandViewModel deviceDELTA;
    FrequencyBandViewModel deviceALPHA;
    FrequencyBandViewModel deviceBETA;
    SensorGoodViewModel isGoodVM;
    private StitchClient _client;
    private MongoClient _mongoClient;
    CountDownTimer countDownTimer;
    MongoClient.Collection collection;
    Document document;
    boolean isPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running);

        sharedpref = getSharedPreferences(mypreference, Context.MODE_PRIVATE);
        String username = sharedpref.getString("username", "");

        Log.d("TAG","Marker2");
        StitchClientManager.initialize(this.getApplicationContext());
        StitchClientManager.registerListener(this);
        document = new Document();
        final String title = username;
        document.append("name", title);
        _mongoClient = new MongoClient(_client, "mongodb-atlas");
        collection = _mongoClient.getDatabase("Users").getCollection("BrainData");


        countDownTimer = new CountDownTimer(600000, 1000) {
            int threshold = 20;
            double[] engagementVals = new  double[threshold];
            double[] drowsinessVals = new double[threshold];
            int aSlider = MainActivity.aSliderVal[0];
            int dSlider = MainActivity.dSliderVal[0];
            double slidingEngagementAverage;
            double slidingDrowisnessAverage;
            long itr = 0;

            public void onTick(long millisUntilFinished) {

                engagementVals[(int)(itr % threshold)] = getEngagement();
                drowsinessVals[(int)(itr % threshold)] = DeviceDetailsActivity.getDrowsiness();
                if(!isPaused) {
                    ProgressBar bar = (ProgressBar) findViewById(R.id.engagementProgress);
                    bar.setProgress((int) (getEngagement() * 100), true);
                    if ((int) (itr % threshold) == (threshold - 1)) {
                        slidingEngagementAverage = getSlidingAvg(engagementVals);
                        slidingDrowisnessAverage = getSlidingAvg(drowsinessVals);

                        if (slidingEngagementAverage != NaN && !isEngaged(aSlider,dSlider,slidingEngagementAverage,slidingDrowisnessAverage)) {
                            Log.d("Engagement", String.valueOf(slidingEngagementAverage));
                            //Alert builder
                            // 1. Instantiate an AlertDialog.Builder with its constructor
                            AlertDialog.Builder builder = new AlertDialog.Builder(RunningActivity.this);

                            int min = 0;
                            int max = 4;

                            Random r = new Random();
                            int i1 = r.nextInt(max - min + 1) + min;
                            String[] titles = {
                                    "Be actively engaged!",
                                    "Stay focused!",
                                    "Don't doze off!",
                                    "Are you paying attention?",
                                    "Wake up your brain!"
                            };

                            int min2 = 0;
                            int max2 = 4;

                            Random r2 = new Random();
                            int i2 = r2.nextInt(max2 - min2 + 1) + min2;
                            String[] sayings = {
                                    "In small doses, caffeine can help boost focus.",
                                    "Make a to-do list to concentrate your energy.",
                                    "Taking a five-minute break can help refocus your attention.",
                                    "Make sure to remove any possible distractions from the environment.",
                                    "Take a deep breath and stretch before you continue."
                            };

                            builder.setTitle(titles[i1]);
                            builder.setMessage(sayings[i2]);
                            document.append("name", title);
                            document.append("timestamp",System.currentTimeMillis());
                            document.append("attention", slidingEngagementAverage);
                            collection.insertOne(document);
                            builder.setTitle(titles[i1]);
                            builder.setMessage(sayings[i2]);
                            // Add the buttons
                            /*
                            builder.setPositiveButton("Positive", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User clicked OK button
                                }
                            });
                            builder.setNegativeButton("Negative", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User cancelled the dialog
                                }
                            });
                            */

                            // Create the AlertDialog
                            final AlertDialog dialog = builder.create();
                            dialog.show();

                            final Timer timer2 = new Timer();
                            timer2.schedule(new TimerTask() {
                                public void run() {
                                    dialog.dismiss();
                                    timer2.cancel(); //this will cancel the timer of the system
                                }
                            }, 10000); // the timer will count 5 seconds....

                            MediaPlayer mPlayer = MediaPlayer.create(RunningActivity.this, R.raw.sound);
                            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mPlayer.start();
                            //
                            // Vibrate
                            vibrate();
                        } else if (slidingEngagementAverage != NaN && isEngaged(aSlider,dSlider,slidingEngagementAverage,slidingDrowisnessAverage)){
                        } else if (slidingEngagementAverage >= 0.3) {
                            document.append("name", title);
                            document.append("timestamp",System.currentTimeMillis());
                            document.append("attention", slidingEngagementAverage);
                            collection.insertOne(document);
                            Log.d("Engagement", "good");
                        } else {
                            Log.d("Engagement", "Not connected");
                            //vibrate();
                        }
                    }
                }
                itr = itr + 1;
                Log.d("Tag",String.valueOf(aSlider));
                Log.d("Tag",String.valueOf(dSlider));
            }

            public void onFinish() {
                Log.d("Engagement","Finished");
            }
        }.start();


    }

    @Override
    public void onReady(StitchClient stitchClient) {
        // Perform any Stitch-dependent initialization here.

        // For example:
        if(!stitchClient.isAuthenticated()) {
            //TODO Make a message if not authenticated
        }
        // Or authenticate directly


        // If this Activity maintains a reference to a
        // StitchClient, populate it here
        this._client = stitchClient;

    }



    public void vibrate(){
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500,VibrationEffect.DEFAULT_AMPLITUDE));
        }else {
            //deprecated in API 26
            v.vibrate(500);
        }
    }

    public double getSlidingAvg(double[] engagementVals){
        double sum = 0.0;
        for(int i = 0; i < engagementVals.length; i++){
            sum += engagementVals[i];
        }
        return sum/engagementVals.length;
    }

    public void goToMain (View view){
        countDownTimer.cancel();
        startActivity(new Intent(RunningActivity.this, ListDevicesActivity.class));
    }

    public void pauseTimer (View view){
        if(!isPaused) {
            isPaused = true;
            Button button = (Button) findViewById(R.id.button3);
            button.setText("Unpause");

        } else {
            isPaused = false;
            Button button = (Button) findViewById(R.id.button3);
            button.setText("Pause");

        }
    }

    public boolean isEngaged(int aSlider, int dSlider, double slidingEngagementAverage, double slidingDrowisnessAverage){

        return slidingEngagementAverage >= 0.2 + (0.5 * 0.01 * (double)aSlider) && slidingDrowisnessAverage <= 0.5 - (0.10 * 0.01 * (double)dSlider);

    }
}
