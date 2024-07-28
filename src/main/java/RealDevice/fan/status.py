from miio.integrations.zhimi.fan.fan import Fan

fan = Fan("192.168.3.121", "687c8694197333dd5ada5de6de908986")
print(fan.status())