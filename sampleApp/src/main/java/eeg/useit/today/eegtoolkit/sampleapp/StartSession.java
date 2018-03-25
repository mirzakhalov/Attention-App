package eeg.useit.today.eegtoolkit.sampleapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by me on 3/24/18.
 */

public class StartSession extends AppCompatActivity {
    final Intent intent = new Intent(StartSession.this,Running.class );
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        Button startRunning = findViewById(R.id.button);

        startRunning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(intent);
            }
        });

    }

}
