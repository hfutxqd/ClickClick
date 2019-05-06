package xyz.imxqd.clickclick.payment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;

import java.util.List;

import xyz.imxqd.clickclick.log.LogUtils;

public class AndroidPayActivity extends Activity implements PurchasesUpdatedListener, BillingClientStateListener {

    public static final int BILLING_MANAGER_NOT_INITIALIZED  = -1;

    private BillingClient billingClient;
    private boolean isServiceConnected = false;
    private int billingClientResponseCode = BILLING_MANAGER_NOT_INITIALIZED;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        billingClient = BillingClient.newBuilder(this).setListener(this).build();
        billingClient.startConnection(this);
    }

    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {

    }

    @Override
    public void onBillingSetupFinished(int responseCode) {
        if (responseCode == BillingClient.BillingResponse.OK) {
            isServiceConnected = true;
        }
    }

    @Override
    public void onBillingServiceDisconnected() {
        isServiceConnected = false;
    }

    public void startServiceConnection(final Runnable executeOnSuccess) {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@BillingClient.BillingResponse int billingResponseCode) {
                LogUtils.d("Setup finished. Response code: " + billingResponseCode);

                if (billingResponseCode == BillingClient.BillingResponse.OK) {
                    isServiceConnected = true;
                    if (executeOnSuccess != null) {
                        executeOnSuccess.run();
                    }
                }
                billingClientResponseCode = billingResponseCode;
            }

            @Override
            public void onBillingServiceDisconnected() {
                isServiceConnected = false;
            }
        });
    }

    private void executeServiceRequest(Runnable runnable) {
        if (isServiceConnected) {
            runnable.run();
        } else {
            startServiceConnection(runnable);
        }
    }
}
