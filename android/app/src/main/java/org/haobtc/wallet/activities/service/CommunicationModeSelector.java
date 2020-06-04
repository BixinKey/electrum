package org.haobtc.wallet.activities.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.chaquo.python.PyObject;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.PinSettingActivity;
import org.haobtc.wallet.activities.SendOne2ManyMainPageActivity;
import org.haobtc.wallet.activities.SendOne2OneMainPageActivity;
import org.haobtc.wallet.activities.SettingActivity;
import org.haobtc.wallet.activities.TransactionDetailsActivity;
import org.haobtc.wallet.activities.VerificationKEYActivity;
import org.haobtc.wallet.activities.WalletUnActivatedActivity;
import org.haobtc.wallet.activities.jointwallet.MultiSigWalletCreator;
import org.haobtc.wallet.activities.personalwallet.ChooseHistryWalletActivity;
import org.haobtc.wallet.activities.personalwallet.ImportHistoryWalletActivity;
import org.haobtc.wallet.activities.personalwallet.PersonalMultiSigWalletCreator;
import org.haobtc.wallet.activities.personalwallet.SingleSigWalletCreator;
import org.haobtc.wallet.activities.personalwallet.hidewallet.HideWalletSetPassActivity;
import org.haobtc.wallet.activities.settings.BixinKeyBluetoothSettingActivity;
import org.haobtc.wallet.activities.settings.CheckXpubResultActivity;
import org.haobtc.wallet.activities.settings.ConfidentialPaymentSettings;
import org.haobtc.wallet.activities.settings.FixBixinkeyNameActivity;
import org.haobtc.wallet.activities.settings.HardwareDetailsActivity;
import org.haobtc.wallet.activities.settings.SetShutdownTimeActivity;
import org.haobtc.wallet.activities.settings.UpgradeBixinKEYActivity;
import org.haobtc.wallet.activities.settings.VersionUpgradeActivity;
import org.haobtc.wallet.activities.settings.recovery_set.BackupMessageActivity;
import org.haobtc.wallet.activities.settings.recovery_set.BackupRecoveryActivity;
import org.haobtc.wallet.activities.settings.recovery_set.RecoveryActivity;
import org.haobtc.wallet.activities.settings.recovery_set.RecoveryResult;
import org.haobtc.wallet.activities.settings.recovery_set.RecoverySetActivity;
import org.haobtc.wallet.activities.sign.SignActivity;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.asynctask.BusinessAsyncTask;
import org.haobtc.wallet.bean.GetnewcreatTrsactionListBean;
import org.haobtc.wallet.bean.HardwareFeatures;
import org.haobtc.wallet.event.ButtonRequestEvent;
import org.haobtc.wallet.event.ChangePinEvent;
import org.haobtc.wallet.event.CheckHideWalletEvent;
import org.haobtc.wallet.event.ConnectingEvent;
import org.haobtc.wallet.event.ExecuteEvent;
import org.haobtc.wallet.event.ExistEvent;
import org.haobtc.wallet.event.FastPayEvent;
import org.haobtc.wallet.event.FinishEvent;
import org.haobtc.wallet.event.FirstEvent;
import org.haobtc.wallet.event.FixAllLabelnameEvent;
import org.haobtc.wallet.event.HandlerEvent;
import org.haobtc.wallet.event.InitEvent;
import org.haobtc.wallet.event.MutiSigWalletEvent;
import org.haobtc.wallet.event.PersonalMutiSigEvent;
import org.haobtc.wallet.event.PinEvent;
import org.haobtc.wallet.event.ReadingEvent;
import org.haobtc.wallet.event.ReceiveXpub;
import org.haobtc.wallet.event.RefreshEvent;
import org.haobtc.wallet.event.ResultEvent;
import org.haobtc.wallet.event.SecondEvent;
import org.haobtc.wallet.event.SendSignBroadcastEvent;
import org.haobtc.wallet.event.SendXpubToSigwallet;
import org.haobtc.wallet.event.SetBluetoothEvent;
import org.haobtc.wallet.event.ShutdownTimeEvent;
import org.haobtc.wallet.event.SignMessageEvent;
import org.haobtc.wallet.event.SignResultEvent;
import org.haobtc.wallet.event.WipeEvent;
import org.haobtc.wallet.exception.BixinExceptions;
import org.haobtc.wallet.fragment.BleDeviceRecyclerViewAdapter;
import org.haobtc.wallet.fragment.BluetoothConnectingFragment;
import org.haobtc.wallet.fragment.BluetoothFragment;
import org.haobtc.wallet.fragment.ErrorDialogFragment;
import org.haobtc.wallet.fragment.ReadingOrSendingDialogFragment;
import org.haobtc.wallet.fragment.mainwheel.CheckHideWalletFragment;
import org.haobtc.wallet.utils.CustomerUsbManager;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.Global;
import org.haobtc.wallet.utils.NfcUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import cn.com.heaton.blelibrary.ble.Ble;
import cn.com.heaton.blelibrary.ble.callback.BleScanCallback;
import cn.com.heaton.blelibrary.ble.model.BleDevice;

import static org.haobtc.wallet.activities.sign.SignActivity.strinputAddress;


public class CommunicationModeSelector extends AppCompatActivity implements View.OnClickListener, BusinessAsyncTask.Helper {

    public static final String TAG = "BLE";
    public static final int BUTTON_REQUEST = 9;
    public static final int PIN_CURRENT = 1;
    public static final int PIN_NEW_FIRST = 2;
    public static final int PASS_NEW_PASSPHRASS = 6;
    public static final int PASS_PASSPHRASS = 3;
    private static final int REQUEST_ENABLE_BT = 1;
    public static PyObject ble, customerUI, nfc, usb, bleHandler, nfcHandler, bleTransport, nfcTransport, usbTransport, protocol;
    public static MyHandler handler;
    public static FutureTask<PyObject> futureTask;
    public static ExecutorService executorService = Executors.newSingleThreadExecutor();
    public static String xpub;
    private boolean isActive = false;
    private boolean isSign = false;
    public static volatile boolean isNFC;
    private BleDeviceRecyclerViewAdapter adapter;
    private RelativeLayout relativeLayout;
    private String tag;
    private String extras;
    public static List<Runnable> runnables = new ArrayList<>();
    private BluetoothConnectingFragment fragment;
    private Ble<BleDevice> mBle;
    private ReadingOrSendingDialogFragment dialogFragment;
    private BluetoothFragment bleFragment;
    public static volatile boolean isDfu;
    private RxPermissions permissions;
    public static final String COMMUNICATION_MODE_BLE = "bluetooth";
    public static final String COMMUNICATION_MODE_NFC = "nfc";
    public static volatile String way;
    private CustomerUsbManager usbManager;
    private UsbDevice device;
    private HardwareFeatures features;
    private boolean isChangePin;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_nfc);
        ImageView imageViewCancel;
        EventBus.getDefault().post(new ExistEvent());
        SharedPreferences preferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
//        boolean bluetoothStatus = preferences.getBoolean("bluetoothStatus", false);
        way = preferences.getString("way", COMMUNICATION_MODE_NFC);
        isNFC = COMMUNICATION_MODE_NFC.equals(way);
        ImageView imageView = findViewById(R.id.touch_nfc);
        TextView textView = findViewById(R.id.text_prompt);
        FrameLayout frameLayout = findViewById(R.id.ble_device);
        RadioGroup radioGroup = findViewById(R.id.radio_group);
        RadioButton radioBle = findViewById(R.id.radio_ble);
        RadioButton radioNfc = findViewById(R.id.radio_nfc);
        imageViewCancel = findViewById(R.id.img_cancel);
        TextView textViewInputByHand = findViewById(R.id.text_input_publickey_by_hand);
        relativeLayout = findViewById(R.id.input_layout);
        textViewInputByHand.setOnClickListener(this);
        imageViewCancel.setOnClickListener(this);
        tag = getIntent().getStringExtra("tag");
//        if (!bluetoothStatus || SetNameActivity.TAG.equals(tag)) {
//            radioBle.setVisibility(View.GONE);
//        }
        nfc = Global.py.getModule("trezorlib.transport.nfc");
        ble = Global.py.getModule("trezorlib.transport.bluetooth");
        usb = Global.py.getModule("trezorlib.transport.android_usb");
        protocol = Global.py.getModule("trezorlib.transport.protocol");
        bleHandler = ble.get("BlueToothHandler");
        nfcHandler = nfc.get("NFCHandle");
        usbTransport = usb.get("AndroidUsbTransport");
        nfcTransport = nfc.get("NFCTransport");
        bleTransport = ble.get("BlueToothTransport");
        customerUI = Global.py.getModule("trezorlib.customer_ui").get("CustomerUI");
        handler = MyHandler.getInstance(this);
        customerUI.put("handler", handler);
        EventBus.getDefault().register(this);
        extras = getIntent().getStringExtra("extras");
        if (!MultiSigWalletCreator.TAG.equals(tag)) {
            relativeLayout.setVisibility(View.GONE);
        }
        adapter = new BleDeviceRecyclerViewAdapter(this);
        bleFragment = new BluetoothFragment(adapter);
        if (COMMUNICATION_MODE_NFC.equals(way)) {
            NfcUtils.nfc(this, true);
            radioBle.setVisibility(View.GONE);
            usbTransport.put("ENABLED", false);
            bleTransport.put("ENABLED", false);
            nfcTransport.put("ENABLED", true);
        } else if ("ble".equals(way)) {
            usbTransport.put("ENABLED", false);
            bleTransport.put("ENABLED", true);
            nfcTransport.put("ENABLED", false);
            mBle = Ble.getInstance();
            radioNfc.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
            getSupportFragmentManager().beginTransaction().replace(R.id.ble_device, bleFragment).commit();
            permissions = new RxPermissions(this);
            radioGroup.check(R.id.radio_ble);
            permissions.request(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION).subscribe(
                    granted -> {
                        if (granted) {
                            turnOnBlueTooth();
                            refreshDeviceList(true);
                        } else {
                            Toast.makeText(this, getString(R.string.blurtooth_need_permission), Toast.LENGTH_LONG).show();
                        }
                    }
            ).dispose();
        } else {
            // usb init
            usbTransport.put("ENABLED", true);
            bleTransport.put("ENABLED", false);
            nfcTransport.put("ENABLED", false);
            radioGroup.setVisibility(View.GONE);
            setVisible(false);
            usbManager = CustomerUsbManager.getInstance(this);
            usbManager.register(this);
            device = usbManager.findBixinKEYDevice();
            if (device != null) {
                try {
                    usbManager.doBusiness(device);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "未找到可用的USB设备", Toast.LENGTH_LONG).show();
                new Handler().postDelayed(this::finish, 2000);
            }
        }
         /*else {
            mBle = Ble.getInstance();
            NfcUtils.nfc(this, true);
            radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                switch (checkedId) {
                    case R.id.radio_ble:
                        frameLayout.setVisibility(View.VISIBLE);
                        imageView.setVisibility(View.GONE);
                        textView.setVisibility(View.GONE);
                        getSupportFragmentManager().beginTransaction().replace(R.id.ble_device, bleFragment).commit();
                        permissions = new RxPermissions(this);
                        permissions.request(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION).subscribe(
                                granted -> {
                                    if (granted) {
                                        group.check(R.id.radio_ble);
                                        turnOnBlueTooth();
                                        refreshDeviceList(true);
                                    } else {
                                        Toast.makeText(this, getString(R.string.blurtooth_need_permission), Toast.LENGTH_LONG).show();
                                    }
                                }
                        ).dispose();
                        break;
                    case R.id.radio_nfc:
                        ble.put("IS_CANCEL", true);
                        group.check(R.id.radio_nfc);
                        mBle.disconnectAll();
                        refreshDeviceList(false);
                        imageView.setVisibility(View.VISIBLE);
                        textView.setVisibility(View.VISIBLE);
                        frameLayout.setVisibility(View.GONE);
                }
            });
        }*/
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.BOTTOM;
            wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
            wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(wlp);
        }
    }


    private final BleScanCallback<BleDevice> scanCallback = new BleScanCallback<BleDevice>() {
        @Override
        public void onLeScan(final BleDevice device, int rssi, byte[] scanRecord) {
            synchronized (mBle.getLocker()) {
                Log.d(TAG, "BLE Device Find====" + device.getBleName());
                adapter.add(device);
            }
        }
    };

    private void turnOnBlueTooth() {
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBle.isBleEnable()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private void refreshDeviceList(boolean start) {
        if (mBle.isScanning()) {
            mBle.stopScan();
        }
        BleDeviceRecyclerViewAdapter.mValues.clear();
        // 由于设备被连接时，会停止广播导致该设备无法被搜索到,所以要添加本APP以连接的设备到列表中
        BleDeviceRecyclerViewAdapter.mValues.addAll(Ble.getInstance().getConnetedDevices());
        adapter.notifyDataSetChanged();
        if (start) {
            mBle.startScan(scanCallback);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefresh(RefreshEvent event) {
        refreshDeviceList(true);
    }


    private final Runnable retry = new Runnable() {
        @Override
        public void run() {
            if (isNFC) {
                return;
            }
            if (Ble.getInstance().getConnetedDevices().size() != 0) {
                startService(new Intent(CommunicationModeSelector.this, BleService.class));
            } else {
                if (device != null) {
                    usbManager.doBusiness(device);
                }
            }
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction(); // get the action of the coming intent
        if (Objects.equals(action, NfcAdapter.ACTION_NDEF_DISCOVERED) // NDEF type
                || Objects.equals(action, NfcAdapter.ACTION_TECH_DISCOVERED)
                || Objects.requireNonNull(action).equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
//            isNFC = true;
            Tag tags = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            nfcTransport.put("ENABLED", true);
            bleHandler.put("ENABLED", false);
            usbTransport.put("ENABLED", false);
            nfcHandler.put("device", tags);
            handlerEverything(true);
        }

    }

    @NonNull
    private HardwareFeatures getFeatures(boolean isNFC) throws Exception {
        String feature;
        try {
            futureTask = new FutureTask<>(() -> {
                Log.d("Features", String.format("method==get_feature===in thread===%d", Thread.currentThread().getId()));
                return Daemon.commands.callAttr("get_feature", isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE);
            });
            executorService.submit(futureTask);
            feature = futureTask.get(5, TimeUnit.SECONDS).toString();
            if (!futureTask.isDone()) {
                futureTask.cancel(true);
            }
            HardwareFeatures features = HardwareFeatures.objectFromData(feature);
            if (features.isInitialized()) {
                SharedPreferences devices = getSharedPreferences("devices", Context.MODE_PRIVATE);
                HardwareFeatures old;
                String backupMessage = "";
                if (devices.contains(features.getBleName())) {
                    old = HardwareFeatures.objectFromData(devices.getString(features.getBleName(), ""));
                    backupMessage = old.getBackupMessage();
                }
                if (!Strings.isNullOrEmpty(backupMessage)) {
                    features.setBackupMessage(backupMessage);
                    devices.edit().putString(features.getBleName(), features.toString()).apply();
                } else {
                    devices.edit().putString(features.getBleName(), feature).apply();
                }
            }
            return features;
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.no_message), Toast.LENGTH_SHORT).show();
            if (ble != null) {
                ble.put("IS_CANCEL", true);
                nfc.put("IS_CANCEL", true);
                protocol.callAttr("notify");
            }
            e.printStackTrace();
            throw e;
        }
    }

    private void handlerEverything(boolean isNFC) {
        isSign = false;
        isActive = false;
        try {
            features = getFeatures(isNFC);
        } catch (Exception e) {
            finish();
            return;
        }
        if (VersionUpgradeActivity.TAG.equals(tag)) {
            // stm32 firmware update by nfc or ble
            if ("hardware".equals(extras)) {
                Intent intent = new Intent(CommunicationModeSelector.this, UpgradeBixinKEYActivity.class);
                intent.putExtra("way", isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE);
                intent.putExtras(Objects.requireNonNull(getIntent().getExtras()));
                intent.putExtra("tag", 1);
                startActivity(intent);
                if (isNFC) {
                    new Handler().postDelayed(() -> EventBus.getDefault().postSticky(new ExecuteEvent()), 1000);
                }
                // ble firmware update by nfc only
            } else if ("ble".equals(extras)) {
                if (isNFC || "usb".equals(way)) {
                    Intent intent = new Intent(CommunicationModeSelector.this, UpgradeBixinKEYActivity.class);
                    intent.putExtra("way", COMMUNICATION_MODE_NFC);
                    intent.putExtras(Objects.requireNonNull(getIntent().getExtras()));
                    intent.putExtra("tag", 2);
                    startActivity(intent);
                    new Handler().postDelayed(() -> EventBus.getDefault().postSticky(new ExecuteEvent()), 2000);
                }
            }
        } else if (HardwareDetailsActivity.TAG.equals(tag) || SettingActivity.TAG_CHANGE_PIN.equals(tag)) {
            dealWithChangePin(isNFC);
        } else if (RecoverySetActivity.TAG.equals(tag)) {
            dealWithWipeDevice(isNFC);
        } else if (SettingActivity.TAG.equals(tag)) {
            String strRandom = UUID.randomUUID().toString().replaceAll("-", "");
            new BusinessAsyncTask().setHelper(this).execute(BusinessAsyncTask.COUNTER_VERIFICATION, strRandom, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE);
            startActivity(new Intent(this, VerificationKEYActivity.class));
//        } else if (ActivatedProcessing.TAG.equals(tag)) {
//            Intent intent = new Intent(this, PinSettingActivity.class);
//            intent.putExtra("tag", tag);
//            intent.putExtra("pin_type", 2);
//            startActivity(intent);
//            finish();
        } else {
            dealWithBusiness(isNFC);
        }
    }

    private void dealWithChangePin(boolean isNFC) {
        if (features.isBootloaderMode()) {
            Toast.makeText(this, R.string.bootloader_mode, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (features.isInitialized()) {
            isChangePin = true;
            new BusinessAsyncTask().setHelper(this).execute(BusinessAsyncTask.CHANGE_PIN, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE);
        } else {
            Toast.makeText(this, R.string.wallet_un_activated_pin, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * dealwith the wipe device logic
     *
     * @param isNFC which communication way we use
     */
    private void dealWithWipeDevice(boolean isNFC) {
        if (features.isBootloaderMode()) {
            Toast.makeText(this, R.string.bootloader_mode, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (features.isInitialized()) {
            new BusinessAsyncTask().setHelper(this).execute(BusinessAsyncTask.WIPE_DEVICE, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE);
//            Intent intent = new Intent(this, PinSettingActivity.class);
//            intent.putExtra("tag", tag);
//            intent.putExtra("pin_type", 1);
//            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.wallet_un_activated, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * dealwith the create wallet 、sign、init logic
     *
     * @param isNFC which communication way we use
     */
    @SuppressLint("CommitPrefEdits")
    private void dealWithBusiness(boolean isNFC) {
        if (features.isBootloaderMode()) {
            Toast.makeText(this, R.string.bootloader_mode, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        boolean isInit = features.isInitialized();//isInit -->  Judge whether it is activated
        if (isInit) {
            if (MultiSigWalletCreator.TAG.equals(tag) || SingleSigWalletCreator.TAG.equals(tag) || PersonalMultiSigWalletCreator.TAG.equals(tag) || CheckHideWalletFragment.TAG.equals(tag) || ImportHistoryWalletActivity.TAG.equals(tag) || "check_xpub".equals(tag)) {
                if (SingleSigWalletCreator.TAG.equals(tag) || CheckHideWalletFragment.TAG.equals(tag)) {
                    if (CheckHideWalletFragment.TAG.equals(tag)) {
                        if (features.isPassphraseProtection()) {
                            customerUI.callAttr("set_pass_state", 1);
                        } else {
                            Toast.makeText(this, getString(R.string.dont_create), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    new BusinessAsyncTask().setHelper(this).execute(BusinessAsyncTask.GET_EXTEND_PUBLIC_KEY_SINGLE, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE, "p2wpkh");
                } else {
                    new BusinessAsyncTask().setHelper(this).execute(BusinessAsyncTask.GET_EXTEND_PUBLIC_KEY, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE);
                }
            } else if (TransactionDetailsActivity.TAG.equals(tag) || SignActivity.TAG.equals(tag) || SignActivity.TAG1.equals(tag) || SignActivity.TAG2.equals(tag) || SignActivity.TAG3.equals(tag) || SendOne2OneMainPageActivity.TAG.equals(tag) || SendOne2ManyMainPageActivity.TAG.equals(tag) || TransactionDetailsActivity.TAG_HIDE_WALLET.equals(tag)) {
                isSign = true;
                if (SignActivity.TAG1.equals(tag) || SignActivity.TAG3.equals(tag)) {
                    if (SignActivity.TAG3.equals(tag)) {
                        //hide wallet sign message -->set_pass_state
                        customerUI.callAttr("set_pass_state", 1);
                    }
                    new BusinessAsyncTask().setHelper(this).execute(BusinessAsyncTask.SIGN_MESSAGE, strinputAddress, extras, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE);
                } else {
                    if (SignActivity.TAG2.equals(tag) || TransactionDetailsActivity.TAG_HIDE_WALLET.equals(tag)) {
                        //hide wallet sign transaction -->set_pass_state
                        customerUI.callAttr("set_pass_state", 1);
                    }
                    new BusinessAsyncTask().setHelper(this).execute(BusinessAsyncTask.SIGN_TX, extras, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE);
                }
            } else if (BackupRecoveryActivity.TAG.equals(tag) || RecoveryActivity.TAG.equals(tag) || BackupMessageActivity.TAG.equals(tag)) {
                if (TextUtils.isEmpty(extras)) {
                    new BusinessAsyncTask().setHelper(this).execute(BusinessAsyncTask.BACK_UP, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE);
                } else {
                    Toast.makeText(this, R.string.recovery_unsupport, Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else if (SettingActivity.TAG.equals(tag)) {
                String strRandom = UUID.randomUUID().toString().replaceAll("-", "");
                //Anti counterfeiting verification
                new BusinessAsyncTask().setHelper(this).execute(BusinessAsyncTask.COUNTER_VERIFICATION, strRandom, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE);
            } else if (BixinKeyBluetoothSettingActivity.TAG_TRUE.equals(tag) || BixinKeyBluetoothSettingActivity.TAG_FALSE.equals(tag)) {
                //bluetooth set to hardware
                if (BixinKeyBluetoothSettingActivity.TAG_TRUE.equals(tag)) {
                    new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.APPLY_SETTING, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE, "setBluetooth", "one");
                } else {
                    new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.APPLY_SETTING, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE, "setBluetooth", "zero");
                }
            } else if (FixBixinkeyNameActivity.TAG.equals(tag)) {
                //fix bixinkey name
                String fixNamed = getIntent().getStringExtra("fixName");
                try {
                    new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.APPLY_SETTING, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE, "label", fixNamed);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (ConfidentialPaymentSettings.TAG.equals(tag)) {
                //Fast pay
                String limit = getIntent().getStringExtra("limit");
                String times = getIntent().getStringExtra("times");
                String noPIN = getIntent().getStringExtra("noPIN");
                String noHard = getIntent().getStringExtra("noHard");
                try {
                    new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.APPLY_SETTING, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE, "fastPay", limit, times, noPIN, noHard);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (SetShutdownTimeActivity.TAG.equals(tag)) {
                //fix bixinkey name
                String shutdownTime = getIntent().getStringExtra("shutdown_time");
                try {
                    new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.APPLY_SETTING, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE, "shutdown_time", shutdownTime);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (!TextUtils.isEmpty(extras) && (BackupMessageActivity.TAG.equals(tag) || RecoveryActivity.TAG.equals(tag))) {
                SharedPreferences preferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
                String locate = preferences.getString("language", "");
                String language = "English".equals(locate) ? "english" : "chinese";
                new BusinessAsyncTask().setHelper(this).execute(BusinessAsyncTask.RECOVER, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE, extras, language);
                return;
            }
            isActive = true;
            Intent intent = new Intent(this, WalletUnActivatedActivity.class);
            if (SingleSigWalletCreator.TAG.equals(tag)) {
                intent.putExtra("tag_Xpub", tag);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            startActivity(intent);
        }
    }

    private void showConnecting() {
        runOnUiThread(() -> {
            if (fragment == null) {
                fragment = new BluetoothConnectingFragment();
            }
            if (hasWindowFocus() && !isFinishing()) {
                getSupportFragmentManager().beginTransaction().replace(R.id.ble_device, fragment, "connecting").commitNow();
                relativeLayout.setVisibility(View.GONE);
            }
        });
    }

    public ReadingOrSendingDialogFragment showReadingDialog(int res) {
        getSupportFragmentManager().beginTransaction().replace(R.id.ble_device, bleFragment).commit();
        ReadingOrSendingDialogFragment fragment = new ReadingOrSendingDialogFragment(res);
        fragment.show(getSupportFragmentManager(), "");
        return fragment;
    }

    public void showErrorDialog(int error, int title) {
        Log.i("ddddjinxoiamin", "onException------: "+error);
        ErrorDialogFragment fragment = new ErrorDialogFragment(error, title);
        fragment.setRunnable(retry);
        fragment.setActivity(this);
        fragment.show(getSupportFragmentManager(), "");
    }

    //Activate interface
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doInit(InitEvent event) {
        if ("Activate".equals(event.getName())) {
            SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
            String locate = preferences.getString("language", "");
            String language = "English".equals(locate) ? "english" : "chinese";
            boolean useSe = Optional.of(event.isUseSE()).orElse(true);
            new BusinessAsyncTask().setHelper(this).execute(BusinessAsyncTask.INIT_DEVICE, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE, "BiXinKEY", language, useSe ? "1" : "");
        }
    }

    //Activate success ,then,get xpub and to back
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void backXpub(SendXpubToSigwallet event) {
        isActive = false;
        //TODO: 获取xpub
        if ("get_xpub_and_send".equals(event.getXpub())) {
            new BusinessAsyncTask().setHelper(this).execute(BusinessAsyncTask.GET_EXTEND_PUBLIC_KEY_SINGLE, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE, "p2wpkh");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doWipe(WipeEvent event) {
        new BusinessAsyncTask().setHelper(this).execute(BusinessAsyncTask.WIPE_DEVICE, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doBusiness(HandlerEvent event) {
        handlerEverything(false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void connecting(ConnectingEvent event) {
        showConnecting();
    }

    @Subscribe
    public void setPin(PinEvent event) {
        if (!Strings.isNullOrEmpty(event.getPinCode())) {
            customerUI.put("pin", event.getPinCode());
        }
    }

    @Subscribe
    public void changePin(ChangePinEvent event) {
        if (!Strings.isNullOrEmpty(event.toString())) {
            customerUI.put("pin", event.toString());
        }
    }

    @Subscribe
    public void setPassphrass(PinEvent event) {
        if (!Strings.isNullOrEmpty(event.getPassphrass())) {
            customerUI.put("passphrase", event.getPassphrass());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showReading(ReadingEvent event) {
        dialogFragment = showReadingDialog(R.string.reading_dot);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onButtonRequest(ButtonRequestEvent event) {
        if (isSign) {
            synchronized (CommunicationModeSelector.class) {
                if (runnables.size() != 0) {
                    runOnUiThread(runnables.get(0));
                    // in order to prevent repeat invoke
                    runnables.clear();
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void exist(ExistEvent event) {
        finish();
    }

    public void onSignSuccessful(SignResultEvent resultEvent) {
        String signedRaw = resultEvent.getSignedRaw();
        if (!TextUtils.isEmpty(signedRaw)) {
            EventBus.getDefault().post(new SecondEvent("finish"));
            Intent intent1 = new Intent(this, TransactionDetailsActivity.class);
            intent1.putExtra("signed_raw_tx", signedRaw);
            intent1.putExtra("isIsmine", true);
            startActivity(intent1);
            finish();
        }
    }

    public void onSignSuccessful(SendSignBroadcastEvent resultEvent) {
        EventBus.getDefault().removeStickyEvent(SendSignBroadcastEvent.class);
        String signedTx = resultEvent.getSignTx();
        String txHash;
        if (!TextUtils.isEmpty(signedTx)) {
            try {
                Gson gson = new Gson();
                GetnewcreatTrsactionListBean getnewcreatTrsactionListBean = gson.fromJson(signedTx, GetnewcreatTrsactionListBean.class);
                String tx = getnewcreatTrsactionListBean.getTx();
                txHash = getnewcreatTrsactionListBean.getTxid();
                //  Log.i("onSignSuccessful", "onSignSuccessful:++ " + tx);
                Daemon.commands.callAttr("broadcast_tx", tx);
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.broad_fail), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return;
            }
            EventBus.getDefault().post(new SecondEvent("finish"));
            EventBus.getDefault().post(new FirstEvent("22"));
            EventBus.getDefault().postSticky(new SecondEvent("ActivateFinish"));
            Intent intent1 = new Intent(this, TransactionDetailsActivity.class);
            intent1.putExtra("listType", "history");
            intent1.putExtra("keyValue", "B");
            intent1.putExtra("tx_hash", txHash);
            intent1.putExtra("isIsmine", true);
            intent1.putExtra("unConfirmStatus", "broadcast_complete");
            startActivity(intent1);
            finish();
        }
    }


    @SingleClick
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_cancel:
                if (BixinKeyBluetoothSettingActivity.TAG_TRUE.equals(tag) || BixinKeyBluetoothSettingActivity.TAG_FALSE.equals(tag)) {
                    EventBus.getDefault().post(new SetBluetoothEvent("recovery_status"));
                }
                if (mBle != null) {
                    refreshDeviceList(false);
                    mBle.disconnectAll();
                }
                finish();
                break;
            case R.id.text_input_publickey_by_hand:
                if (mBle != null) {
                    refreshDeviceList(false);
                }
                runOnUiThread(runnables.get(0));
                finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this, getString(R.string.open_bluetooth), Toast.LENGTH_LONG).show();
        } else if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            refreshDeviceList(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (NfcUtils.mNfcAdapter != null && NfcUtils.mNfcAdapter.isEnabled()) {
            // enable nfc discovery for the app
            Log.i("NFC", "为本App启用NFC感应");
            NfcUtils.mNfcAdapter.enableForegroundDispatch(this, NfcUtils.mPendingIntent, NfcUtils.mIntentFilter, NfcUtils.mTechList);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (dialogFragment != null) {
            dialogFragment.dismiss();
        }
        if (NfcUtils.mNfcAdapter != null && NfcUtils.mNfcAdapter.isEnabled()) {
            // disable nfc discovery for the app
            NfcUtils.mNfcAdapter.disableForegroundDispatch(this);
            Log.i("NFC", "禁用本App的NFC感应");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyHandler.myHandler = null;
        NfcUtils.mNfcAdapter = null;
        EventBus.getDefault().unregister(this);
        if ("usb".equals(way)) {
            CustomerUsbManager.getInstance(this).unRegister(this);
        }
    }

    @Override
    public void onPreExecute() {
        if (isActive) {
            return;
        }
        if (isSign) {
            if (SignActivity.TAG1.equals(tag) || SignActivity.TAG3.equals(tag)) {
                dialogFragment = showReadingDialog(R.string.message_loading);
            } else {
                dialogFragment = showReadingDialog(R.string.transaction_loading);
            }

        } else if (MultiSigWalletCreator.TAG.equals(tag) || SingleSigWalletCreator.TAG.equals(tag) || PersonalMultiSigWalletCreator.TAG.equals(tag) || CheckHideWalletFragment.TAG.equals(tag) || ImportHistoryWalletActivity.TAG.equals(tag)) {
            // 获取公钥之前需完成的工作
            if (!SingleSigWalletCreator.TAG.equals(tag)) {
                dialogFragment = showReadingDialog(R.string.reading_dot);
            }
        }
    }

    @Override
    public void onException(Exception e) {
        Log.i("jinxoaminsssss", "onException-----: "+e.getMessage());
        if (dialogFragment != null) {
            dialogFragment.dismiss();
        }
        EventBus.getDefault().post(new FinishEvent());
        // EventBus.getDefault().post(new SendingFailedEvent(e));
//        if (hasWindowFocus()) {
        if (BixinExceptions.PIN_INVALID.getMessage().equals(e.getMessage()) || e.getMessage().contains("May be BiXin cannot pair with your device or invaild password")) {
            showErrorDialog(0, R.string.pin_wrong);
        } else if (BixinExceptions.UN_PAIRABLE.equals(e.getMessage()) || e.getMessage().contains("(7, 'PIN invalid')")) {
            showErrorDialog(R.string.try_another_key, R.string.unpair);
        } else if (BixinExceptions.TRANSACTION_FORMAT_ERROR.getMessage().equals(e.getMessage())) {
            showErrorDialog(R.string.sign_failed, R.string.transaction_parse_error);
        } else if (e.getMessage().contains("Sign failed, May be BiXin cannot pair with your device")|| e.getMessage().contains("Can't Pair With You Device When Sign Message")) {
            showErrorDialog(R.string.try_another_key, R.string.sign_failed_device);
        } else if (e.getMessage().contains(BixinExceptions.BLE_RESPONSE_READ_TIMEOUT.getMessage())) {
            showErrorDialog(R.string.timeout_error, R.string.read_pk_failed);
        } else {
            showErrorDialog(R.string.key_wrong_prompte, R.string.read_pk_failed);
        }
//        }

    }

    @Override
    public void onResult(String s) {
        if (dialogFragment != null) {
            dialogFragment.dismiss();
        }
        if (isActive) {
            EventBus.getDefault().post(new ResultEvent(s));
            finish();
            return;
        }

        if (MultiSigWalletCreator.TAG.equals(tag) || SingleSigWalletCreator.TAG.equals(tag) || PersonalMultiSigWalletCreator.TAG.equals(tag) || CheckHideWalletFragment.TAG.equals(tag) || ImportHistoryWalletActivity.TAG.equals(tag) || "check_xpub".equals(tag)) {
            xpub = s;
            EventBus.getDefault().post(new FinishEvent());
            if (ImportHistoryWalletActivity.TAG.equals(tag)) {
                Intent intent1 = new Intent(this, ChooseHistryWalletActivity.class);
                intent1.putExtra("histry_xpub", xpub);
                startActivity(intent1);
            } else if (SingleSigWalletCreator.TAG.equals(tag)) {
                EventBus.getDefault().post(new ReceiveXpub(xpub, features.getDeviceId()));
            } else if ("check_xpub".equals(tag)) {
                Intent intent = new Intent(this, CheckXpubResultActivity.class);
                intent.putExtra("label", Optional.ofNullable(features.getLabel()).orElse("BixinKEY"));
                intent.putExtra("xpub", s);
                startActivity(intent);
            } else if (CheckHideWalletFragment.TAG.equals(tag)) {
                EventBus.getDefault().post(new CheckHideWalletEvent(xpub, features.getDeviceId()));
            } else if (PersonalMultiSigWalletCreator.TAG.equals(tag)) {
                EventBus.getDefault().post(new PersonalMutiSigEvent(xpub, features.getDeviceId(),features.getLabel()));
            } else if (MultiSigWalletCreator.TAG.equals(tag)) {
                EventBus.getDefault().post(new MutiSigWalletEvent(xpub, features.getDeviceId(),features.getLabel()));
            } else {
                runOnUiThread(runnables.get(1));
            }
        } else if (isSign) {
            if (SignActivity.TAG1.equals(tag) || SignActivity.TAG3.equals(tag)) {
                EventBus.getDefault().post(new SignMessageEvent(s));
            } else if (SendOne2OneMainPageActivity.TAG.equals(tag) || SendOne2ManyMainPageActivity.TAG.equals(tag)) {
                if (runnables.size() != 0) {
                    onSignSuccessful(new SendSignBroadcastEvent(s));
                    return;
                }
                EventBus.getDefault().postSticky(new SendSignBroadcastEvent(s));
            } else {
                if (runnables.size() != 0) {
                    onSignSuccessful(new SignResultEvent(s));
                    return;
                }
                EventBus.getDefault().post(new SignResultEvent(s));
            }
            // runOnUiThread(runnables.get(0));
        } else if (BackupRecoveryActivity.TAG.equals(tag) || BackupMessageActivity.TAG.equals(tag) || RecoveryActivity.TAG.equals(tag)) {
            if (TextUtils.isEmpty(extras)) {
                SharedPreferences backup = getSharedPreferences("backup", Context.MODE_PRIVATE);
                backup.edit().putString(features.getDeviceId(), Strings.isNullOrEmpty(features.getLabel()) ? features.getBleName() + ":" + s : features.getLabel() + ":" + s).apply();
                Intent intent = new Intent(this, BackupMessageActivity.class);
                intent.putExtra("label", Strings.isNullOrEmpty(features.getLabel()) ? features.getBleName() : features.getLabel());
                intent.putExtra("tag", "backup");
                intent.putExtra("message", s);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, RecoveryResult.class);
                intent.putExtra("tag", s);
                startActivity(intent);
            }
        } else if (RecoverySetActivity.TAG.equals(tag) || HardwareDetailsActivity.TAG.equals(tag) || SettingActivity.TAG_CHANGE_PIN.equals(tag) || SettingActivity.TAG.equals(tag)) {
            EventBus.getDefault().postSticky(new ResultEvent(s));
        } else if (BixinKeyBluetoothSettingActivity.TAG_TRUE.equals(tag) || BixinKeyBluetoothSettingActivity.TAG_FALSE.equals(tag)) {
            EventBus.getDefault().post(new SetBluetoothEvent(s));
        } else if (ConfidentialPaymentSettings.TAG.equals(tag)) {
            EventBus.getDefault().post(new FastPayEvent(s));
        } else if (FixBixinkeyNameActivity.TAG.equals(tag)) {
            String oldBleName = features.getBleName();
            EventBus.getDefault().post(new FixAllLabelnameEvent(oldBleName, s));
        } else if (SetShutdownTimeActivity.TAG.equals(tag)) {
            EventBus.getDefault().post(new ShutdownTimeEvent(s));
        }
        finish();
    }

    @Override
    public void onCancelled() {
//        runOnUiThread(() -> Toast.makeText(this, getString(R.string.task_cancle), Toast.LENGTH_SHORT).show());
    }

    public static class MyHandler extends Handler {
        private static volatile MyHandler myHandler;
        private FragmentActivity fragmentActivity;

        private MyHandler(FragmentActivity activity) {
            this.fragmentActivity = activity;
        }

        static MyHandler getInstance(FragmentActivity fragmentActivity) {
            if (myHandler == null) {
                synchronized (MyHandler.class) {
                    if (myHandler == null) {
                        myHandler = new MyHandler(fragmentActivity);
                    }
                }
            }
            return myHandler;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            String tag = ((CommunicationModeSelector) fragmentActivity).tag;
            boolean isSign = ((CommunicationModeSelector) fragmentActivity).isSign;
            boolean isChangePin = ((CommunicationModeSelector) fragmentActivity).isChangePin;
            switch (msg.what) {
                case PIN_CURRENT:
                    Intent intent = new Intent(fragmentActivity, PinSettingActivity.class);
                    if (!isSign) {
                        intent.putExtra("tag", tag);
                    }
                    if (isChangePin) {
                        intent.putExtra("pin_type", 3);
                    }
                    fragmentActivity.startActivity(intent);
                    break;
                case PIN_NEW_FIRST:
                    Intent intent1 = new Intent(fragmentActivity, PinSettingActivity.class);
                    intent1.putExtra("pin_type", PIN_NEW_FIRST);
                    intent1.putExtra("tag", SettingActivity.TAG_CHANGE_PIN);
                    fragmentActivity.startActivity(intent1);
                    break;
                case BUTTON_REQUEST:
                    EventBus.getDefault().postSticky(new ButtonRequestEvent());
                    break;
                case PASS_NEW_PASSPHRASS:
                case PASS_PASSPHRASS:
                    //Set password
                    Intent intent3 = new Intent(fragmentActivity, HideWalletSetPassActivity.class);
                    fragmentActivity.startActivity(intent3);
                    break;
            }
        }
    }
}