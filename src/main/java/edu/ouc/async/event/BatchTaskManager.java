package edu.ouc.async.event;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BatchTaskManager {

	private BatchTask task;

	private long timeout = -1;

	private TimeUnit unit;
	
	private Listener<BatchTask> listener;

	public BatchTaskManager task(BatchTask task){
		return task(task, -1, null);
	}
	
	public BatchTaskManager task(BatchTask task, long timeout, TimeUnit unit){
		this.task = task;
		this.timeout = timeout;
		this.unit = unit;
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
