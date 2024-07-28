from miio.integrations.yeelight.light.yeelight import Yeelight, YeelightStatus
import sys

light = Yeelight("192.168.3.110", "8abc18c479329aa02755102c1d7a1346")

args = sys.argv

light.set_rgb([int(args[1]), int(args[2]), int(args[3])])
