package edu.ouc.dp.async.event;

/**
 * Listener接口
 * 
 * @author wqx
 *
 * @param <T>
 */
public interface Listener<T> {

	/**
	 * success
	 * 
	 * @param event
	 */
	public void onSuccess(Event<T> event);

	/**
	 * failure
	 * 
	 * @param event
	 * @param t
	 */
	void onException(Event<T> event, Throwable t);
	
}
