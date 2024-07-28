import sys
from miio.integrations.zhimi.humidifier.airhumidifier_miot import AirHumidifierMiot


if len(sys.argv) != 2:
    print("Usage: python setTargetHumidity.py <humidifier-targetHumidity>")
    sys.exit(1)
humidity = int(sys.argv[1])

humidifier = AirHumidifierMiot("192.168.3.129", "e5425414e9aa3804b6b3749e6c818429")

humidifier.set_target_humidity(humidity)
print(humidifier.status())

