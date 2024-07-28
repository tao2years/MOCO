from miio.integrations.zhimi.airpurifier.airpurifier_miot import AirPurifierMiot

airPurifier = AirPurifierMiot("192.168.3.139", "e3d2c9f7cfa3c2c0244ddc9ab22515c5")

airPurifier.on()
# airPurifier.set_favorite_rpm(1000)

print(airPurifier.status())

# <AirPurifierMiotStatus
# anion=None
# aqi=None
# average_aqi=None
# buzzer=None
# buzzer_volume=None
# child_lock=None
# fan_level=1
# favorite_level=None
# favorite_rpm=None
# filter_hours_used=None
# filter_left_time=None
# filter_life_remaining=670
# filter_rfid_product_id=None
# filter_rfid_tag=None
# filter_type=None
# gestures=None
# humidity=25.0
# is_on=0
# led=None
# led_brightness=LedBrightness.Dim led_brightness_level=None
# mode=OperationMode.Unknown
# motor_speed=None
# pm10_density=None
# power=off
# purify_volume=None
# temperature=0
# tvoc=None
# use_time=None>

