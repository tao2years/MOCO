# MOCO
Match Word with Deed: Maintaining Consistency for IoT Systems with Behavior Models

This repository is dedicated to sharing the tool MOCO and the data for the ASE 2024 paper entitled Match Word with Deed: Maintaining Consistency for IoT Systems with Behavior Models.

## METHODOLOGY

MOCO is a platform-independent tool to maintain the IoT System's runtime consistency.


1. First, MOCO learns device behavior models based
   on the Active Automata Learning (AAL) technique.

2. At runtime, MOCO monitors system operations and checks the feasibility of executing a command in the current state based on the previously learned behavior model.

3. After executing the command, MOCO checks both the post-execution states of the physical device and digital space against the theoretical
   states specified by the behavior model.

4. Upon detecting inconsistencies, MOCO employs appropriate actions to resolve the issue.


## Implementation

The implementation of MOCO involves several key components.


## System Architecture

\toolname lies between the application and the IoT system, providing a runtime consistency assurance framework for IoT systems. The following are the key components of \toolname:

### Learner

The **Learner** interacts with IoT devices to generate precise behavior models, which are essential for understanding how devices should function under various conditions. These models form the basis for detecting and resolving inconsistencies.

### Concurrent Handler

The **Concurrent Handler** manages concurrent requests from applications by sequencing these requests and forwarding them to the **Pre-execution Checker**.

### Pre-execution Checker

The **Pre-execution Checker** performs a pre-check to verify the legality of the current instruction.

### Execution Monitor

The **Execution Monitor** ensures the execution of the request, with some exception-handling mechanisms.

### Post-execution Checker

The **Post-execution Checker** performs a post-check to identify any inconsistencies. If an inconsistency is detected, it creates a resolution, which is then communicated to the **IoT System**. This ensures that any detected deviations are addressed promptly to maintain system consistency.

## IoT System Components

The **IoT System** itself comprises several modules including the **Task Scheduler**, **Device Controller**, and **Device Twin** components.

### Task Scheduler

The **Task Scheduler** is responsible for receiving commands from \toolname and forwarding them to the corresponding device controllers.

### Device Controller

The **Device Controller** interacts directly with physical IoT devices such as Yeelight and Gateway, updating their digital states accordingly.

### Device Twin

The **Device Twin** maintains a digital representation of each device, enabling virtual interaction and monitoring.



## Getting MOCO to run

Ensure you have the necessary dependencies configured as per the `pom.xml` file. You can use Maven to manage dependencies.


## Running MOCO

To run MOCO, execute the Main.java file. Configuration for messaging can be set in the sendMessage.

To enable or disable MOCO, you can set the proxy on or off in the Main function:

```java
messageProxy.setProxyOn(true); // Enable MOCO
messageProxy.setProxyOn(false); // Disable MOCO
```

In the TaskScheduler class, you can comment invoke methods to enable or disable the postCheck in the ExecutionChecker:

```java
// ExecutionChecker.postCheck(...); 
ExecutionChecker.postCheck(...); 
```

