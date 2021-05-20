/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.compare;

import java.util.Comparator;

import com.prey.actions.PreyAction;
import com.prey.actions.observer.ActionJob;

public class CompareAction implements Comparator<ActionJob> {

    public int compare(ActionJob o1, ActionJob o2) {
        if (o1 == null) {
            return 1;
        }
        if (o2 == null) {
            return -1;
        }
        PreyAction p1 = o1.getAction();
        PreyAction p2 = o2.getAction();
        if (p1 == null) {
            return 1;
        }
        if (p2 == null) {
            return -1;
        }
        if (p1.getPriority() > p2.getPriority()) {
            return 1;
        } else {
            return -1;
        }
    }

}