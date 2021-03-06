package org.haobtc.onekey.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.common.base.Strings;
import com.noober.background.drawable.DrawableCreator;
import me.jessyan.autosize.utils.AutoSizeUtils;
import org.haobtc.onekey.R;
import org.haobtc.onekey.bean.WalletAccountBalanceInfo;
import org.haobtc.onekey.constant.Vm;

/** @Description: java类作用描述 @Author: peter Qin */
public class SelectAccountAdapter
        extends BaseQuickAdapter<WalletAccountBalanceInfo, BaseViewHolder> {

    public SelectAccountAdapter() {
        super(R.layout.item_select_account);
    }

    @Override
    protected void convert(BaseViewHolder helper, WalletAccountBalanceInfo item) {
        helper.setText(R.id.text_name, item.getName());
        View view = helper.getView(R.id.layout_background);
        if (!Strings.isNullOrEmpty(item.getHardwareLabel())) {
            helper.setGone(R.id.hardware_label_img, true);
        } else {
            helper.setGone(R.id.hardware_label_img, false);
        }
        Vm.CoinType coinType = item.getCoinType();
        int walletType = item.getWalletType();

        int bgColor = Color.parseColor("#F7931B");
        if (coinType == Vm.CoinType.BTC) {
            bgColor = Color.parseColor("#F7931B");
        } else if (coinType == Vm.CoinType.ETH) {
            bgColor = Color.parseColor("#3E5BF2");
        }
        Drawable build =
                new DrawableCreator.Builder()
                        .setCornersRadius(AutoSizeUtils.dp2px(mContext, 13F))
                        .setSolidColor(bgColor)
                        .build();
        view.setBackground(build);

        if (walletType == Vm.WalletType.MAIN) {
            helper.setGone(R.id.type_layout, true);
            helper.setText(R.id.text_type, mContext.getString(R.string.main_account));
        } else if (walletType == Vm.WalletType.HARDWARE) {
            helper.setGone(R.id.type_layout, true);
            helper.setText(R.id.text_type, item.getHardwareLabel());
        } else if (walletType == Vm.WalletType.IMPORT_WATCH) {
            helper.setGone(R.id.type_layout, true);
            helper.setText(R.id.text_type, mContext.getString(R.string.watch));
        } else {
            helper.setGone(R.id.type_layout, false);
        }
        String address = item.getAddress();
        String front6 = address.substring(0, 6);
        String after6 = address.substring(address.length() - 6);
        helper.setText(R.id.text_address, String.format("%s…%s", front6, after6));
        SharedPreferences preferences =
                mContext.getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        String loadWalletName =
                preferences.getString(
                        org.haobtc.onekey.constant.Constant.CURRENT_SELECTED_WALLET_NAME, "");
        ImageView chooseView = helper.getView(R.id.img_choose);
        if (loadWalletName.equals(item.getId())) {
            chooseView.setVisibility(View.VISIBLE);
        } else {
            chooseView.setVisibility(View.GONE);
        }
        helper.setText(R.id.tv_amount, item.getBalance().getBalanceFormat(8));
        helper.addOnClickListener(R.id.rel_background);
    }
}
