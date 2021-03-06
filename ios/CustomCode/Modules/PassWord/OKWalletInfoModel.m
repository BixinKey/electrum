//
//  OKWalletInfoModel.m
//  OneKey
//
//  Created by xiaoliang on 2020/12/7.
//  Copyright © 2020 OneKey. All rights reserved.
//

#import "OKWalletInfoModel.h"

@implementation OKWalletInfoModel
+ (OKWalletType)walletTypeWithStr:(NSString *)type {

    OKWalletType walletType = OKWalletTypeHD;
    #define OK_WALLET_TYPE_CASE(argTypeStr,argType) \
        else if([type ignoreCaseCointain:(argTypeStr)]) {walletType = (argType);}
    if ([type ignoreCaseCointain:@"hd-standard"]) {
        walletType = OKWalletTypeHD;
    }
    OK_WALLET_TYPE_CASE(@"derived-standard",    OKWalletTypeHD)
    OK_WALLET_TYPE_CASE(@"private-standard",    OKWalletTypeIndependent)
    OK_WALLET_TYPE_CASE(@"watch-standard",      OKWalletTypeObserve)
    OK_WALLET_TYPE_CASE(@"hw-derived-",         OKWalletTypeHardware)
    OK_WALLET_TYPE_CASE(@"hd-hw-",              OKWalletTypeHardware)
    OK_WALLET_TYPE_CASE(@"hw-",                 OKWalletTypeMultipleSignature)
    OK_WALLET_TYPE_CASE(@"standard",            OKWalletTypeIndependent)
    return walletType;
}

- (void)setType:(NSString *)type {
    _type = type;
    if ([type ignoreCaseCointain:@"eth"]) {
        self.chainType = OKWalletChainTypeETHLike;
    } else {
        self.chainType = OKWalletChainTypeBTC;
    }
    self.walletType = [OKWalletInfoModel walletTypeWithStr:type];
}

- (NSAttributedString *)walletTypeDesc {
    NSString *desc;
    switch (self.walletType) {
        case OKWalletTypeHD: desc = @"HD".localized; break;
        case OKWalletTypeHardware: {
            NSString *deviceName = [[OKDevicesManager sharedInstance] getDeviceModelWithID:self.device_id].deviceInfo.label;
            if (deviceName.length) {
                desc = [NSString stringWithFormat:@"  %@", deviceName];;
                if (desc.length > 16) {
                    desc = [desc substringToIndex:16];
                }
            } else {
                desc = @"hardware".localized;
            }
        } break;
        case OKWalletTypeObserve: desc = @"Observation".localized; break;
        default: desc = @""; break;
    }

    NSMutableAttributedString *attributedString = [[NSMutableAttributedString alloc] initWithString:desc];
    if (self.walletType == OKWalletTypeHardware) {
        NSTextAttachment *attchment = [[NSTextAttachment alloc] init];
        attchment.bounds = CGRectMake(0,-2,8,12);
        attchment.image = [UIImage imageNamed:@"device_white"];
        NSAttributedString *attchmentStr = [NSAttributedString attributedStringWithAttachment:attchment];
        [attributedString insertAttributedString:attchmentStr atIndex:1];
    }

    return attributedString;
}

- (OKWalletCoinType)walletCoinType {
    NSString *coinType = self.coinType;
    OKWalletCoinType type = OKWalletCoinTypeUnknown;
    if ([coinType ignoreCaseCointain:@"btc"]) {
        type = OKWalletCoinTypeBTC;
    } else if ([coinType ignoreCaseCointain:@"eth"]) {
        type = OKWalletCoinTypeETH;
    }
    return type;
}

@end
