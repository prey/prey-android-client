package com.prey.barcodereader;

import android.app.Activity;

import com.prey.barcodereader.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

class BarcodeTrackerFactory implements MultiProcessor.Factory<Barcode> {
    private GraphicOverlay<BarcodeGraphic> mGraphicOverlay;
    BarcodeCaptureActivity activity;

    BarcodeTrackerFactory(GraphicOverlay<BarcodeGraphic> barcodeGraphicOverlay,BarcodeCaptureActivity activity) {
        mGraphicOverlay = barcodeGraphicOverlay;
        this.activity=activity;
    }

    @Override
    public Tracker<Barcode> create(Barcode barcode) {
        BarcodeGraphic graphic = new BarcodeGraphic(mGraphicOverlay);
        return new BarcodeGraphicTracker(mGraphicOverlay, graphic,activity);
    }

}

