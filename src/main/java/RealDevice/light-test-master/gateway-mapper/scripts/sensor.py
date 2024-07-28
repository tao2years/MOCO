from miio import Gateway

gateway = Gateway("192.168.3.119", "d64808c22af0d8c6d73eb9f6e2b9119f")

gateway.discover_devices()

devices = gateway.devices

for k,v in devices.items():
    v.update()
    print(k,v)
    print(v.status)
    print(v.device_type)
    print(v.sid)