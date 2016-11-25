package warrocker.musicbox.device;


final class Device {
    private String deviceName;
    private String ipAddress;

    Device(String deviceName, String ipAddress) {
        this.deviceName = deviceName;
        this.ipAddress = ipAddress;
    }

    String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
