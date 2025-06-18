package com.example.gamedex.ui.adapters;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.example.gamedex.R;

/**
 * Clase para añadir efectos táctiles a las vistas
 */
public class NeonTouchEffect implements View.OnTouchListener {

    private final Animation scaleDownAnim;
    private final Animation scaleUpAnim;

    public NeonTouchEffect(Context context) {
        scaleDownAnim = AnimationUtils.loadAnimation(context, R.anim.card_press_animation);
        scaleUpAnim = AnimationUtils.loadAnimation(context, R.anim.card_release_animation);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Cuando se presiona la vista
                view.startAnimation(scaleDownAnim);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // Cuando se suelta o se cancela el toque
                view.startAnimation(scaleUpAnim);
                break;
        }
        // Devolvemos false para que el onClick también funcione
        return false;
    }


}