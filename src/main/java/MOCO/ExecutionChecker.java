package MOCO;

import IoTSystem.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Set;

public class ExecutionChecker {
    private static final Logger LOGGER = LogManager.getLogger();
    public static boolean preCheck(Message message, String currentState, Map<String, Set<String>> gateway_outEdgeApis,
                                   Map<String, Set<String>> light_outEdgeApis, Map<String, Set<String>> cm_outEdgeApis,
                                   Map<String, Set<String>> vc_outEdgeApis, Map<String, Set<String>> wm_outEdgeApis) {
        LOGGER.info("[*] PreCheck. Msg:" + message + " CurrentState:" + currentState);
        if (currentState.equals("Invalid")){
            return false;
        }
        String deviceType = message.getDeviceType();
        LOGGER.info(currentState);
        LOGGER.info(cm_outEdgeApis.keySet());
        switch (deviceType) {
            case "CoffeeMachine":
                return cm_outEdgeApis.get(currentState).contains(message.getDeviceAPI());
            case "Gateway":
                return gateway_outEdgeApis.get(currentState).contains(message.getDeviceAPI());
            case "Yeelight":
                return light_outEdgeApis.get(currentState).contains(message.getDeviceAPI());
            case "VideoCamera":
                return vc_outEdgeApis.get(currentState).contains(message.getDeviceAPI());
            case "WashingMachine":
                return wm_outEdgeApis.get(currentState).contains(message.getDeviceAPI());
            default:
                return false;
        }
    }
}
