/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.net.http.kotlin

import java.io.InputStream

class EntityFile {
    var idFile: String? = null
    var type: String? = null
    var name: String? = null
    var mimeType: String? = null
    var file: InputStream? = null
    var length: Int = 0
    /**
     * Method return the file name
     * @return file name
     */
    /**
     * Method update the file name
     * @param fileName
     */
    var filename: String? = null
}