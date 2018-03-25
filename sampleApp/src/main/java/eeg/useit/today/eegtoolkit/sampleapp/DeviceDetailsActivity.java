package eeg.useit.today.eegtoolkit.sampleapp;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.choosemuse.libmuse.Eeg;
import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.Battery;
import com.choosemuse.libmuse.MuseDataPacket;
import com.choosemuse.libmuse.MuseDataPacketType;
import com.choosemuse.libmuse.MuseListener;
import com.choosemuse.libmuse.MuseManagerAndroid;

import eeg.useit.today.eegtoolkit.common.BaseDataPacketListener;
import eeg.useit.today.eegtoolkit.common.FrequencyBands.Band;
import eeg.useit.today.eegtoolkit.common.FrequencyBands.ValueType;
import eeg.useit.today.eegtoolkit.sampleapp.databinding.ActivityDeviceDetailsBinding;
import eeg.useit.today.eegtoolkit.view.ConnectionStrengthView;
import eeg.useit.today.eegtoolkit.view.graph.GraphGLView;
import eeg.useit.today.eegtoolkit.view.graph.GraphSurfaceView;
import eeg.useit.today.eegtoolkit.vm.ConnectionStrengthViewModel;
import eeg.useit.today.eegtoolkit.vm.FrequencyBandViewModel;
import eeg.useit.today.eegtoolkit.vm.RawChannelViewModel;
import eeg.useit.today.eegtoolkit.vm.SensorGoodViewModel;
import eeg.useit.today.eegtoolkit.vm.StreamingDeviceViewModel;
import eeg.useit.today.eegtoolkit.model.TimeSeries;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import static java.lang.Double.NaN;

/**
 * Example activity that displays live details for a connected device.
 * Includes isGood connection status, and scrolling line graphs using surface and GL views.
 */
public class DeviceDetailsActivity extends AppCompatActivity {
  public static int DURATION_SEC = 5;

  /** The live device VM backing this view. */
  private final StreamingDeviceViewModel deviceVM = new StreamingDeviceViewModel();
  static FrequencyBandViewModel deviceTHETA;
  static FrequencyBandViewModel deviceDELTA;
  static FrequencyBandViewModel deviceALPHA;
  static FrequencyBandViewModel deviceBETA;
  SensorGoodViewModel isGoodVM;

  CountDownTimer countDownTimer;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Initialize Muse first up.
    MuseManagerAndroid.getInstance().setContext(this);

    // Bind viewmodel to the view.
    ActivityDeviceDetailsBinding binding =
            DataBindingUtil.setContentView(this, R.layout.activity_device_details);
    binding.setDeviceVM(deviceVM);
    binding.setIsGoodVM(new SensorGoodViewModel(deviceVM));
    binding.setConnectionVM(new ConnectionStrengthViewModel(deviceVM));
    binding.setRawVM(  deviceVM.createRawTimeSeries(Eeg.EEG3, DURATION_SEC));
    binding.setThetaVM(deviceVM.createFrequencyLiveValue(Band.THETA, ValueType.SCORE));
    binding.setDeltaVM(deviceVM.createFrequencyLiveValue(Band.DELTA, ValueType.SCORE));
    binding.setAlphaVM(deviceVM.createFrequencyLiveValue(Band.ALPHA, ValueType.SCORE));
    binding.setBetaVM( deviceVM.createFrequencyLiveValue(Band.BETA,  ValueType.SCORE));

    // IDK whether to do score or relative
    Log.d("Top","Restarted");
    deviceTHETA = (deviceVM.createFrequencyLiveValue(Band.THETA, ValueType.RELATIVE));
    deviceDELTA = (deviceVM.createFrequencyLiveValue(Band.DELTA, ValueType.RELATIVE));
    deviceALPHA = (deviceVM.createFrequencyLiveValue(Band.ALPHA, ValueType.RELATIVE));
    deviceBETA = (deviceVM.createFrequencyLiveValue(Band.BETA, ValueType.RELATIVE));

    // Bind action bar, seems like this can't be done in the layout 😞
    deviceVM.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
      @Override
      public void onPropertyChanged(Observable sender, int propertyId) {
        DeviceDetailsActivity.this.getSupportActionBar().setTitle(
                String.format("%s: %s", deviceVM.getName(), deviceVM.getConnectionState())
        );
      }
    });

    isGoodVM = new SensorGoodViewModel(deviceVM);

    // And attach the desired muse to the VM once connected.
    final String macAddress = getIntent().getExtras().getString("mac");
    if (macAddress != null) {
      MuseManagerAndroid.getInstance().startListening();
      MuseManagerAndroid.getInstance().setMuseListener(new MuseListener() {
        @Override
        public void museListChanged() {
          for (Muse muse : MuseManagerAndroid.getInstance().getMuses()) {
            if (macAddress.equals(muse.getMacAddress())) {
              DeviceDetailsActivity.this.deviceVM.setMuse(muse);
              MuseManagerAndroid.getInstance().stopListening();
              break;
            }
          }
        }
      });
    }

    final Button button = (Button)findViewById(R.id.startButton);
    final TextView battery = (TextView)findViewById(R.id.textView8);
    countDownTimer = new CountDownTimer(600000, 1000) {

      public void onTick(long millisUntilFinished) {
        battery.setText(String.valueOf((int)deviceBETA.getBattery()) + "%");
        if(isGoodVM.getConnected()[0] && isGoodVM.getConnected()[1] && isGoodVM.getConnected()[2] && isGoodVM.getConnected()[3]) {
          button.setText("Ready");
        } else {
          button.setText("Adjust Muse");
        }
        if(getEngagement() != NaN && getEngagement() < 0.3) {
          //Log.d("Engagement", String.valueOf(getEngagement()));
          //vibrate();
        } else if (getEngagement() >= 0.3){
          //Log.d("Engagement", "good");
        } else {
          //Log.d("Engagement", "Not connected");
          //vibrate();
        }
      }

      public void onFinish() {
        Log.d("Engagement","Finished");
      }
    }.start();

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.moreViews) {
      Intent intent = new Intent(this, MoreDeviceDetailsActivity.class);
      intent.putExtra("mac", this.deviceVM.getMacAddress());
      this.startActivity(intent);
      return true;
    } else if (item.getItemId() == R.id.recordOption) {
      Intent intent = new Intent(this, RecordActivity.class);
      intent.putExtra("mac", this.deviceVM.getMacAddress());
      this.startActivity(intent);
      return true;
    } else if (item.getItemId() == R.id.getValues) {
      getEngagement();

      // 1. Instantiate an AlertDialog.Builder with its constructor
      AlertDialog.Builder builder = new AlertDialog.Builder(this);

      builder.setTitle("Notification Box");
      builder.setMessage("Test: This is a test notification to help focus");
      // Add the buttons
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

      // Create the AlertDialog
      AlertDialog dialog = builder.create();
      //dialog.show();
      /*
      MediaPlayer mPlayer = MediaPlayer.create(this, R.raw.sound);
      mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
      mPlayer.start();
      */
      // Vibrate
      Handler handler = new Handler();
      handler.postDelayed(new Runnable() {
        public void run() {
          vibrate();
        }
      }, 10000);

      return true;
    }
    return false;
  }

  public static double getEngagement() {
    double engagement = deviceBETA.getAverage() / (deviceALPHA.getAverage() + deviceTHETA.getAverage());
    Log.d("Values", "TEST");
    Log.d("Theta", String.valueOf(deviceTHETA.getAverage()));
    //Log.d("Delta", String.valueOf(deviceDELTA.getAverage()));
    //Log.d("Alpha", String.valueOf(deviceALPHA.getAverage()));
    //Log.d("Beta", String.valueOf(deviceBETA.getAverage()));
    //Log.d("Battery", String.valueOf(deviceBETA.getBattery()));
    Log.d("Engagement", String.valueOf(engagement));
    return engagement;
  }

  public static double getDrowsiness() {
    double drowsiness = deviceTHETA.getAverage();
    //Log.d("Values", "TEST");
    //Log.d("Theta", String.valueOf(deviceTHETA.getAverage()));
    //Log.d("Delta", String.valueOf(deviceDELTA.getAverage()));
    //Log.d("Alpha", String.valueOf(deviceALPHA.getAverage()));
    //Log.d("Beta", String.valueOf(deviceBETA.getAverage()));
    //Log.d("Battery", String.valueOf(deviceBETA.getBattery()));
    //Log.d("Engagement", String.valueOf(engagement));
    return drowsiness;
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

  public void goToMain (View view){
    if(isGoodVM.getConnected()[0] && isGoodVM.getConnected()[1] && isGoodVM.getConnected()[2] && isGoodVM.getConnected()[3]) {
      countDownTimer.cancel();
      startActivity(new Intent(DeviceDetailsActivity.this, MainActivity.class));
    }
  }
}