package xyz.imxqd.clickclick.model.web;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public abstract class SimpleHttpObserver<T> implements Observer<T> {
    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onComplete() {

    }
}
