package cz.muni.fi.anglictina.utils;

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public abstract class OnSwipeTouchListener implements OnTouchListener {

    public final GestureDetector gestureDetector;
    private boolean onDown;
    private View mView;

    public OnSwipeTouchListener (Context ctx, boolean onDown){
        gestureDetector = new GestureDetector(ctx, new GestureListener());
        this.onDown = onDown;
    }

    public void setView(View view) {
        mView = view;
    }

    private final class GestureListener extends SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return onViewDown(mView);
        }




//        @Override
//        public boolean onSingleTapConfirmed(MotionEvent e) {
//            onClick(mView);
//            return true;
//        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            onClick(mView);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            onLongClick(mView);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
//                            onSwipeRight();
                            onSwipeLeft();
                        } else {
                            onSwipeLeft();
                        }
                    }
                    result = true;
                }
                else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
//                        onSwipeBottom();
                    } else {
//                        onSwipeTop();
                    }
                }
                result = true;

            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

//    public void onSwipeRight() {
//    }

    abstract public boolean onViewDown(View v);

    public void onSwipeLeft() {
    }

    public void onClick(View v){
    }
    public void onLongClick(View v) {

    }

//    public void onSwipeTop() {
//    }
//
//    public void onSwipeBottom() {
//    }
}