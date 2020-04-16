package br.mssjim.hidroponia.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

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

    private TextView tvEmail;

    private TextView lDados;
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

    private AlertDialog.Builder alertDialog;
    LinearLayout.LayoutParams lp;

    private int i, j, mensagens; // Variável utilizada para a contagem de documentos no método de exclusão de conta

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

        tvEmail = findViewById(R.id.tvEmail);

        tvEmail.setText(user.getEmail());

        // Dados
        lDados = findViewById(R.id.lDados);
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
            tvNameField.setText(getString(R.string.fullName));
            tvCpfField.setText(getString(R.string.cpf));
        } else {
            lDados.setVisibility(View.GONE);
            llDados.setVisibility(View.GONE);
        }

        // TODO Formatar campos
        tvName.setText(dados.getName());
        tvData.setText(dados.getDate());
        tvCpf.setText(dados.getCpf());
        tvPhone.setText(String.valueOf(dados.getPhone()));
        tvAddress.setText(dados.getAddress());
        tvCep.setText(dados.getCep());

        lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
    }

    public void createAlertDialog(String s, View view, DialogInterface.OnClickListener listener) {
        // TODO Atualizar 'user', 'roles' e 'dados' para não exibir valores antigos
        alertDialog = null;
        TextView title = new TextView(this);
        title.setText(s);
        title.setPadding(20, 30, 20, 30);
        title.setTextSize(20F);
        title.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        title.setTextColor(getResources().getColor(R.color.colorWhite));
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        final LinearLayout layout = new LinearLayout(this);
        layout.setLayoutParams(lp);
        layout.setPadding(10, 40, 10, 0);
        layout.addView(view);

        new AlertDialog.Builder(Profile.this)
                .setView(layout)
                .setPositiveButton(android.R.string.yes, listener)
                .setNegativeButton(android.R.string.no, null)
                .setCustomTitle(title)
                .show()
            .getWindow().setBackgroundDrawable(getDrawable(R.drawable.bg_alertdialog));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0) {
            if(data == null) {
                return;
            }
            Uri imageUri = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                ivImage.setImageDrawable(new BitmapDrawable(bitmap));

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
        final EditText input = new EditText(this);
        input.setBackground(getDrawable(R.drawable.bg_edittext));
        input.setText(user.getUsername());
        input.setPadding(10, 10, 10, 10);
        input.setLayoutParams(lp);
        createAlertDialog(getString(R.string.changeUsername), input, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                tvUsername.setText(input.getText().toString());
                Log.i("AppLog", "Alterando dados...");
                FirebaseFirestore.getInstance().collection("users").
                        document(user.getUserId()).update("username", input.getText().toString());
                Log.i("AppLog", "Dados Alterados com Sucesso!");
            }
        });
    }

    public void changePassword(View view) {

    }

    public void changeDados(View view) {
        Intent intent = new Intent(Profile.this, Join.class);
        startActivity(intent);
    }

    public void deleteAccount(View view) {
        // TODO Arrumar sabagaça kkkkkk Separar em funções e apagar dados apenas se existirem
        final TextView message = new TextView(this);
        message.setText(getString(R.string.deleteAccountConfirm));
        message.setTextSize(16F);
        message.setLayoutParams(lp);
        createAlertDialog(getString(R.string.deleteAccount), message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Deletar conta
                Log.i("AppLog", "Excluindo dados da conta...");
                Log.i("AppLog", "1/5");
                FirebaseStorage.getInstance().getReference("/profile-images/" +
                user.getUsername() + "(" + user.getEmail() + ")").delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("AppLog", "2/5");
                        FirebaseFirestore.getInstance().collection("users").
                        document(user.getUserId()).delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i("AppLog", "3/5");
                                FirebaseFirestore.getInstance().collection("data")
                                .document(user.getUserId()).collection("data").get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        final List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                                        i = 0;
                                        for (DocumentSnapshot doc : docs) {
                                            Log.i("AppLog", "Excluindo Data ("+ ++i +"/" + docs.size() +  ")... (" + doc.getReference().getPath() + ")");
                                            doc.getReference().delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    if(docs.size() == i) {
                                                        FirebaseFirestore.getInstance().collection("data")
                                                        .document(user.getUserId()).collection("last-messages").get()
                                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                final List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                                                                i = 0;
                                                                mensagens = Math.max(mensagens, docs.size());
                                                                for (DocumentSnapshot doc : docs) {
                                                                    Log.i("AppLog", "Excluindo Mensagens (" + ++i + "/" + docs.size() + ")... (" + doc.getReference().getPath() + ")");
                                                                    final String path = doc.getReference().getPath().substring(doc.getReference().getPath().lastIndexOf("/"));
                                                                    doc.getReference().delete()
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            FirebaseFirestore.getInstance().collection("data")
                                                                            .document(user.getUserId()).collection("messages")
                                                                            .document(path).collection("all").get()
                                                                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                                @Override
                                                                                public void onSuccess(final QuerySnapshot queryDocumentSnapshots) {
                                                                                    final List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                                                                                    j = 0;
                                                                                    for (final DocumentSnapshot doc : docs) {
                                                                                        Log.i("AppLog", "Excluindo Mensagens (" + i + "/" + mensagens + ") (" + ++j + "/" + docs.size() + ") ... (" + doc.getReference().getPath() + ")");
                                                                                        doc.getReference().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                            @Override
                                                                                            public void onSuccess(Void aVoid) {
                                                                                                FirebaseFirestore.getInstance().collection("data")
                                                                                                .document(user.getUserId()).get()
                                                                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                                                    @Override
                                                                                                    public void onSuccess(final DocumentSnapshot documentSnapshot) {
                                                                                                        new Handler().postDelayed(new Runnable() {
                                                                                                            @Override
                                                                                                            public void run() {
                                                                                                                if(!documentSnapshot.exists()) {
                                                                                                                    ok();
                                                                                                                }
                                                                                                            }
                                                                                                        }, 5000);
                                                                                                    }
                                                                                                });
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                }
                                                                            });
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    public void ok() {
        Log.i("AppLog", "4/5");
        FirebaseAuth.getInstance().getCurrentUser().delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        FirebaseAuth.getInstance().signOut();
                        Log.i("AppLog", "5/5");
                        Log.i("AppLog", "Conta excluída com sucesso!");
                        Intent intent = new Intent(Profile.this, Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        System.exit(0); // TODO Arrumar isso pq ainda não entendi pq não bugou 2
                    }
                });
    }
}
