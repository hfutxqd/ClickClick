package xyz.imxqd.clickclick.payment;

import android.content.Intent;
import android.net.Uri;

public class PayUtil {
    public static Intent makeAlipayIntent() {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        String payUrl = "HTTPS://QR.ALIPAY.COM/FKX08754UVTJSBSTAYXN8C";
        intent.setData(Uri.parse("alipayqr://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=" + payUrl));
        return intent;
    }

    public static Intent makePaypalIntent() {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setData(Uri.parse("https://paypal.me/imxqd"));
        return intent;
    }

}
