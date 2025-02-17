package com.prey.barcodereader;

import android.app.Activity;

import com.prey.PreyLogger;
import com.prey.barcodereader.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

class BarcodeGraphicTracker extends Tracker<Barcode> {
    private GraphicOverlay<BarcodeGraphic> mOverlay;
    private BarcodeGraphic mGraphic;
    private BarcodeCaptureActivity activity;

    BarcodeGraphicTracker(GraphicOverlay<BarcodeGraphic> overlay, BarcodeGraphic graphic,BarcodeCaptureActivity activity) {
        mOverlay = overlay;
        mGraphic = graphic;
        this.activity=activity;
    }

    @Override
    public void onNewItem(int id, Barcode item) {
        mGraphic.setId(id);
    }

    @Override
    public void onUpdate(Detector.Detections<Barcode> detectionResults, Barcode item) {
        mOverlay.add(mGraphic);
        mGraphic.updateItem(item);
        activity.updateBarcode(item);
    }

    @Override
    public void onMissing(Detector.Detections<Barcode> detectionResults) {
        mOverlay.remove(mGraphic);
    }

    @Override
    public void onDone() {
        mOverlay.remove(mGraphic);
    }
}
