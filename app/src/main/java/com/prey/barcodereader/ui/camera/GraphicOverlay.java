package com.prey.barcodereader.ui.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.gms.vision.CameraSource;

import java.util.HashSet;
import java.util.Set;

public class GraphicOverlay<T extends GraphicOverlay.Graphic> extends View {
    private final Object mLock = new Object();
    private int mPreviewWidth;
    private float mWidthScaleFactor = 1.0f;
    private int mPreviewHeight;
    private float mHeightScaleFactor = 1.0f;
    private int mFacing = CameraSource.CAMERA_FACING_BACK;
    private Set<T> mGraphics = new HashSet<>();
    private T mFirstGraphic;

    public static abstract class Graphic {
        private GraphicOverlay mOverlay;
        public Graphic(GraphicOverlay overlay) {
            mOverlay = overlay;
        }
        public abstract void draw(Canvas canvas);
        public float scaleX(float horizontal) {
            return horizontal * mOverlay.mWidthScaleFactor;
        }
        public float scaleY(float vertical) {
            return vertical * mOverlay.mHeightScaleFactor;
        }
        public float translateX(float x) {
            if (mOverlay.mFacing == CameraSource.CAMERA_FACING_FRONT) {
                return mOverlay.getWidth() - scaleX(x);
            } else {
                return scaleX(x);
            }
        }
        public float translateY(float y) {
            return scaleY(y);
        }
        public void postInvalidate() {
            mOverlay.postInvalidate();
        }
    }

    public GraphicOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void clear() {
        synchronized (mLock) {
            mGraphics.clear();
            mFirstGraphic = null;
        }
        postInvalidate();
    }

    public void add(T graphic) {
        synchronized (mLock) {
            mGraphics.add(graphic);
            if (mFirstGraphic == null) {
                mFirstGraphic = graphic;
            }
        }
        postInvalidate();
    }

    public void remove(T graphic) {
        synchronized (mLock) {
            mGraphics.remove(graphic);
            if (mFirstGraphic != null && mFirstGraphic.equals(graphic)) {
                mFirstGraphic = null;
            }
        }
        postInvalidate();
    }

    public T getFirstGraphic() {
        synchronized (mLock) {
            return mFirstGraphic;
        }
    }

    public void setCameraInfo(int previewWidth, int previewHeight, int facing) {
        synchronized (mLock) {
            mPreviewWidth = previewWidth;
            mPreviewHeight = previewHeight;
            mFacing = facing;
        }
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        synchronized (mLock) {
            if ((mPreviewWidth != 0) && (mPreviewHeight != 0)) {
                mWidthScaleFactor = (float) canvas.getWidth() / (float) mPreviewWidth;
                mHeightScaleFactor = (float) canvas.getHeight() / (float) mPreviewHeight;
            }
            for (Graphic graphic : mGraphics) {
                graphic.draw(canvas);
            }
        }
    }

}