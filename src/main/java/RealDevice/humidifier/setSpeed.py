import sys
from miio.integrations.zhimi.humidifier.airhumidifier_miot import AirHumidifierMiot


if len(sys.argv) != 2:
    print("Usage: python setSpeed.py <humidifier-speed>")
    sys.exit(1)
speed = int(sys.argv[1])
# speed = False

humidifier = AirHumidifierMiot("192.168.3.129", "e5425414e9aa3804b6b3749e6c818429")

humidifier.set_speed(speed)
print(humidifier.status())

