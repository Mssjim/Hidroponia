package br.mssjim.hidroponia.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import br.mssjim.hidroponia.R;

public class Update extends Activity {

    private final String currentVersion = "0.4.14";
    private String lastVersion;
    private String changes;
    private Uri url;

    private LinearLayout ll;
    private ProgressBar pb;
    private TextView tvResult;
    private TextView tvCurrentVersion;
    private TextView tvLastVersion;
    private TextView tvChanges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_update);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        ll = findViewById(R.id.ll);
        pb = findViewById(R.id.pb);
        tvResult = findViewById(R.id.tvResult);
        tvCurrentVersion = findViewById(R.id.tvCurrentVersion);
        tvLastVersion = findViewById(R.id.tvLastVersion);
        tvChanges = findViewById(R.id.tvChanges);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUpdates();
    }

    private void checkUpdates() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() { // Handler pra não travar tudo
                Log.i("AppLog", "Buscando atualizações...");
                final FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
                FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings
                        .Builder().setMinimumFetchIntervalInSeconds(1).build();
                remoteConfig.setConfigSettingsAsync(configSettings);
                remoteConfig.fetchAndActivate()
                        .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                            @Override
                            public void onSuccess(Boolean aBoolean) {
                                lastVersion = remoteConfig .getString("version");
                                changes = remoteConfig .getString("changes");
                                url = Uri.parse(remoteConfig .getString("url"));

                                if(!currentVersion.equals(lastVersion)) {
                                    Log.i("AppLog", "Nova versão encontrada! " + lastVersion);
                                    ll.setVisibility(View.GONE);
                                    tvCurrentVersion.setText(currentVersion);
                                    tvLastVersion.setText(lastVersion);
                                    tvChanges.setText(changes);
                                } else {
                                    // TODO Alterar visibilidade do layout
                                    Log.i("AppLog", "Não há novas atualizações!");
                                    pb.setVisibility(View.GONE);
                                    tvResult.setText(R.string.updated);
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("AppLog", "Erro: " + e.getLocalizedMessage());
                        Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        // TODO Catch Block
                    }
                });
            }
        }, 0);
    }

    public void download(View view) {
        Log.i("AppLog", "Iniciando download...");
        view.setEnabled(false);
        view.setBackground(getDrawable(R.drawable.bg_buttondisabled));
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(url);
        startActivity(intent);

        // TODO Retornar confirmação de download
    }
}
