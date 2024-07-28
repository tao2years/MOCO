from miio.integrations.yeelight.light.yeelight import Yeelight, YeelightStatus

light = Yeelight("192.168.3.110", "8abc18c479329aa02755102c1d7a1346")

lightStatus = light.status()

print(lightStatus.brightness)
