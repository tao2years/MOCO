package IoTSystem;

import IoTSystem.DeviceController.*;
import IoTSystem.DeviceTwin.CMTwin;
import VirtualDevice.CoffeeMachine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TaskScheduler {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ExecutorService executor;
    private MessageQueue messageQueue;
    private CMController cmController;
    private GatewayController gatewayController;
    private LightController lightController;
    private VCController vcController;
    private WMController wmController;

    public TaskScheduler(int threadPoolSize, CMController cmController) {
        if (threadPoolSize == 0) {
            this.executor = Executors.newSingleThreadExecutor();
        }else{
            this.executor = Executors.newFixedThreadPool(threadPoolSize);
        }
        this.messageQueue = new MessageQueue();
        this.cmController = cmController;
    }

    public TaskScheduler(int threadPoolSize, CMController cmController, GatewayController gatewayController, LightController lightController, VCController vcController, WMController wmController) {
        this.executor = Executors.newFixedThreadPool(threadPoolSize);
        this.messageQueue = new MessageQueue();
        this.cmController = cmController;
        this.gatewayController = gatewayController;
        this.lightController = lightController;
        this.vcController = vcController;
        this.wmController = wmController;
    }

    public void addMessage(Message message) {
        messageQueue.addMessage(message);
    }

    // Start the task scheduler
    public void start() {
        executor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                // Take the next message from the queue
                Message message = messageQueue.getMessage();
                // Dispatch the message to the appropriate controller
                dispatchMessage(message);
            }
        });
    }

    // Method to dispatch message to the appropriate controller
    private void dispatchMessage(Message message) {
        try {
            String deviceType = message.getDeviceType();
            String api = message.getDeviceAPI();
            String[] args = message.getDeviceAPIArgs();
            // Dispatch the message to the appropriate controller
            if (args == null) {
                args = new String[0];
            }
            Class<?>[] argTypes = new Class[args.length];
            Object[] parsedArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                parsedArgs[i] = parseArg(args[i]);
                argTypes[i] = getPrimitiveType(parsedArgs[i].getClass());
            }
            Method method = cmController.getClass().getMethod(api, argTypes);
            method.invoke(cmController,  parsedArgs);
            LOGGER.info("[Waiting...]" + message);
            sleep(1000);
            cmController.printInternalState();
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Object parseArg(String arg) {
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            return arg;
        }
    }

    // Shutdown the task scheduler gracefully
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    System.err.println("Executor did not terminate");
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private Class<?> getPrimitiveType(Class<?> wrapper) {
        if (wrapper == Integer.class) {
            return int.class;
        }
        // 可以扩展其他类型转换
        return wrapper;
    }

    public static void main(String[] args) {
        CoffeeMachine coffeeMachine = new CoffeeMachine();
        CMTwin cmTwin = new CMTwin();
        CMController cmController = new CMController("CM001", coffeeMachine, cmTwin);
        TaskScheduler taskScheduler = new TaskScheduler(0, cmController);

        // Test adding and getting messages
        Message message1 = new Message("CoffeeMachine", "turnOn", new String[]{});
        Message message2 = new Message("CoffeeMachine", "addWater", new String[]{});
        Message message3 = new Message("CoffeeMachine", "addMilk", new String[]{});
        Message message4 = new Message("CoffeeMachine", "addCoffeeBean", new String[]{});
        Message message5 = new Message("CoffeeMachine", "placeCup", new String[]{});
        Message message6 = new Message("CoffeeMachine", "brewCoffee", new String[]{"1"});

        taskScheduler.addMessage(message1);
        taskScheduler.addMessage(message2);
        taskScheduler.addMessage(message3);
        taskScheduler.addMessage(message4);
        taskScheduler.addMessage(message5);
        taskScheduler.addMessage(message6);

        taskScheduler.start();
        sleep(1000);
        taskScheduler.shutdown();
    }

}
