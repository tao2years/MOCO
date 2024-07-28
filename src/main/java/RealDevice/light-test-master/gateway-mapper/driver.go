package main

import (
	//"fmt"
	"os"
	"os/exec"
	"strings"

	"k8s.io/klog/v2"
)

// To Do
type Gateway struct {
	power      string
	brightness string
}

// To Do
func (gateway *Gateway) GetStates() map[string]string {

	properties := make(map[string]string)

	command1 := exec.Command("python", "getPower.py")
	output1, err := command1.Output()
	if err != nil {
		klog.Infoln(err)
	}
	properties["power"] = strings.TrimRight(string(output1), "\n")

	command2 := exec.Command("python", "getBrightness.py")
	output2, err := command2.Output()
	if err != nil {
		klog.Infoln(err)
	}
	properties["brightness"] = strings.TrimRight(string(output2), "\n")

	//return strings.TrimRight(string(output1), "\n"), strings.TrimRight(string(output2), "\n")
	return properties

}

func (gateway *Gateway) TurnOn() {

	command := exec.Command("python", "on.py")

	command.Stdout = os.Stdout
	command.Stderr = os.Stderr

	err := command.Run()
	if err != nil {
		klog.Infoln("Fail to execute script.\n")
		//return可能有问题
		return
	}

	gateway.power = "ON"

	klog.Infoln("Success to turn on the gateway.\n")
}

func (gateway *Gateway) TurnOff() {

	command := exec.Command("python", "off.py")

	command.Stdout = os.Stdout
	command.Stderr = os.Stderr

	err := command.Run()
	if err != nil {
		klog.Infoln("Fail to execute script.\n")
		//return可能有问题
		return
	}

	gateway.power = "OFF"

	klog.Infoln("Success to turn off the gateway.\n")
}

func (gateway *Gateway) SetBrightness(value string) {

	command := exec.Command("python", "setBrightness.py", value)

	command.Stdout = os.Stdout
	command.Stderr = os.Stderr

	err := command.Run()
	if err != nil {
		klog.Infoln("Fail to execute script.\n")
		//return可能有问题
		return
	}

	gateway.brightness = value

	klog.Infoln("Success to set brightness.\n")
}

// power和brightness用来记录上次云端修改的状态值，并不是设备的实际状态值
// 这是为了避免因为实际物理设备状态产生变化，仍然重复调用driver
func NewGateway() *Gateway {
	gateway := &Gateway{
		power:      "OFF",
		brightness: "50",
	}
	return gateway
}

//func CloseLight(gateway *Gateway) {
//	close(gateway.power)
//}
