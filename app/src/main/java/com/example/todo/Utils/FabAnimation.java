package com.example.todo.Utils;

import android.animation.Animator;
import android.view.View;

public class FabAnimation {
    public static void init(View view){
        view.setVisibility(View.GONE);
        view.setTranslationY(view.getHeight());
        view.setAlpha(0f);

    }
    public static boolean rotate(View view,boolean clock){
        view.animate().setDuration(200).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        }).rotation(clock?135f:0f);
        return clock;
    }

    public static void fabOpen(View view){
        view.setVisibility(View.VISIBLE);
        view.setTranslationY(view.getHeight());
        view.setAlpha(0f);
        view.animate().setDuration(200).translationY(0).alpha(1f).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        }).start();
    }
    public static void fabClose(View view){
        view.setAlpha(1f);
        view.setTranslationY(0);
        view.animate().setDuration(200).translationY(view.getHeight()).alpha(0f).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        }).start();
    }
}
