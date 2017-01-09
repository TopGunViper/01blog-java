package edu.ouc.async.event;


public interface Listener<T> {

	/**
	 * success
	 * 
	 * @param event
	 */
	public void onSuccess(Event<T> event);
	
	/**
	 * timeout
	 * 
	 * @param event
	 * @param timeout
	 */
	public void onTimeout(Event<T> event, int timeout);
	
	/**
	 * 异常
	 * 
	 * @param event
	 * @param t
	 */
	void onException(Event<T> event, Throwable t);
	
	/**
	 * failure
	 * 
	 * @param event
	 * @param msg
	 */
	public void onFailure(Event<T> event, Object msg);
	
}
