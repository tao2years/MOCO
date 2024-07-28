from miio.integrations.zhimi.humidifier.airhumidifier_miot import AirHumidifierMiot

humidifier = AirHumidifierMiot("192.168.3.129", "e5425414e9aa3804b6b3749e6c818429")

humidifier.on()

print(humidifier.status())
