package edu.ouc.dp.async;

/**
 * Packet对象：封装request和response对象
 * 
 * @author wqx
 *
 */
public class Packet{
	
	private Object request;
	
	private CallBack cb;
	
	private Object ctx;

	private Object response;
	
	private int errorCode;
	
	public CallBack getCb() {
		return cb;
	}

	public Object getCtx() {
		return ctx;
	}

	public void setCb(CallBack cb) {
		this.cb = cb;
	}

	public void setCtx(Object ctx) {
		this.ctx = ctx;
	}

	public Object getRequest() {
		return request;
	}

	public Object getResponse() {
		return response;
	}

	public void setRequest(Object request) {
		this.request = request;
	}

	public void setResponse(Object response) {
		this.response = response;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
}
