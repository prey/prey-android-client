/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2012 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.observer;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;

import android.content.Context;

import com.prey.actions.PreyAction;

/**
 * This class is a representation of a related actions needed to be run - and
 * sent as report - as a whole.
 * 
 * @author Carlos
 * 
 */
public class JobsGroup {

	private Hashtable<Long,ActionJob> actionModules;
	private Hashtable<Long,ActionJob> reportModules;
	private long id;
	private long creationTime;
	private boolean running;
	private boolean finished;
	private ArrayList<ActionResult> syncResults;
	// Only gets track of report modules jobs, because them could take a while to finish,
	// and maybe we need to kill them before.
	private Hashtable<Long,Thread> runningJobs;
	private JobsQueue queue;
	private Context ctx;

	// public JobsGroup() {
	// this.initialize();
	// }

	public JobsGroup(ArrayList<PreyAction> actions, Context ctx) {
		this.initialize(ctx);
		for (PreyAction preyAction : actions) {
			this.addJobToGroup(new ActionJob(preyAction, this, ctx));
		}
	}

	private void initialize(Context ctx) {
		Random rnd = new Random();
		this.id = rnd.nextLong();
		this.ctx = ctx;
		this.actionModules = new Hashtable<Long,ActionJob>();
		this.reportModules = new Hashtable<Long,ActionJob>();
		this.creationTime = System.currentTimeMillis();
		this.syncResults = new ArrayList<ActionResult>();
		this.runningJobs = new Hashtable<Long,Thread>();
	}

	public void addJobToGroup(ActionJob job) {
		if (job.isReportModuleJob())
			this.reportModules.put(Long.valueOf(job.getId()), job);
		else
			this.actionModules.put(Long.valueOf(job.getId()), job);
	}

	public void run(JobsQueue queue, boolean isMissing) {
		this.queue = queue;
		Enumeration<Long> actionModulesJobId = this.actionModules.keys();
		Long jobId;
		while (actionModulesJobId.hasMoreElements()) {
			jobId = (Long) actionModulesJobId.nextElement();
			new Thread((ActionJob) this.actionModules.get(jobId)).start();
		}
		if (isMissing){
			Enumeration<Long> reportModulesJobId = this.reportModules.keys();
			Long syncJobId;
			while (reportModulesJobId.hasMoreElements()) {
				syncJobId = (Long) reportModulesJobId.nextElement();
					Thread actionJobRun = new Thread((ActionJob) this.reportModules.get(syncJobId), syncJobId.toString());
				actionJobRun.start();
				// Only syncjobs are added to the running jobs list
				this.runningJobs.put(syncJobId, actionJobRun);
			}
		}
	}

	public void jobFinished(ActionJob job) {
		this.syncResults.add(job.getResult());
		this.runningJobs.remove(Long.valueOf(job.getId()));
		if (this.runningJobs.isEmpty())
			this.queue.groupFinished(this, this.ctx);
	}
	
	public boolean hasReportModules(){
		return !this.reportModules.isEmpty();
	}

	public long getId() {
		return id;
	}

	public long getCreationTime() {
		return creationTime;
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isFinished() {
		return finished;
	}

	public ArrayList<ActionResult> getResults() {
		return this.syncResults;
	}

	public void destroy() {
		Enumeration<Long> runningJobsId = this.runningJobs.keys();
		Long jobId;
		while (runningJobsId.hasMoreElements()) {
			jobId = (Long) runningJobsId.nextElement();
			Thread runningJob = (Thread) this.runningJobs.get(jobId);
			if (runningJob.isAlive())
				runningJob.interrupt();
		}
	}

}
