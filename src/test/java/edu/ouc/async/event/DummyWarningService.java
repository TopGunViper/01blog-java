package edu.ouc.async.event;

public class DummyWarningService {
	
	String taskName;
	Object warningMsg;
	
	public void dummyWarning(BatchTask task, Object msg){
		taskName = task.getTaskName();
		warningMsg = msg;
		/**
		 * send email or messages.
		 */
	}
}
