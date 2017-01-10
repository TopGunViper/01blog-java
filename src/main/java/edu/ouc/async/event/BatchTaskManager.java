package edu.ouc.async.event;

import java.util.concurrent.TimeUnit;

public class BatchTaskManager {

	private BatchTask task;

	private Listener<BatchTask> listener;

	public BatchTaskManager task(BatchTask task){
		this.task = task;
		return this;
	}
	
	public BatchTaskManager listener(Listener<BatchTask> listener){
		this.listener = listener;
		return this;
	}
	public void process(){
		if(task == null)
			throw new IllegalArgumentException("Task is null");
		boolean success = false;
		try{
			task.process();
			success = true;
		}catch(Throwable t){
			listener.onException(new BatchTaskEvent(task,EventType.ERROR), t);
		}
		if(success){
			listener.onSuccess(new BatchTaskEvent(task,EventType.SUCCESS));
		}
	}
}
