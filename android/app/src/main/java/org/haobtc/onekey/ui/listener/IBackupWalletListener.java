package org.haobtc.onekey.ui.listener;

import org.haobtc.onekey.mvp.base.IBaseListener;

public interface IBackupWalletListener extends IBaseListener, IUpdateTitleListener {

    void onReadyGo();
}
