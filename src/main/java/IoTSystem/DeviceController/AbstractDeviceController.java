package IoTSystem.DeviceController;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Common base for the device controllers: holds the device id, the physical
 * device and its digital twin, and renders the internal-state comparison log
 * line shared by every controller.
 *
 * <p>Controllers are thin adapters: each API method invokes the device, and
 * only when the device reports {@code "success"} mirrors the change into the
 * twin. The twin synchronization itself stays in the concrete controllers,
 * because each device updates different twin fields.</p>
 *
 * @param <D> physical device type
 * @param <T> digital twin type
 */
public abstract class AbstractDeviceController<D, T> {
    private static final Logger LOGGER = LogManager.getLogger();

    /** Class-name prefix used by the physical device's toString(). */
    private final String deviceName;
    /** Class-name prefix used by the twin's toString(). */
    private final String twinName;

    protected String deviceId;
    protected D device;
    protected T twin;

    protected AbstractDeviceController(String deviceId, D device, T twin,
                                       Class<D> deviceType, Class<T> twinType) {
        this.deviceId = deviceId;
        this.device = device;
        this.twin = twin;
        this.deviceName = deviceType.getSimpleName();
        this.twinName = twinType.getSimpleName();
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public D getDevice() {
        return device;
    }

    public void setDevice(D device) {
        this.device = device;
    }

    public T getTwin() {
        return twin;
    }

    public void setTwin(T twin) {
        this.twin = twin;
    }

    /**
     * Logs device, twin and whether both representations currently agree.
     * Kept byte-compatible with the controllers' original printInternalState
     * output (same replace-based comparison, same log lines).
     */
    public void printInternalState() {
        LOGGER.info("Device ID: " + deviceId);
        String deviceString = device.toString();
        LOGGER.info(deviceName + ": " + deviceString);
        String twinString = twin.toString();
        LOGGER.info(twinName + ": " + twinString);
        boolean equal = deviceString.replace(deviceName, "")
                .equals(twinString.replace(twinName, ""));
        LOGGER.info("Equal: " + equal);
    }
}
