from miio.integrations.lumi.gateway.gateway import Gateway

gateway = Gateway("192.168.3.119", "d64808c22af0d8c6d73eb9f6e2b9119f")

print(gateway.light.night_light_status().get("brightness"))
