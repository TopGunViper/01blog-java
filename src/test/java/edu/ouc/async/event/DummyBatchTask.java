package edu.ouc.async.event;

public class DummyBatchTask extends BatchTask {

	/*
	 * ≤‚ ‘¿‡–Õ
	 */
	private int type;

	public DummyBatchTask(String taskName, int type){
		super(taskName);
		this.type = type;
	}

	@Override
	void process() {
		System.out.println(taskName + " begin....");

		switch(type){
		case 1:
			//process normally
			break;
		case 2:
			throw new NullPointerException();
		case 3:
			 try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {}
			break;
		}
		
		System.out.println(taskName + " completed....");
	}
}
