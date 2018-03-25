package eeg.useit.today.eegtoolkit.sampleapp;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.choosemuse.libmuse.Eeg;
import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseListener;
import com.choosemuse.libmuse.MuseManagerAndroid;

import org.w3c.dom.Text;

import eeg.useit.today.eegtoolkit.common.FrequencyBands;
import eeg.useit.today.eegtoolkit.sampleapp.databinding.ActivityDeviceDetailsBinding;
import eeg.useit.today.eegtoolkit.vm.ConnectionStrengthViewModel;
import eeg.useit.today.eegtoolkit.vm.FrequencyBandViewModel;
import eeg.useit.today.eegtoolkit.vm.SensorGoodViewModel;
import eeg.useit.today.eegtoolkit.vm.StreamingDeviceViewModel;

/**
 * Created by me on 3/24/18.
 */

public class Running extends Activity {


    public static int DURATION_SEC = 5;

    /** The live device VM backing this view. */
    private final StreamingDeviceViewModel deviceVM = new StreamingDeviceViewModel();
    static FrequencyBandViewModel deviceTHETA;
    static FrequencyBandViewModel deviceDELTA;
    static FrequencyBandViewModel deviceALPHA;
    static FrequencyBandViewModel deviceBETA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_running);
        CheckBox start_stop = (CheckBox) findViewById(R.id.start_stop);
        TextView averageData = (TextView) findViewById(R.id.AverageData);
        averageData.setMovementMethod(new ScrollingMovementMethod());
        int counter = 0;
        while(counter < 10){
            if(start_stop.isChecked()){
                //averageData.setText(String.valueOf(DeviceDetailsActivity.getEngagement()));
            }
            counter++;
        }
        // Initialize Muse first up.
        MuseManagerAndroid.getInstance().setContext(this);

        // Bind viewmodel to the view.
        ActivityDeviceDetailsBinding binding =
                DataBindingUtil.setContentView(this, R.layout.activity_device_details);

        binding.setDeviceVM(deviceVM);
        binding.setIsGoodVM(new SensorGoodViewModel(deviceVM));
        binding.setConnectionVM(new ConnectionStrengthViewModel(deviceVM));
        binding.setRawVM(  deviceVM.createRawTimeSeries(Eeg.EEG3, DURATION_SEC));
        binding.setThetaVM(deviceVM.createFrequencyLiveValue(FrequencyBands.Band.THETA, FrequencyBands.ValueType.SCORE));
        binding.setDeltaVM(deviceVM.createFrequencyLiveValue(FrequencyBands.Band.DELTA, FrequencyBands.ValueType.SCORE));
        binding.setAlphaVM(deviceVM.createFrequencyLiveValue(FrequencyBands.Band.ALPHA, FrequencyBands.ValueType.SCORE));
        binding.setBetaVM( deviceVM.createFrequencyLiveValue(FrequencyBands.Band.BETA,  FrequencyBands.ValueType.SCORE));

        // IDK whether to do score or relative
        deviceTHETA = (deviceVM.createFrequencyLiveValue(FrequencyBands.Band.THETA, FrequencyBands.ValueType.RELATIVE));
        deviceDELTA = (deviceVM.createFrequencyLiveValue(FrequencyBands.Band.DELTA, FrequencyBands.ValueType.RELATIVE));
        deviceALPHA = (deviceVM.createFrequencyLiveValue(FrequencyBands.Band.ALPHA, FrequencyBands.ValueType.RELATIVE));
        deviceBETA = (deviceVM.createFrequencyLiveValue(FrequencyBands.Band.BETA, FrequencyBands.ValueType.RELATIVE));

       /* // Bind action bar, seems like this can't be done in the layout :(
        deviceVM.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                Running.this.getSupportActionBar().setTitle(
                        String.format("%s: %s", deviceVM.getName(), deviceVM.getConnectionState())
                );
            }
        }); */

        // And attach the desired muse to the VM once connected.
        final String macAddress = getIntent().getExtras().getString("mac");
        if (macAddress != null) {
            MuseManagerAndroid.getInstance().startListening();
            MuseManagerAndroid.getInstance().setMuseListener(new MuseListener() {
                @Override
                public void museListChanged() {
                    for (Muse muse : MuseManagerAndroid.getInstance().getMuses()) {
                        if (macAddress.equals(muse.getMacAddress())) {
                            Running.this.deviceVM.setMuse(muse);
                            MuseManagerAndroid.getInstance().stopListening();
                            break;
                        }
                    }
                }
            });
        }

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
            return true;
        }
        return false;
    }

    public static double getEngagement() {
        double engagement = 0.0;
        engagement = deviceBETA.getAverage() / (deviceALPHA.getAverage() + deviceTHETA.getAverage());
        Log.d("Values","TEST");
        Log.d("Theta", String.valueOf(deviceTHETA.getAverage()));
        Log.d("Delta", String.valueOf(deviceDELTA.getAverage()));
        Log.d("Alpha", String.valueOf(deviceALPHA.getAverage()));
        Log.d("Beta", String.valueOf(deviceBETA.getAverage()));
        Log.d("Battery",String.valueOf(deviceBETA.getBattery()));
        Log.d("Engagement", String.valueOf(engagement));
        return engagement;
    }


}
