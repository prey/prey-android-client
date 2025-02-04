/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.barcodereader

import com.google.android.gms.vision.Detector.Detections
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.barcode.Barcode
import com.prey.barcodereader.ui.camera.GraphicOverlay

internal class BarcodeGraphicTracker(
    private val mOverlay: GraphicOverlay<BarcodeGraphic>,
    private val mGraphic: BarcodeGraphic,
    private val activity: BarcodeCaptureActivity
) :
    Tracker<Barcode?>() {
    override fun onNewItem(id: Int, item: Barcode?) {
        mGraphic.setId(id)
    }

    override fun onUpdate(detectionResults: Detections<Barcode?>, item: Barcode?) {
        mOverlay.add(mGraphic)
       // mGraphic.updateItem(item)
        activity.updateBarcode(item)
    }

    override fun onMissing(detectionResults: Detections<Barcode?>) {
        mOverlay.remove(mGraphic)
    }

    override fun onDone() {
        mOverlay.remove(mGraphic)
    }
}
