package org.pixel.demo.android;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import org.pixel.android.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.cornflower_blue));
    }
}

