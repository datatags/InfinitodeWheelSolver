package me.datatags.infinitodewheelsolver.derived;

import com.badlogic.gdx.pay.Information;
import com.badlogic.gdx.pay.PurchaseManager;
import com.badlogic.gdx.pay.PurchaseManagerConfig;
import com.badlogic.gdx.pay.PurchaseObserver;

public class NullPurchaseManager implements PurchaseManager {
    @Override
    public String storeName() {
        return "";
    }

    @Override
    public void install(PurchaseObserver purchaseObserver, PurchaseManagerConfig purchaseManagerConfig, boolean b) {
    }

    @Override
    public boolean installed() {
        return false;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void purchase(String s) {
    }

    @Override
    public void purchaseRestore() {
    }

    @Override
    public Information getInformation(String s) {
        return null;
    }
}
