package br.mssjim.hidroponia.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import br.mssjim.hidroponia.Dados;
import br.mssjim.hidroponia.Hidroponia;
import br.mssjim.hidroponia.R;
import br.mssjim.hidroponia.Roles;

public class Join extends Activity {
    // TODO Perfil para pessoas que só buscam comprar alimentos (talvez)

    private boolean home;
    private boolean business;
    private boolean farm;
    private boolean sale;
    private boolean organic;
    private boolean store;

    private Button btJoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_join);

        btJoin = findViewById(R.id.btJoin);

        getActionBar().hide();
    }

    public void cbTermos(View v) {
        CheckBox cb = findViewById(v.getId());
        if(cb.isChecked()) {
            btJoin.setClickable(true);
            btJoin.setBackground(getDrawable(R.drawable.bg_button));
        } else {
            btJoin.setClickable(false);
            btJoin.setBackground(getDrawable(R.drawable.bg_buttondisabled));
        }
    }

    public void termos(View view) {
        Intent intent = new Intent(Join.this, Termos.class);
    }

    public void join(View view) {
        setContentView(R.layout.act_join2);
    }

    public void home(View view) {
        home = true;
        business = false;

        ImageView ivHome = findViewById(R.id.ivHome);
        ImageView ivBusiness = findViewById(R.id.ivBusiness);
        TextView tvJoin2 = findViewById(R.id.tvJoin2);
        Button btJoin2 = findViewById(R.id.btJoin2);

        ivHome.setImageResource(R.drawable.home);
        ivBusiness.setImageResource(R.drawable.business_disabled);
        tvJoin2.setText(getString(R.string.home));
        btJoin2.setVisibility(View.VISIBLE);
    }
    public void business(View view) {
        home = false;
        business = true;

        ImageView ivHome = findViewById(R.id.ivHome);
        ImageView ivBusiness = findViewById(R.id.ivBusiness);
        TextView tvJoin2 = findViewById(R.id.tvJoin2);
        Button btJoin2 = findViewById(R.id.btJoin2);

        ivHome.setImageResource(R.drawable.home_disabled);
        ivBusiness.setImageResource(R.drawable.business);
        tvJoin2.setText(getString(R.string.business));
        btJoin2.setVisibility(View.VISIBLE);
    }
    public void join2(View view) {
        if(home) {
            setContentView(R.layout.act_join3);
        } else {
            setContentView(R.layout.act_join4);
        }
    }

    public void farm(View view) {
        farm = true;
        sale = false;

        ImageView ivFarm = findViewById(R.id.ivFarm);
        ImageView ivSale = findViewById(R.id.ivSale);
        TextView tvJoin3 = findViewById(R.id.tvJoin3);
        Button btJoin3 = findViewById(R.id.btJoin3);

        ivFarm.setImageResource(R.drawable.farm);
        ivSale.setImageResource(R.drawable.sale_disabled);
        tvJoin3.setText(getString(R.string.farm));
        btJoin3.setVisibility(View.VISIBLE);
    }
    public void sale(View view) {
        farm = false;
        sale = true;

        ImageView ivFarm = findViewById(R.id.ivFarm);
        ImageView ivSale = findViewById(R.id.ivSale);
        TextView tvJoin3 = findViewById(R.id.tvJoin3);
        Button btJoin3 = findViewById(R.id.btJoin3);

        ivFarm.setImageResource(R.drawable.farm_disabled);
        ivSale.setImageResource(R.drawable.sale);
        tvJoin3.setText(getString(R.string.sale));
        btJoin3.setVisibility(View.VISIBLE);
    }
    public void join3(View view) {
        setContentView(R.layout.act_join5);
    }

    public void organic(View view) {
        organic = true;
        store = false;

        ImageView ivOrganic = findViewById(R.id.ivOrganic);
        ImageView ivStore = findViewById(R.id.ivStore);
        TextView tvJoin4 = findViewById(R.id.tvJoin4);
        Button btJoin4 = findViewById(R.id.btJoin4);

        ivOrganic.setImageResource(R.drawable.organic);
        ivStore.setImageResource(R.drawable.store_disabled);
        tvJoin4.setText(getString(R.string.organic));
        btJoin4.setVisibility(View.VISIBLE);
    }
    public void store(View view) {
        organic = false;
        store = true;

        ImageView ivOrganic = findViewById(R.id.ivOrganic);
        ImageView ivStore = findViewById(R.id.ivStore);
        TextView tvJoin4 = findViewById(R.id.tvJoin4);
        Button btJoin4 = findViewById(R.id.btJoin4);

        ivOrganic.setImageResource(R.drawable.organic_disabled);
        ivStore.setImageResource(R.drawable.store);
        tvJoin4.setText(getString(R.string.store));
        btJoin4.setVisibility(View.VISIBLE);
    }
    public void join4(View view) {
        setContentView(R.layout.act_join6);
    }

    public void finishPF(View view) {
        // TODO Verificar dados antes de adicioná-los

        EditText etName = findViewById(R.id.etName);
        EditText etNasc = findViewById(R.id.etNasc);
        EditText etCpf = findViewById(R.id.etCpf);
        EditText etPhone = findViewById(R.id.etPhone);
        EditText etAddress = findViewById(R.id.etAddress);
        EditText etAddressNumber = findViewById(R.id.etAddressNumber);
        EditText etCep = findViewById(R.id.etCep);

        // TODO Evitar erros de Parse
        Dados dados = new Dados(
                Hidroponia.getUser().getUserId(),
                etName.getText().toString(),
                etNasc.getText().toString(),
                Integer.parseInt(etCpf.getText().toString()),
                Integer.parseInt(etPhone.getText().toString()),
                etAddress.getText().toString(),
                Integer.parseInt(etAddressNumber.getText().toString()),
                Integer.parseInt(etCep.getText().toString())
        );

        Log.i("AppLog", "Adicionando dados ao Firestore...");
        FirebaseFirestore.getInstance().collection("/data")
                .document(Hidroponia.getUser().getUserId()).collection("data")
                .document("dados").set(dados)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("AppLog", "Dados atualizados com sucesso!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("AppLog", "Erro: " + e.getLocalizedMessage());
                        // TODO Catch Block
                    }
                });

        updateRoles();
        this.finish();
    }

    public void finishPJ(View view) {
        // TODO Verificar dados antes de adicioná-los

        EditText etRazao = findViewById(R.id.etRazao);
        EditText etCnpj = findViewById(R.id.etCnpj);
        EditText etPhone = findViewById(R.id.etPhone);
        EditText etAddress = findViewById(R.id.etAddress);
        EditText etAddressNumber = findViewById(R.id.etAddressNumber);
        EditText etCep = findViewById(R.id.etCep);

        // TODO Evitar erros de Parse
        Dados dados = new Dados(
                Hidroponia.getUser().getUserId(),
                etRazao.getText().toString(),
                Integer.parseInt(etCnpj.getText().toString()),
                Integer.parseInt(etPhone.getText().toString()),
                etAddress.getText().toString(),
                Integer.parseInt(etAddressNumber.getText().toString()),
                Integer.parseInt(etCep.getText().toString())
        );

        Log.i("AppLog", "Adicionando dados ao Firestore...");
        FirebaseFirestore.getInstance().collection("/data")
                .document(Hidroponia.getUser().getUserId()).collection("data")
                .document("dados").set(dados)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("AppLog", "Dados atualizados com sucesso!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("AppLog", "Erro: " + e.getLocalizedMessage());
                        // TODO Catch Block
                    }
                });

        updateRoles();
        this.finish();
    }

    public void updateRoles() {
        Log.i("AppLog", "Adicionando dados ao Firestore...");
        FirebaseFirestore.getInstance().collection("/data")
                .document(Hidroponia.getUser().getUserId()).collection("data")
                .document("roles").set(new Roles(farm, sale, organic, store))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("AppLog", "Dados atualizados com sucesso!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("AppLog", "Erro: " + e.getLocalizedMessage());
                        // TODO Catch Block
                    }
                });
    }
}
