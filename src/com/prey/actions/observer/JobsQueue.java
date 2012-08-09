/*******************************************************************************
 * Created by Carlos Yaconi.
 * Copyright 2011 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.observer;

import java.util.Enumeration;
import java.util.Hashtable;

import android.content.Context;

public class JobsQueue {

	private Hashtable<Long,JobsGroup> jobs;
	private ActionsController controller;

	/**
	 * Public constructor that establishes a link to the ActionController that it
	 * belongs to.
	 * 
	 * @param controller
	 */
	public JobsQueue(ActionsController controller) {
		this.jobs = new Hashtable<Long,JobsGroup>();
		this.controller = controller;
	}

	/**
	 * Adds a JobsGroup to the queue, and starts immediately to execute jobs on
	 * that group. If there was a previously group running, this method finishes that execution first.
	 * 
	 * @param jobsGroup
	 *            group to add to the queue
	 */
	public void addAndRunJobGroup(JobsGroup jobsGroup, Context ctx, boolean isMissing) {
		this.finishRunningJobs(ctx);
		this.jobs.put(Long.valueOf(jobsGroup.getId()), jobsGroup);
		jobsGroup.run(this, isMissing);
	}

	/**
	 * This method will finish all running jobs discarding its results. Results
	 * from already finished jobs are informed to the ActionsController
	 * observer.
	 */
	protected void finishRunningJobs(Context ctx) {
		Enumeration<Long> jobsRunningId = this.jobs.keys();
		Long jobId;
		while (jobsRunningId.hasMoreElements()) {
			jobId = (Long) jobsRunningId.nextElement();
			JobsGroup jobGroup = (JobsGroup) this.jobs.get(jobId);
			this.controller.jobGroupFinished(jobGroup.getResults(), ctx);
			jobGroup.destroy();
			this.jobs.remove(jobId);
		}

	}

	/**
	 * This method is a callback from JobsGroup, used to notice JobsQueue that a
	 * group has finished to run all its actions. Once the callback is received,
	 * we call-back the ActionController giving it the actions' results.
	 * 
	 * @param jobsGroup
	 *            jobsGroup who finished to run all jobs.
	 * @param ctx
	 *            Application context
	 */
	public void groupFinished(JobsGroup jobsGroup, Context ctx) {
		this.jobs.remove(Long.valueOf(jobsGroup.getId()));
		this.controller.jobGroupFinished(jobsGroup.getResults(), ctx);
	}

}
