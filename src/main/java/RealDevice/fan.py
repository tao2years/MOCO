from miio.integrations.zhimi.fan.fan import Fan



#light = Device("192.168.3.110", "8abc18c479329aa02755102c1d7a1346")
#gateway = Device("192.168.3.108", "bedf4dc93ddd7a926b218160adacb17a")
fan = Fan("192.168.3.121", "687c8694197333dd5ada5de6de908986")
#camera.off()


#print(camera.info())
#print(light.get_properties(["brightness"]))
#print(gateway.get_properties(["brightness"]))
print(fan.status())

