package IoTSystem.DeviceController;

import IoTSystem.DeviceTwin.VCTwin;
import VirtualDevice.VideoCamera;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VCController extends AbstractDeviceController<VideoCamera, VCTwin> {
    private static final Logger LOGGER = LogManager.getLogger();

    public VCController(String deviceId, VideoCamera vc, VCTwin vcTwin) {
        super(deviceId, vc, vcTwin, VideoCamera.class, VCTwin.class);
    }

    public VideoCamera getVc() {
        return getDevice();
    }

    public void setVc(VideoCamera vc) {
        setDevice(vc);
    }

    public VCTwin getVcTwin() {
        return getTwin();
    }

    public void setVcTwin(VCTwin vcTwin) {
        setTwin(vcTwin);
    }

    public void turnOn() {
        if (getVc().turnOn().equals("success"))
            getVcTwin().setPowerOn(true);
    }

    public void turnOff() {
        if (getVc().turnOff().equals("success"))
            getVcTwin().setPowerOn(false);
    }

    public void turnOnMotionRecord() {
        if (getVc().turnOnMotionRecord().equals("success"))
            getVcTwin().setMotionRecord(true);
    }

    public void turnOffMotionRecord() {
        if (getVc().turnOffMotionRecord().equals("success"))
            getVcTwin().setMotionRecord(false);
    }

    public void turnOnLight() {
        if (getVc().turnOnLight().equals("success"))
            getVcTwin().setLight(true);
    }

    public void turnOffLight() {
        if (getVc().turnOffLight().equals("success"))
            getVcTwin().setLight(false);
    }

    public void turnOnFullColor() {
        if (getVc().turnOnFullColor().equals("success"))
            getVcTwin().setFullColor(true);
    }

    public void turnOffFullColor() {
        if (getVc().turnOffFullColor().equals("success"))
            getVcTwin().setFullColor(false);
    }

    public void turnOnFlip() {
        if (getVc().turnOnFlip().equals("success"))
            getVcTwin().setFlip(true);
    }

    public void turnOffFlip() {
        if (getVc().turnOffFlip().equals("success"))
            getVcTwin().setFlip(false);
    }

    public void turnOnImproveProgram() {
        if (getVc().turnOnImproveProgram().equals("success"))
            getVcTwin().setImproveProgram(true);
    }

    public void turnOffImproveProgram() {
        if (getVc().turnOffImproveProgram().equals("success"))
            getVcTwin().setImproveProgram(false);
    }

    public void turnOnWdr() {
        if (getVc().turnOnWdr().equals("success"))
            getVcTwin().setWdr(true);
    }

    public void turnOffWdr() {
        if (getVc().turnOffWdr().equals("success"))
            getVcTwin().setWdr(false);
    }

    public void turnOnTrack() {
        if (getVc().turnOnTrack().equals("success"))
            getVcTwin().setTrack(true);
    }

    public void turnOffTrack() {
        if (getVc().turnOffTrack().equals("success"))
            getVcTwin().setTrack(false);
    }

    public void turnOffWatermark() {
        if (getVc().turnOffWatermark().equals("success"))
            getVcTwin().setWatermark(false);
    }

    public void setMaxClient(int maxClient) {
        if (getVc().setMaxClient(maxClient).equals("success"))
            getVcTwin().setMaxClient(maxClient);
    }

    public void setNightMode(int nightMode) {
        if (getVc().setNightMode(nightMode).equals("success"))
            getVcTwin().setNightMode(nightMode);
    }

    public void setMiniLevel(int miniLevel) {
        if (getVc().setMiniLevel(miniLevel).equals("success"))
            getVcTwin().setMiniLevel(miniLevel);
    }

    public static void main(String[] args) {
        VCController vcController =
                new VCController("vc001", new VideoCamera(), new VCTwin());
        vcController.turnOn();
        vcController.turnOnMotionRecord();
        vcController.turnOnLight();
        vcController.turnOnFullColor();
        vcController.turnOnFlip();
        vcController.turnOnImproveProgram();
        vcController.turnOnWdr();
        vcController.turnOnTrack();
        vcController.turnOffWatermark();
        vcController.setMaxClient(10);
        vcController.setNightMode(1);
        vcController.setMiniLevel(2);
        vcController.printInternalState();
    }
}
