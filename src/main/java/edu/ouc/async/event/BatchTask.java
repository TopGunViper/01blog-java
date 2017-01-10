package edu.ouc.async.event;

import java.util.concurrent.TimeoutException;

/**
 * BatchTask
 * 
 * @author wqx
 *
 */
public abstract class BatchTask {
	
	public String taskName;
	
	public BatchTask(String taskName){
		this.taskName = taskName;
	}
	public String getTaskName(){
		return taskName;
	}
	
	abstract void process() throws TimeoutException;
}
