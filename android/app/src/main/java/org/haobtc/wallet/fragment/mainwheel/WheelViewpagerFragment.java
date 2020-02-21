package org.haobtc.wallet.fragment.mainwheel;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.chaquo.python.PyObject;
import com.google.gson.Gson;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.ReceivedPageActivity;
import org.haobtc.wallet.activities.SendOne2OneMainPageActivity;
import org.haobtc.wallet.activities.onlywallet.CheckWalletDetailActivity;
import org.haobtc.wallet.bean.MainNewWalletBean;
import org.haobtc.wallet.utils.Daemon;

/**
 * A simple {@link Fragment} subclass.
 */
public class WheelViewpagerFragment extends Fragment {

    private TextView wallet_card_name;
    private TextView walletpersonce;
    private TextView walletBlance;

    private String name;
    private String personce;
    private String balance;
    private Button btnLeft;
    private Button btncenetr;
    private PyObject select_wallet;
    private TextView tetCny;
    private PyObject pyObject;
    private CardView card_view;
    private Button btn_appWallet;
    //    private Button btnright;

    public WheelViewpagerFragment(String name, String personce, String balance) {
        this.name = name;
        this.personce = personce;
        this.balance = balance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_wheel_viewpager, container, false);
        wallet_card_name = view.findViewById(R.id.wallet_card_name);
        walletpersonce = view.findViewById(R.id.wallet_card_tv2);
        walletBlance = view.findViewById(R.id.wallet_card_tv4);

        btnLeft = view.findViewById(R.id.wallet_card_bn1);
        btncenetr = view.findViewById(R.id.wallet_card_bn2);
        btn_appWallet = view.findViewById(R.id.app_wallet);
        tetCny = view.findViewById(R.id.tet_Cny);
        card_view = view.findViewById(R.id.wallet_card);
        init();
        initdata();

        return view;
    }

    private void init() {
        wallet_card_name.setText(name);
        walletpersonce.setText(personce);
        if (!TextUtils.isEmpty(personce)){
            if (personce.equals("standard")){
                btn_appWallet.setVisibility(View.VISIBLE);
                walletpersonce.setVisibility(View.GONE);
            }else{
                String of = personce.replaceAll("of", "/");
                walletpersonce.setText(of);
            }
        }

        if (!TextUtils.isEmpty(balance)) {
            if (balance.contains("(")) {
                String substring = balance.substring(0, balance.indexOf("("));
                walletBlance.setText(substring);
                String userIdJiequ = balance.substring(balance.indexOf("("));
                String replace = userIdJiequ.replace(")", "");
                String replace1 = replace.replace("(", "");
                tetCny.setText(String.format("≈%s", replace1));
            } else {
                walletBlance.setText(balance);
                String replaceAll = balance.replaceAll(" ", "");
                try {
                    pyObject = Daemon.commands.callAttr("get_exchange_currency", "base", replaceAll);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (pyObject!=null) {
                    tetCny.setText(String.format("≈%sCNY", pyObject.toString()));
                }

            }
        }
    }

    private void initdata() {
        btnLeft.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SendOne2OneMainPageActivity.class);
            intent.putExtra("wallet_name", name);
            intent.putExtra("wallet_balance", balance);
            startActivity(intent);
        });
        btncenetr.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ReceivedPageActivity.class);
            startActivity(intent);
        });
        card_view.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CheckWalletDetailActivity.class);
            intent.putExtra("wallet_name", name);
            startActivity(intent);
        });

    }

    public void refreshList() {
        //get wallet message
        try {
            Daemon.commands.callAttr("load_wallet", name);
            select_wallet = Daemon.commands.callAttr("select_wallet", name);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (select_wallet != null) {
            String toString = select_wallet.toString();
            Log.i("select_wallet", "select_wallet+++: " + toString);
            Gson gson = new Gson();
            MainNewWalletBean mainWheelBean = gson.fromJson(toString, MainNewWalletBean.class);
            String walletType = mainWheelBean.getWalletType();
            String balanceC = mainWheelBean.getBalance();
            String nameAC = mainWheelBean.getName();
            String streplaceC = walletType.replaceAll("of", "/");
            wallet_card_name.setText(nameAC);
            walletpersonce.setText(streplaceC);
            if (!TextUtils.isEmpty(balanceC)) {
                if (balanceC.contains("(")) {
                    String substring = balanceC.substring(0, balanceC.indexOf("("));
                    walletBlance.setText(substring);
                } else {
                    walletBlance.setText(balanceC);
                }
            }

        }
    }

}