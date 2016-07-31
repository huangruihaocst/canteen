package com.huangruihao.canteen;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableField;

/**
 * Created by alexwang on 7/31/16.
 */
public class Canteen {
    public final ObservableField<String> name =
            new ObservableField<>();
    public Canteen(String name) {
        this.name.set(name);
    }
}
