package edu.ouc.dp.async.event;

public class BatchTaskEvent implements Event<BatchTask> {

	private BatchTask task;
	
	private EventType type;
	
	public BatchTaskEvent(BatchTask task, EventType type){
		this.task = task;
		this.type = type;
	}
	@Override
	public BatchTask getElement() {
		return task;
	}
	@Override
	public EventType getEventType() {
		return type;
	}
}
