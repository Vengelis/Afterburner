package fr.vengelis.afterburner.configurations;

public enum ConfigBroadcaster {

    API_HOST(null),
    API_PORT(null),
    ;

    private Object data;

    ConfigBroadcaster(Object data) {
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

}
