package br.mssjim.hidroponia.activities;

import android.app.Activity;
import android.os.Bundle;

import br.mssjim.hidroponia.R;

public class Termos extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_termos);

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
