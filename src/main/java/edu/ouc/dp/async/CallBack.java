package edu.ouc.dp.async;

/**
 * 模拟异步回调接口
 * 
 * @author wqx
 *
 */
public interface CallBack{
	
	/**
	 * 回调函数
	 * 
	 * @param rc: result code 
	 * @param response
	 * @param ctx : context
	 */
	void process(int rc, Object response,Object ctx);
}
