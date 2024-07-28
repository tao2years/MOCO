from miio.integrations.chuangmi.camera.chuangmi_camera import ChuangmiCamera
import sys

camera = ChuangmiCamera("192.168.3.113", "663371493743636d7533686661463351")

args = sys.argv

if args[1] == "power":
    if args[2] == "ON":
        camera.on()
    elif args[2] == "OFF":
        camera.off()
elif args[1] == "flip": 
    if args[2] == "ON":
        camera.flip_on()
    elif args[2] == "OFF":
        camera.flip_off()
elif args[1] == "light": 
    if args[2] == "ON":
        camera.light_on()
    elif args[2] == "OFF":
        camera.light_off()
elif args[1] == "nightmode": 
    if args[2] == "0":
        camera.night_mode_auto()
    elif args[2] == "1":
        camera.night_mode_off()
    elif args[2] == "2":
        camera.night_mode_on()
elif args[1] == "watermark": 
    if args[2] == "ON":
        camera.watermark_on()
    elif args[2] == "OFF":
        camera.watermark_off()