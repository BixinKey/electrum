package org.haobtc.wallet.activities;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.chaquo.python.PyObject;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yzq.zxinglibrary.encode.CodeCreator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.service.CommunicationModeSelector;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.bean.GetCodeAddressBean;
import org.haobtc.wallet.event.CheckReceiveAddress;
import org.haobtc.wallet.utils.Daemon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReceivedPageActivity extends BaseActivity {

    public static final String TAG = ReceivedPageActivity.class.getSimpleName();
    @BindView(R.id.imageView2)
    ImageView imageView2;
    @BindView(R.id.textView5)
    TextView textView5;
    @BindView(R.id.textView6)
    TextView textView6;
    @BindView(R.id.button)
    Button button;
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.text_receive_tip)
    TextView textReceiveTip;
    @BindView(R.id.copy_and_check)
    LinearLayout copyAndCheck;
    @BindView(R.id.text_change_address)
    TextView textChangeAddress;
    private Bitmap bitmap;
    private RxPermissions rxPermissions;
    private String hideWalletReceive;
    private boolean checking = true;

    @Override
    public int getLayoutId() {
        return R.layout.address_info;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        rxPermissions = new RxPermissions(this);
        String walletType = getIntent().getStringExtra("walletType");
        hideWalletReceive = getIntent().getStringExtra("hideWalletReceive");

//        if ("standard".equals(walletType)) {
//            textChangeAddress.setVisibility(View.VISIBLE);
//            copyAndCheck.setVisibility(View.GONE);
//        } else {
//            textChangeAddress.setVisibility(View.GONE);
//            copyAndCheck.setVisibility(View.VISIBLE);
//        }
    }

    @Override
    public void initData() {
        //Generate QR code
        mGeneratecode();

    }

    private void mGeneratecode() {
        PyObject walletAddressShowUi = null;
        try {
            walletAddressShowUi = Daemon.commands.callAttr("get_wallet_address_show_UI");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (walletAddressShowUi != null) {
            String strCode = walletAddressShowUi.toString();
            Gson gson = new Gson();
            GetCodeAddressBean getCodeAddressBean = gson.fromJson(strCode, GetCodeAddressBean.class);
            String qrData = getCodeAddressBean.getQrData();
            String addr = getCodeAddressBean.getAddr();
            textView5.setText(addr);
            bitmap = CodeCreator.createQRCode(qrData, 250, 250, null);
            imageView2.setImageBitmap(bitmap);
        }

    }

    @SingleClick
    @OnClick({R.id.textView6, R.id.button, R.id.img_back, R.id.text_check_address, R.id.text_change_address, R.id.textView5})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.textView6:
                //change address

                break;
            case R.id.button:
                rxPermissions
                        .request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(granted -> {
                            if (granted) { // Always true pre-M
                                String uri = saveImageToGallery(this, bitmap);
                                if (uri != null) {
                                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(uri));
                                    shareIntent.setType("image/*");
                                    shareIntent.putExtra(Intent.EXTRA_TEXT, textView5.getText().toString());
                                    shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    shareIntent = Intent.createChooser(shareIntent, "Here is the title of Select box");
                                    startActivity(shareIntent);

                                } else {
                                    mToast(getString(R.string.pictrue_fail));
                                }

                            } else { // Oups permission denied
                                Toast.makeText(this, R.string.reservatpion_photo, Toast.LENGTH_SHORT).show();
                            }
                        }).dispose();

                break;
            case R.id.img_back:
                finish();
                break;
            case R.id.text_check_address:
//                if (checking) {
//                    CommunicationModeSelector.runnables.clear();
//                    CommunicationModeSelector.runnables.add(null);
//                    Intent intent = new Intent(this, CommunicationModeSelector.class);
//                    intent.putExtra("tag", TAG);
//                    intent.putExtra("contrastAddress", textView5.getText().toString());
//                    intent.putExtra("hideWalletReceive", hideWalletReceive);
//                    startActivity(intent);
//                } else {
//                    mToast(getString(R.string.checking));
//                }

                break;
            case R.id.text_change_address:
                //change address
                if (checking) {
                    CommunicationModeSelector.runnables.clear();
                    CommunicationModeSelector.runnables.add(null);
                    Intent intent = new Intent(this, CommunicationModeSelector.class);
                    intent.putExtra("tag", TAG);
                    intent.putExtra("contrastAddress", textView5.getText().toString());
                    intent.putExtra("hideWalletReceive", hideWalletReceive);
                    startActivity(intent);
                } else {
                    mToast(getString(R.string.checking));
                }
                break;
            case R.id.textView5:
                //copy text
                ClipboardManager cm2 = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // The text is placed on the system clipboard.
                Objects.requireNonNull(cm2, "ClipboardManager not available").setPrimaryClip(ClipData.newPlainText(null, textView5.getText()));
                Toast.makeText(ReceivedPageActivity.this, R.string.copysuccess, Toast.LENGTH_LONG).show();
                break;
            default:
        }
    }


    private String saveImageToGallery(Context context, @NonNull Bitmap bmp) {
        // Save picture
        String storePath = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator;
        File appDir = new File(storePath);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".png";
        File file = new File(appDir, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            //Compress and save pictures by IO stream
            boolean isSuccess;
            isSuccess = bmp.compress(Bitmap.CompressFormat.PNG, 60, fos);
            fos.flush();
            //Insert file into system library
            if (!isSuccess) {
                mToast(getString(R.string.fail));
                return null;
            }
            String uri = MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);
            //Send broadcast notice to update database after saving picture
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(uri)));
            return uri;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showReading(CheckReceiveAddress event) {
        if ("getResult".equals(event.getType())) {
            checking = true;
            textReceiveTip.setVisibility(View.GONE);
        } else if ("checking".equals(event.getType())) {
            checking = false;
            textReceiveTip.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}

