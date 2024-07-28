from miio.integrations.cgllc.airmonitor.airqualitymonitor_miot import AirQualityMonitorCGDN1

monitor = AirQualityMonitorCGDN1("192.168.3.125", "d56befc9dd39dea74710102fdf1ee198")

print(monitor.status())

print(monitor.status().pm25)

# <AirQualityMonitorCGDN1Status
# battery=100
# charging_state=ChargingState.Charging
# co2=700 device_off=10
# display_temperature_unit=DisplayTemperatureUnitCGDN1.Celcius
# humidity=59
# monitoring_frequency=0
# pm10=11
# pm25=11
# screen_off=600
# temperature=29.8385>
