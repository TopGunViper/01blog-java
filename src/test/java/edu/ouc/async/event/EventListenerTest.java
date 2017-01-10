package edu.ouc.async.event;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ouc.base.BaseTest;

public class EventListenerTest extends BaseTest{
	private static final Logger LOG = LoggerFactory.getLogger(EventListenerTest.class);

	@BeforeClass
	public static void setUp() throws Exception {
	}
	@Test
	public void test() {
		LOG.info("Sucessful configuration");
	}

	@Test
	public void testTaskSuccess() {

		DummyWarningService warningService = new DummyWarningService();
		BatchTaskListener listener = new BatchTaskListener(warningService);
		
		DummyBatchTask task = new DummyBatchTask("DummyTask-01",1);
		BatchTaskManager manager = new BatchTaskManager();
		
		manager.task(task)
         	   .listener(listener)
		 	   .process();
		
		Assert.assertEquals("DummyTask-01", warningService.taskName);
		Assert.assertEquals("Success", warningService.warningMsg);
	}

	@Test
	public void testTaskException() {

		DummyWarningService warningService = new DummyWarningService();
		
		BatchTaskListener listener = new BatchTaskListener(warningService);
		
		DummyBatchTask task = new DummyBatchTask("DummyTask-02",2);
		
		BatchTaskManager manager = new BatchTaskManager();
		
		manager.task(task)
         	   .listener(listener)
		 	   .process();
		
		Assert.assertEquals("DummyTask-02", warningService.taskName);
		Assert.assertEquals(NullPointerException.class, warningService.warningMsg.getClass());
	}
	@AfterClass
	public static void tearDown() throws Exception {
	}
}
