package com.prey.actions.observer;

/**
 * Created by oso on 24-08-15.
 */

import java.util.Random;

import android.content.Context;

import com.prey.PreyLogger;
import com.prey.actions.PreyAction;
import com.prey.exceptions.PreyException;

/**
 * Class that represent the abstraction of a PreyAction execution. This class
 * implements <code>Runnable</code> so its started on its own Thread.
 *
 * @author Carlos
 *
 */
public class ActionJob implements Runnable {

    private long id;
    private long startedAt;
    private PreyAction action;
    private boolean finished = false;
    private ActionResult result;
    private boolean reportModuleJob;
    private JobsGroup jobsGroup;
    private boolean shouldStop;
    private Context ctx;

    /**
     * ActionJob constructor.
     *
     * @param actionToRun
     *            PreyAction this job will execute
     * @param jobsGroup
     *            Observer JobsGroup which this job belongs to.
     */
    public ActionJob(PreyAction actionToRun, JobsGroup jobsGroup, Context ctx) {
        this.action = actionToRun;
        this.id = this.hashCode();
        this.reportModuleJob = actionToRun.isSyncAction();
        this.jobsGroup = jobsGroup;
        this.shouldStop = false;
        this.ctx = ctx;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(long startedAt) {
        this.startedAt = startedAt;
    }

    public PreyAction getAction() {
        return action;
    }

    public void setAction(PreyAction action) {
        this.action = action;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void run() {
        try {
            this.startedAt = System.currentTimeMillis();
            this.action.execute(this, this.ctx);
        } catch (PreyException e) {
            PreyLogger.e("Error while running job [" + id + "] :" + e.getMessage(), e);
        }
    }

    public boolean isReportModuleJob() {
        return reportModuleJob;
    }

    public boolean isFinished() {
        return finished;
    }

    /**
     * This method should be calling by its corresponding Action when finish.
     * Doing that we inform the result to our JobsGroups observer and finish the
     * running Thread
     *
     * @param result
     */
    public void finish(ActionResult result) {
        this.result = result;
        this.finished = true;
        this.shouldStop = true;
        this.jobsGroup.jobFinished(this);
    }

    public ActionResult getResult() {
        return this.result;
    }

    private long getRandomId() {
        Random rnd = new Random();
        return rnd.nextLong();
    }

}

