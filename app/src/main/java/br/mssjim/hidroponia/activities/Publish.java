package br.mssjim.hidroponia.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

import br.mssjim.hidroponia.Hidroponia;
import br.mssjim.hidroponia.Post;
import br.mssjim.hidroponia.R;
import br.mssjim.hidroponia.User;

public class Publish extends Activity {
    private ImageView ivPublish;
    private EditText etPublish;

    private String publishId;
    private Uri imageUri;

    @Override
    protected void onCreate(@Nullable @android.support.annotation.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_publish);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        ivPublish = findViewById(R.id.ivPublish);
        etPublish = findViewById(R.id.etPublish);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_publish, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.send) {
            enviar();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (data == null) {
                return;
            }
            imageUri = data.getData();
            Bitmap bitmap;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                ivPublish.setImageDrawable(new BitmapDrawable(bitmap));
            } catch (IOException e) {
                // TODO Catch Block
            }
        }
    }

    public void image(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 0);
    }

    private void enviar() {
        final User user = Hidroponia.getUser();
        final long time = System.currentTimeMillis();
        final String text = etPublish.getText().toString().trim();

        if(text.isEmpty()) {
            Toast.makeText(getApplicationContext(), getString(R.string.emptyField), Toast.LENGTH_SHORT).show();
            return;
        } else {
            etPublish.setText(null);
        }

        publishId = time + user.getUserId();

        if(imageUri != null) {
            Log.i("AppLog", "Fazendo upload de imagem...");
            // TODO Animação durante upload;
            final StorageReference ref = FirebaseStorage.getInstance().getReference("/publish-images/" + publishId);
            ref.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Log.i("AppLog", "Upload de imagem concluído! Url pública: " + uri.toString());
                                    FirebaseFirestore.getInstance().collection("publish")
                                            .document(publishId).set(new Post(user.getUserId(), text, uri.toString(), time))
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(getApplicationContext(), getString(R.string.publishSuccess), Toast.LENGTH_SHORT).show();
                                                    Log.i("AppLog", getString(R.string.publishSuccess));

                                                    Intent intent = new Intent(Publish.this, Inicio.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);
                                                }
                                            });
                                }
                            });
                        }
                    });
        } else {
            FirebaseFirestore.getInstance().collection("publish")
                    .document(publishId).set(new Post(user.getUserId(), text, null, time))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext(), getString(R.string.publishSuccess), Toast.LENGTH_SHORT).show();
                            Log.i("AppLog", getString(R.string.publishSuccess));

                            Intent intent = new Intent(Publish.this, Inicio.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    });
        }
    }
}
