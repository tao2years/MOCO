package main

import (
	"time"
	"k8s.io/klog/v2"
)

func (camera *ChuangmiCamera) initialize() {

	for {
		properties := camera.GetStates()
		//klog.Infoln("Properties:", properties["power"], properties["flip"])
		camera.publishToMqtt(properties)
		time.Sleep(5 * time.Second)
	}
}

func (camera *ChuangmiCamera) run() {
	//delta这个topic的原理是desired和reported只要不相同，就是一直发message
	token := cli.Subscribe(topic+"/delta", 0, camera.subcribeFromMqtt)

	if token.Wait() && token.Error() != nil {
		klog.Infoln(token.Error())
	}
}
