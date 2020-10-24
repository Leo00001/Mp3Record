package com.baiyuas.media;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.baiyuas.media.util.ScreenUtils;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


/**
 * author: rivenlee
 * date: 2018/10/30
 * email: rivenlee0@gmail.com
 */
public abstract class BaseDialogFragment extends DialogFragment {

    protected Context mContext;
    protected View mView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_recordDialog);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //dialog基本设置
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCancelable(false);
        getDialog().setCanceledOnTouchOutside(false);

        mView = inflater.inflate(setView(), container, false);
        Window window = getDialog().getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            if (lp != null) {
                lp.gravity = Gravity.BOTTOM;
                lp.width = ScreenUtils.getScreenWidth(mContext);
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                window.setAttributes(lp);
            }
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
        return mView;
    }

    protected <V extends View> V find(@IdRes int id) {
        return mView.findViewById(id);
    }

    protected abstract int setView();
}
