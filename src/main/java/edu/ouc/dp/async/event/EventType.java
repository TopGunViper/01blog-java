package edu.ouc.dp.async.event;


/**
 * Event type
 * 
 * @author wqx
 *
 */
public enum EventType {
	NULL(0,""),
	SUCCESS(1,"process success"),
	TIMEOUT(2,"task process timeout"),
	ERROR(3, "Internal error");
	
	EventType(int value, String desc){
		this.value = value;
		this.desc = desc;
	}
	
	private final int value;
	
	private final String desc;

	public int getValue() {
		return value;
	}
	public String getDesc() {
		return desc;
	}

    public EventType genEnumByValue(int intValue) {
        for (EventType item: EventType.values()) {
            if (item.value == intValue)
                return item;
        }
        return NULL;
    }
	
}
