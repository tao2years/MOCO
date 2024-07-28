package main

import (
	"encoding/json"
	//"fmt"
	"os"
	"os/signal"

	//"strconv"
	"syscall"

	"k8s.io/klog/v2"

	mqtt "github.com/eclipse/paho.mqtt.golang"
)

var cli mqtt.Client

// createActualUpdateMessage function is used to create the device twin update message
func createActualUpdateMessage(properties map[string]string) DeviceTwinUpdate {

	var deviceTwinUpdateMessage DeviceTwinUpdate

	actualMap := make(map[string]*MsgTwin)

	for key, value := range properties {
		//必须加一个临时变量，不然会有bug
		temp := value
		actualMap[key] = &MsgTwin{Actual: &TwinValue{Value: &temp}, Metadata: &TypeMetadata{Type: "Updated"}}
	}

	deviceTwinUpdateMessage.Twin = actualMap
	return deviceTwinUpdateMessage
}

func (camera *ChuangmiCamera) publishToMqtt(properties map[string]string) {
	updateMessage := createActualUpdateMessage(properties)
	twinUpdateBody, _ := json.Marshal(updateMessage)

	token := cli.Publish(topic, 0, false, twinUpdateBody)

	if token.Wait() && token.Error() != nil {
		klog.Infoln(token.Error())
	}
}

func (camera *ChuangmiCamera) subcribeFromMqtt(client mqtt.Client, msg mqtt.Message) {

	Update := &DeviceTwinDelta{}
	err := json.Unmarshal(msg.Payload(), Update)
	if err != nil {
		klog.Infoln("Unmarshal error: %v\n", err)
	}

	//To Do
	new_power := *Update.Twin["power"].Expected.Value
	//klog.Infoln("New cmd:", new_power)
	//klog.Infoln("camera object:", camera.power)
	if new_power != camera.power {
		camera.SetPower(new_power)
		klog.Infoln("SetPower camera.\n")
	}
	new_flip := *Update.Twin["flip"].Expected.Value
	if new_flip != camera.flip {
		camera.SetFlip(new_flip)
		klog.Infoln("SetFlip camera.\n")
	}
	new_light := *Update.Twin["light"].Expected.Value
	if new_light != camera.light {
		camera.SetLight(new_light)
		klog.Infoln("SetLight camera.\n")
	}
	new_nightmode := *Update.Twin["nightmode"].Expected.Value
	if new_nightmode != camera.nightmode {
		camera.SetNightmode(new_nightmode)
		klog.Infoln("SetNightmode camera.\n")
	}
	new_watermark := *Update.Twin["watermark"].Expected.Value
	if new_watermark != camera.watermark {
		camera.SetWatermark(new_watermark)
		klog.Infoln("SetWatermark camera.\n")
	}

}

func connectToMqtt() mqtt.Client {
	opts := mqtt.NewClientOptions()
	opts.AddBroker(mqttUrl)

	cli = mqtt.NewClient(opts)

	token := cli.Connect()
	if token.Wait() && token.Error() != nil {
		klog.Infoln(token.Error())
	}

	return cli
}

func main() {
	stopchan := make(chan os.Signal)
	signal.Notify(stopchan, syscall.SIGINT, syscall.SIGKILL)
	//在函数结束时关闭 stopchan 通道，以确保程序正常退出时通道被关闭。
	defer close(stopchan)

	cli = connectToMqtt()

	camera := NewCamera()
	klog.Infoln("New camera establish.\n")

	//开始监控状态更新
	go camera.initialize()

	camera.run()

	//mapper的生命周期结束
	select {
	case <-stopchan:
		cli.Disconnect(250)
		klog.Infoln("Interrupt, exit.\n")
		break
	}
}
