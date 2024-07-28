package main

import (
	"time"

	"k8s.io/klog/v2"
)

func (light *Light) initialize() {

	for {
		//actualPower, actualBrightness := light.GetStates()

		properties := light.GetStates()

		//klog.Infoln("actualPower:" + actualPower + " actualBrightness:" + actualBrightness)
		klog.Infoln("Properties:", properties["power"], properties["brightness"], properties["red"], properties["green"], properties["blue"])

		light.publishToMqtt(properties)

		time.Sleep(5 * time.Second)
	}
}

func (light *Light) run() {
	//delta这个topic的原理是desired和reported只要不相同，就是一直发message
	token := cli.Subscribe(topic+"/delta", 0, light.subcribeFromMqtt)

	if token.Wait() && token.Error() != nil {
		klog.Infoln(token.Error())
	}
}
