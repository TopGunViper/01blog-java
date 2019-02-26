package edu.ouc.dp.async.event;

/**
 *	Event
 */
public interface Event<T> {

	public T getElement();
	
	public EventType getEventType();
	
}
