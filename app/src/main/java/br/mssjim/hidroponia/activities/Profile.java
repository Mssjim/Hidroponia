package br.mssjim.hidroponia.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import br.mssjim.hidroponia.Dados;
import br.mssjim.hidroponia.Hidroponia;
import br.mssjim.hidroponia.R;
import br.mssjim.hidroponia.Roles;
import br.mssjim.hidroponia.User;


public class Profile extends Activity {
    private User user;
    private Roles roles;
    private Dados dados;

    private ImageView ivImage;
    private TextView tvUsername;
    private TextView tvRole;

    private LinearLayout llDados;
    private TextView tvNameField;
    private TextView tvName;
    private TextView tvDataField;
    private TextView tvData;
    private TextView tvCpfField;
    private TextView tvCpf;
    private TextView tvPhone;
    private TextView tvAddress;
    private TextView tvCep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_profile);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        user = Hidroponia.getUser();
        roles = Hidroponia.getRoles();
        dados = Hidroponia.getDados();

        ivImage = findViewById(R.id.ivImage);
        tvUsername = findViewById(R.id.tvUsername);
        tvRole = findViewById(R.id.tvRole);

        tvUsername.setText(user.getUsername());
        tvRole.setText(roles.getRole());
        Picasso.get().load(user.getProfileImage()).placeholder(R.drawable.default_profile).into(ivImage);

        // Dados
        llDados = findViewById(R.id.llDados);
        tvNameField = findViewById(R.id.tvNameField);
        tvName = findViewById(R.id.tvName);
        tvDataField = findViewById(R.id.tvDataField);
        tvData = findViewById(R.id.tvData);
        tvCpfField = findViewById(R.id.tvCpfField);
        tvCpf = findViewById(R.id.tvCpf);
        tvPhone = findViewById(R.id.tvPhone);
        tvAddress = findViewById(R.id.tvAddress);
        tvCep = findViewById(R.id.tvCep);

        if(roles.isOrganic() || roles.isStore()) {
            tvDataField.setVisibility(View.GONE);
            tvData.setVisibility(View.GONE);
            tvNameField.setText(getString(R.string.razao));
            tvCpfField.setText(getString(R.string.cnpj));
        } else if (roles.isFarm() || roles.isSale()) {

        } else {
            llDados.setVisibility(View.GONE);
        }

        tvName.setText(dados.getName());
        tvData.setText(dados.getDate());
        tvCpf.setText(dados.getCpf());
        tvPhone.setText(dados.getPhone());
        tvAddress.setText(dados.getAddress());
        tvCep.setText(dados.getCep());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0) {
            Uri imageUri = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                ivImage.setImageDrawable(new BitmapDrawable(bitmap));

                if(imageUri == null) { // TODO Testar se um 'return' tem o mesmo efeito
                    Log.i("AppLog", "Fazendo upload de imagem...");
                    // TODO Animação durante upload
                    String filename = user.getUsername() + "(" + user.getEmail() + ")";
                    final StorageReference ref = FirebaseStorage.getInstance().getReference("/profile-images/" + filename);
                    ref.putFile(imageUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Log.i("AppLog", "Upload de imagem concluído! Url pública: " + uri.toString());
                                            FirebaseFirestore.getInstance().collection("users")
                                                    .document(user.getUserId())
                                                    .update("profileImage", uri.toString());
                                        }
                                    });
                                }
                            });
                }
            } catch (IOException e) {
                // TODO Catch Block
            }
        }
    }

    public void changeImage(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 0);
    }

    public void changeUsername(View view) {
        final EditText input = new EditText(Profile.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.VERTICAL);
        input.setLayoutParams(lp);

        new AlertDialog.Builder(getApplicationContext())
                .setTitle("Alterar nome de usuário")
                .setView(input)
                .setIcon(R.drawable.ic_edit)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String s = input.getText().toString();
                        if(TextUtils.isEmpty(s)) {
                            input.setError(getString(R.string.emptyField));
                        } else {
                            tvUsername.setText(s);
                            FirebaseFirestore.getInstance().collection("users").
                                    document(user.getUserId()).update("username", s);
                        }
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }
}
