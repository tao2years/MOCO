from miio.integrations.yeelight.light.yeelight import Yeelight, YeelightStatus


light = Yeelight("192.168.3.110", "8abc18c479329aa02755102c1d7a1346")
lightStatus = light.status()
print(lightStatus.rgb[0], lightStatus.rgb[1], lightStatus.rgb[2])
#255 255 255\n
