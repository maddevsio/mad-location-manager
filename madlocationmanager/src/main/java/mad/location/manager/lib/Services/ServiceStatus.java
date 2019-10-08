package mad.location.manager.lib.Services;

public enum ServiceStatus {
    PERMISSION_DENIED(0),
    SERVICE_STOPPED(1),
    SERVICE_STARTED(2),
    HAS_LOCATION(3),
    SERVICE_PAUSED(4);

    int value;

    ServiceStatus(int value) { this.value = value;}

    public int getValue() { return value; }
}