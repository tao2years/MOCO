from miio.integrations.yeelight.light.yeelight import Yeelight, YeelightStatus
import sys


light = Yeelight("192.168.3.110", "8abc18c479329aa02755102c1d7a1346")

args = sys.argv
value1 = int(args[1])


light.set_brightness(value1)

