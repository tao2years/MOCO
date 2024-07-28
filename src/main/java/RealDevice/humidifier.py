from miio.integrations.zhimi.humidifier.airhumidifier_miot import AirHumidifierMiot

humidifier = AirHumidifierMiot("192.168.3.129", "e5425414e9aa3804b6b3749e6c818429")

humidifier.on()
humidifier.set_buzzer(False)    # True & False
humidifier.set_speed(500) # [200, 2000] x%10=0, int
humidifier.set_target_humidity(75) # [30, 80], int
humidifier.set_dry(False)   # True & False
humidifier.set_clean_mode(True)    # True & False

print(humidifier.status())

# <AirHumidifierMiotStatus
# actual_speed=0
# button_pressed=PressedButton.No
# buzzer=True
# child_lock=False
# clean_mode=False
# dry=True
# error=0
# fahrenheit=81.8
# humidity=69
# is_on=True
# led_brightness=LedBrightness.Dim
# mode=OperationMode.High
# motor_speed=500
# power=on
# power_time=186
# target_humidity=70
# temperature=27.7
# use_time=1212683
# water_level=0
# water_tank_detached=False>



