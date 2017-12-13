package com.example.fragmenttransitions;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

/**
 * Created by Owner on 2017. 08. 08..
 */

public class BaseFragment
        extends Fragment {
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startPostponedEnterTransition();
    }

    @NonNull
    public final <T extends BaseKey> T getKey() {
        T key = getArguments() != null ? getArguments().getParcelable("KEY") : null;
        if(key == null) {
            throw new NullPointerException("Key should not be null");
        }
        return key;
    }
}
