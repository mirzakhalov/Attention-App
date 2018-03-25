package eeg.useit.today.eegtoolkit.sampleapp;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import static java.lang.Double.NaN;

public class MainActivity extends AppCompatActivity {
    static int[] aSliderVal = {0};
    static int[] dSliderVal = {0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SeekBar aSlider = (SeekBar)findViewById(R.id.seekBar2);
        aSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                aSliderVal[0] = progress;
                Log.d("Taga",String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        SeekBar dSlider = (SeekBar)findViewById(R.id.seekBar4);
        dSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                dSliderVal[0] = progress;
                Log.d("Tagd",String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void goToRunning (View view) {
        Intent i = new Intent(MainActivity.this, RunningActivity.class);
        startActivity(i);
    }
}