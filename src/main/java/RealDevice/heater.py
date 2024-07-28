from miio.integrations.zhimi.heater.heater_miot import HeaterMiot



#light = Device("192.168.3.110", "8abc18c479329aa02755102c1d7a1346")
#gateway = Device("192.168.3.108", "bedf4dc93ddd7a926b218160adacb17a")
heater = HeaterMiot("192.168.3.140", "f1aca47e0dcee799e5583d044c257ac5")
#camera.off()


#print(camera.info())
#print(light.get_properties(["brightness"]))
#print(gateway.get_properties(["brightness"]))
print(heater.status())
