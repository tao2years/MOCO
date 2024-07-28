from miio.integrations.chuangmi.camera.chuangmi_camera import ChuangmiCamera
import sys

camera = ChuangmiCamera("192.168.3.113", "663371493743636d7533686661463351")
#mijia.camera.v3
cameraStatus = camera.status()

args = sys.argv

if args[1] == "power":
    if (cameraStatus.power == True):
        print("ON")
    else:
        print("OFF")   
elif args[1] == "flip":
    if (cameraStatus.flip == True):
        print("ON")
    else:
        print("OFF")  
elif args[1] == "light":
    if (cameraStatus.light == True):
        print("ON")
    else:
        print("OFF")  
elif args[1] == "nightmode":
    print(cameraStatus.night_mode)  
elif args[1] == "watermark":
    if (cameraStatus.watermark == True):
        print("ON")
    else:
        print("OFF")  
