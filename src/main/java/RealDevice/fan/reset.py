from miio.integrations.zhimi.fan.fan import Fan

fan = Fan("192.168.3.121", "687c8694197333dd5ada5de6de908986")
fan.on()
fan.set_angle(0)
fan.set_direct_speed(1)
fan.off()
print(fan.status())