from miio.integrations.zhimi.fan.fan import Fan

fan = Fan("192.168.3.121", "687c8694197333dd5ada5de6de908986")
fan.on()
print(fan.status())

# <FanStatus
# ac_power=True
# angle=120
# battery=None
# battery_charge=None
# battery_state=None
# button_pressed=None
# buzzer=False
# child_lock=False
# delay_off_countdown=0
# direct_speed=56
# humidity=None
# is_on=True
# led=None
# led_brightness=LedBrightness.Bright
# natural_speed=0
# oscillate=True
# power=on
# speed=558
# temperature=None
# use_time=1345>
