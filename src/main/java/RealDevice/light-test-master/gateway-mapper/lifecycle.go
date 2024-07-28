package main

import (
	"time"

	"k8s.io/klog/v2"
)

func (gateway *Gateway) initialize() {

	for {
		//actualPower, actualBrightness := light.GetStates()

		properties := gateway.GetStates()

		klog.Infoln("Properties:", properties["power"], properties["brightness"])

		gateway.publishToMqtt(properties)

		time.Sleep(5 * time.Second)
	}
}

func (gateway *Gateway) run() {
	//delta这个topic的原理是desired和reported只要不相同，就是一直发message
	token := cli.Subscribe(topic+"/delta", 0, gateway.subcribeFromMqtt)

	if token.Wait() && token.Error() != nil {
		klog.Infoln(token.Error())
	}
}
