package main

import (
	//"fmt"
	"os"
	"os/exec"
	"strings"

	"k8s.io/klog/v2"
)

// To Do
type Light struct {
	power      string
	brightness string
	red        string
	green      string
	blue       string
}

// To Do
func (light *Light) GetStates() map[string]string {

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

	command3 := exec.Command("python", "getRGB.py")
	output3, err := command3.Output()
	if err != nil {
		klog.Infoln(err)
	}
	rgb := strings.Split(strings.TrimRight(string(output3), "\n"), " ")
	properties["red"] = rgb[0]
	properties["green"] = rgb[1]
	properties["blue"] = rgb[2]

	//return strings.TrimRight(string(output1), "\n"), strings.TrimRight(string(output2), "\n")
	return properties

}

func (light *Light) TurnOn() {

	command := exec.Command("python", "on.py")

	command.Stdout = os.Stdout
	command.Stderr = os.Stderr

	err := command.Run()
	if err != nil {
		klog.Infoln("Fail to execute script.\n")
		//return可能有问题
		return
	}

	light.power = "ON"

	klog.Infoln("Success to turn on the light.\n")
}

func (light *Light) TurnOff() {

	command := exec.Command("python", "off.py")

	command.Stdout = os.Stdout
	command.Stderr = os.Stderr

	err := command.Run()
	if err != nil {
		klog.Infoln("Fail to execute script.\n")
		//return可能有问题
		return
	}

	light.power = "OFF"

	klog.Infoln("Success to turn off the light.\n")
}

func (light *Light) SetBrightness(value string) {

	command := exec.Command("python", "setBrightness.py", value)

	command.Stdout = os.Stdout
	command.Stderr = os.Stderr

	err := command.Run()
	if err != nil {
		klog.Infoln("Fail to execute script.\n")
		//return可能有问题
		return
	}

	light.brightness = value

	klog.Infoln("Success to set brightness.\n")
}

func (light *Light) SetRGB(red, green, blue string) {

	command := exec.Command("python", "setRGB.py", red, green, blue)

	command.Stdout = os.Stdout
	command.Stderr = os.Stderr

	err := command.Run()
	if err != nil {
		klog.Infoln("Fail to execute script.\n")
		//return可能有问题
		return
	}

	light.red = red
	light.green = green
	light.blue = blue

	klog.Infoln("Success to set RGB.\n")
}

// power和brightness用来记录上次云端修改的状态值，并不是设备的实际状态值
// 这是为了避免因为实际物理设备状态产生变化，仍然重复调用driver
func NewLight() *Light {
	light := &Light{
		power:      "OFF",
		brightness: "50",
		red:        "255",
		green:      "255",
		blue:       "255",
	}
	return light
}

//func CloseLight(light *Light) {
//	close(light.power)
//}
