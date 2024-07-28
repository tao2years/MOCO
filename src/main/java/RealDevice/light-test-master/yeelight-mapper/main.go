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
	//"github.com/kubeedge/kubeedge/cloud/pkg/devicecontroller/types"
	//"github.com/kubeedge/kubeedge/edge/pkg/devicetwin/dttype"
)

var cli mqtt.Client

// createActualUpdateMessage function is used to create the device twin update message
func createActualUpdateMessage(properties map[string]string) DeviceTwinUpdate {

	var deviceTwinUpdateMessage DeviceTwinUpdate

	actualMap := make(map[string]*MsgTwin)
	//actualMap2 := make(map[string]*MsgTwin)

	for key, value := range properties {
		//必须加一个临时变量，不然会有bug
		temp := value
		actualMap[key] = &MsgTwin{Actual: &TwinValue{Value: &temp}, Metadata: &TypeMetadata{Type: "Updated"}}
	}

	/*
		power := properties["power"]
		brightness := properties["brightness"]
		red := properties["red"]
		green := properties["green"]
		blue := properties["blue"]

		actualMap["power"] = &MsgTwin{Actual: &TwinValue{Value: &power}, Metadata: &TypeMetadata{Type: "Updated"}}
		actualMap["brightness"] = &MsgTwin{Actual: &TwinValue{Value: &brightness}, Metadata: &TypeMetadata{Type: "Updated"}}
		actualMap["red"] = &MsgTwin{Actual: &TwinValue{Value: &red}, Metadata: &TypeMetadata{Type: "Updated"}}
		actualMap["green"] = &MsgTwin{Actual: &TwinValue{Value: &green}, Metadata: &TypeMetadata{Type: "Updated"}}
		actualMap["blue"] = &MsgTwin{Actual: &TwinValue{Value: &blue}, Metadata: &TypeMetadata{Type: "Updated"}}


		klog.Infoln("1st actualMap:")
		for key, value := range actualMap {
			klog.Infoln(key, value)
		}
	*/

	deviceTwinUpdateMessage.Twin = actualMap
	return deviceTwinUpdateMessage
}

func (light *Light) publishToMqtt(properties map[string]string) {
	updateMessage := createActualUpdateMessage(properties)
	twinUpdateBody, _ := json.Marshal(updateMessage)

	token := cli.Publish(topic, 0, false, twinUpdateBody)

	if token.Wait() && token.Error() != nil {
		klog.Infoln(token.Error())
	}
}

func (light *Light) subcribeFromMqtt(client mqtt.Client, msg mqtt.Message) {

	Update := &DeviceTwinDelta{}
	err := json.Unmarshal(msg.Payload(), Update)
	if err != nil {
		klog.Infoln("Unmarshal error: %v\n", err)
	}

	//To Do
	cmd := *Update.Twin["power"].Expected.Value
	cmd2 := *Update.Twin["brightness"].Expected.Value
	cmd3 := *Update.Twin["red"].Expected.Value
	cmd4 := *Update.Twin["green"].Expected.Value
	cmd5 := *Update.Twin["blue"].Expected.Value

	klog.Infoln("New cmd:", cmd, cmd2, cmd3, cmd4, cmd5)
	klog.Infoln("light object:", light.power, light.brightness, light.red, light.green, light.blue)

	if cmd == "ON" && cmd != light.power {
		light.TurnOn()
		klog.Infoln("turn on yeelight.\n")
	}

	if cmd == "OFF" && cmd != light.power {
		light.TurnOff()
		klog.Infoln("turn off yeelight.\n")
	}

	if light.power == "ON" && cmd2 != light.brightness {
		light.SetBrightness(cmd2)
		klog.Infoln("Set brightness to " + cmd2 + ".\n")
	}

	if light.power == "ON" && (cmd3 != light.red || cmd4 != light.green || cmd5 != light.blue) {
		light.SetRGB(cmd3, cmd4, cmd5)
		klog.Infoln("Set RGB to " + cmd3 + " " + cmd4 + " " + cmd5 + ".\n")
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

	light := NewLight()
	klog.Infoln("New yeelight establish.\n")

	//开始监控状态更新
	go light.initialize()

	light.run()

	//mapper的生命周期结束
	select {
	case <-stopchan:
		cli.Disconnect(250)
		klog.Infoln("Interrupt, exit.\n")
		break
	}
}
