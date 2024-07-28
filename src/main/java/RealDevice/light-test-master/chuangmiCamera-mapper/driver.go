package main

import (
	//"fmt"
	"os"
	"os/exec"
	"strings"

	"k8s.io/klog/v2"
)

// To Do
type ChuangmiCamera struct {
	power     string
	flip      string
	light     string
	nightmode string
	watermark string
}

// To Do
func (camera *ChuangmiCamera) GetStates() map[string]string {

	properties := make(map[string]string)

	command1 := exec.Command("python", "get.py", "power")
	output1, err := command1.Output()
	if err != nil {
		klog.Infoln(err)
	}
	properties["power"] = strings.TrimRight(string(output1), "\n")

	//return strings.TrimRight(string(output1), "\n"), strings.TrimRight(string(output2), "\n")

	command2 := exec.Command("python", "get.py", "flip")
	output2, err := command2.Output()
	if err != nil {
		klog.Infoln(err)
	}
	properties["flip"] = strings.TrimRight(string(output2), "\n")

	command3 := exec.Command("python", "get.py", "light")
	output3, err := command3.Output()
	if err != nil {
		klog.Infoln(err)
	}
	properties["light"] = strings.TrimRight(string(output3), "\n")

	command4 := exec.Command("python", "get.py", "nightmode")
	output4, err := command4.Output()
	if err != nil {
		klog.Infoln(err)
	}
	properties["nightmode"] = strings.TrimRight(string(output4), "\n")

	command5 := exec.Command("python", "get.py", "watermark")
	output5, err := command5.Output()
	if err != nil {
		klog.Infoln(err)
	}
	properties["watermark"] = strings.TrimRight(string(output5), "\n")

	return properties

}

func (camera *ChuangmiCamera) SetPower(value string) {

	command := exec.Command("python", "set.py", "power", value)

	command.Stdout = os.Stdout
	command.Stderr = os.Stderr

	err := command.Run()
	if err != nil {
		klog.Infoln("Fail to execute script.\n")
		//return可能有问题
		return
	}

	camera.power = value

	klog.Infoln("Success to SetPower.\n")
}

func (camera *ChuangmiCamera) SetFlip(value string) {

	command := exec.Command("python", "set.py", "flip", value)

	command.Stdout = os.Stdout
	command.Stderr = os.Stderr

	err := command.Run()
	if err != nil {
		klog.Infoln("Fail to execute script.\n")
		//return可能有问题
		return
	}

	camera.flip = value

	klog.Infoln("Success to SetFlip.\n")
}

func (camera *ChuangmiCamera) SetLight(value string) {

	command := exec.Command("python", "set.py", "light", value)

	command.Stdout = os.Stdout
	command.Stderr = os.Stderr

	err := command.Run()
	if err != nil {
		klog.Infoln("Fail to execute script.\n")
		//return可能有问题
		return
	}

	camera.light = value

	klog.Infoln("Success to SetLight.\n")
}

func (camera *ChuangmiCamera) SetNightmode(value string) {

	command := exec.Command("python", "set.py", "nightmode", value)

	command.Stdout = os.Stdout
	command.Stderr = os.Stderr

	err := command.Run()
	if err != nil {
		klog.Infoln("Fail to execute script.\n")
		//return可能有问题
		return
	}

	camera.nightmode = value

	klog.Infoln("Success to SetNightmode.\n")
}

func (camera *ChuangmiCamera) SetWatermark(value string) {

	command := exec.Command("python", "set.py", "watermark", value)

	command.Stdout = os.Stdout
	command.Stderr = os.Stderr

	err := command.Run()
	if err != nil {
		klog.Infoln("Fail to execute script.\n")
		//return可能有问题
		return
	}

	camera.watermark = value

	klog.Infoln("Success to SetWatermark.\n")
}

// power和brightness用来记录上次云端修改的状态值，并不是设备的实际状态值
// 这是为了避免因为实际物理设备状态产生变化，仍然重复调用driver
func NewCamera() *ChuangmiCamera {
	camera := &ChuangmiCamera{
		power:     "OFF",
		flip:      "OFF",
		light:     "OFF",
		nightmode: "0",
		watermark: "OFF",
	}
	return camera
}

//func CloseLight(gateway *Gateway) {
//	close(gateway.power)
//}
