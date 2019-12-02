package org.haobtc.wallet.activities;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.CommonUtils;

public class PublicKeyInputEditActivity extends BaseActivity implements View.OnClickListener {
    private EditText editTextPublicKey;


    public int getLayoutId() {
        return R.layout.input_address_edit;
    }

    public void initView() {
        Button buttonSweep, buttonPaste, buttonConfirm;
        CommonUtils.enableToolBar(this, R.string.import_mutiSig);
        buttonSweep = findViewById(R.id.bn_sweep_create);
        buttonPaste = findViewById(R.id.bn_paste_create);
        buttonConfirm = findViewById(R.id.bn_confirm_create);
        editTextPublicKey = findViewById(R.id.edit_public_key);
        editTextPublicKey.addTextChangedListener(new TextWatcher(){

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO：
                Toast.makeText(PublicKeyInputEditActivity.this,"按钮状态恢复", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void afterTextChanged(Editable s) {
                editTextPublicKey.getText().toString();
            }
        });
        buttonSweep.setOnClickListener(this);
        buttonPaste.setOnClickListener(this);
        buttonConfirm.setOnClickListener(this);
    }

    @Override
    public void initData() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bn_sweep_create:
                editTextPublicKey.setText("sweep");
                break;
            case R.id.bn_paste_create:
                editTextPublicKey.setText("paste");
                break;
            case R.id.bn_confirm_create:
                if (editTextPublicKey.getText().toString().equals("1234")) {
                    Intent intent = new Intent(this, SelectMultiSigWalletActivity.class);
                    startActivity(intent);
                } else {
                    // todo:
                    Toast.makeText(PublicKeyInputEditActivity.this,"输入有误,按钮状态改变", Toast.LENGTH_SHORT).show();
                }
                break;
            default:

        }

    }
}
