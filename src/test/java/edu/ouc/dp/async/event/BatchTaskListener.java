package edu.ouc.dp.async.event;


public class BatchTaskListener implements Listener<BatchTask> {
	
	private DummyWarningService warningService;
	
	public BatchTaskListener(DummyWarningService warningService){
		this.warningService = warningService;
	}
	@Override
	public void onSuccess(Event<BatchTask> event) {
		warningService.dummyWarning(event.getElement(),"Success");
	}

	@Override
	public void onException(Event<BatchTask> event, Throwable t) {
		warningService.dummyWarning(event.getElement(),t);
	}
}
