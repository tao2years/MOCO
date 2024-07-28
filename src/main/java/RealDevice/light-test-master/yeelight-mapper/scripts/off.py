from miio.integrations.yeelight.light.yeelight import Yeelight
#from miio.device import Device

#light = Device("192.168.3.110", "8abc18c479329aa02755102c1d7a1346")
light = Yeelight("192.168.3.110", "8abc18c479329aa02755102c1d7a1346")
light.off()

#print(light.info())
#print(light.status())

#light.send("set_power", ["off"])