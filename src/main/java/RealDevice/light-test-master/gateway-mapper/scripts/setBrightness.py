from miio.integrations.lumi.gateway.gateway import Gateway
import sys

gateway = Gateway("192.168.3.119", "d64808c22af0d8c6d73eb9f6e2b9119f")

args = sys.argv
value1 = int(args[1])

gateway.light.set_night_light_brightness(value1)

