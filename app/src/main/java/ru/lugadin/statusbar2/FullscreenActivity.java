package ru.lugadin.statusbar2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class FullscreenActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent svc = new Intent(this, OverlayShowingService.class);
        startService(svc);

        if(1==1){
            finish();
        }

        finish();
    }

}