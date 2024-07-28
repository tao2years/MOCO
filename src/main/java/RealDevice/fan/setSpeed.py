import sys
from miio.integrations.zhimi.fan.fan import Fan

if len(sys.argv) != 2:
    print("Usage: python setSpeed.py <fan-speed>")
    sys.exit(1)
speed = int(sys.argv[1])
# speed = 50

fan = Fan("192.168.3.121", "687c8694197333dd5ada5de6de908986")

fan.set_direct_speed(speed)
print(fan.status())

