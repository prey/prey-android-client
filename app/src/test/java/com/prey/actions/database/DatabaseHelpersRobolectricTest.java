/*******************************************************************************
 * Created by Prey
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;

import com.prey.actions.fileretrieval.FileretrievalDto;
import com.prey.actions.fileretrieval.FileretrievalOpenHelper;
import com.prey.actions.triggers.TriggerDto;
import com.prey.actions.triggers.TriggerOpenHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertFalse;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 30)
public class DatabaseHelpersRobolectricTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void givenFileRetrievalReadQuery_whenFinished_thenReadableDatabaseIsClosed() {
        RecordingFileretrievalOpenHelper helper = new RecordingFileretrievalOpenHelper(context);
        FileretrievalDto dto = new FileretrievalDto();
        dto.setFileId("file-id");
        dto.setPath("/tmp/file");
        dto.setSize(10L);
        dto.setStatus(1);
        helper.insertFileretrieval(dto);

        helper.getFileretrieval("file-id");

        assertFalse(helper.lastReadableDatabase.isOpen());
    }

    @Test
    public void givenTriggerReadQuery_whenFinished_thenReadableDatabaseIsClosed() {
        RecordingTriggerOpenHelper helper = new RecordingTriggerOpenHelper(context);
        TriggerDto dto = new TriggerDto();
        dto.setId(1);
        dto.setName("wifi");
        dto.setEvents("[]");
        dto.setActions("[]");
        helper.insertTrigger(dto);

        helper.getTrigger("1");

        assertFalse(helper.lastReadableDatabase.isOpen());
    }

    private static final class RecordingFileretrievalOpenHelper extends FileretrievalOpenHelper {

        private SQLiteDatabase lastReadableDatabase;

        private RecordingFileretrievalOpenHelper(Context context) {
            super(context);
        }

        @Override
        public SQLiteDatabase getReadableDatabase() {
            lastReadableDatabase = super.getReadableDatabase();
            return lastReadableDatabase;
        }
    }

    private static final class RecordingTriggerOpenHelper extends TriggerOpenHelper {

        private SQLiteDatabase lastReadableDatabase;

        private RecordingTriggerOpenHelper(Context context) {
            super(context);
        }

        @Override
        public SQLiteDatabase getReadableDatabase() {
            lastReadableDatabase = super.getReadableDatabase();
            return lastReadableDatabase;
        }
    }
}
