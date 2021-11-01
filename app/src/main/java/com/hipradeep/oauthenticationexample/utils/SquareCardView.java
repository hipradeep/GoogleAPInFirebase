package com.hipradeep.oauthenticationexample.utils;


import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

class SquareCardView extends CardView {


    public SquareCardView(@NonNull Context context ) {
        super(context);

    }

    public SquareCardView(@NonNull Context context, @Nullable AttributeSet attrs ) {
        super(context, attrs);

    }

    public SquareCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    public void onMeasure(int widthMeasureSpec,int  ignoredHeightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}