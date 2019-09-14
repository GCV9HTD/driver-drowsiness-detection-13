import urllib.request
import RPi.GPIO as GPIO
from drowsy import *
import os
import threading
from time import sleep

flag = 20
GPIO.setmode(GPIO.BCM)     
GPIO.setwarnings(False)
GPIO.setup(flag,GPIO.OUT)

def request():
    url = "http://ctcorphyd.com/Drowsy/status.php"
    contents = urllib.request.urlopen(url).read()
    #print(contents.decode()[2],contents.decode()[6])
    return contents.decode()[2],contents.decode()[6]

def post(val):    
    url = "http://ctcorphyd.com/Drowsy/alertkit.php?sts="+str(val)
    #print(url)
    contents = urllib.request.urlopen(url).read()
    

class connect(threading.Thread):
    def __init__(self, threadID, name, counter):
        threading.Thread.__init__(self)
        self.threadID = threadID
        self.name = name
        self.counter = counter
    def run(self):
        try:
            while True:
                power,_ = request()
                if(power=='0'):
                    GPIO.output(flag,0)
                    sleep(1)
                    #print("Quitt")
                    break              
                f = open('alert.txt', 'r')
                val=f.read()
                #print("val: ",str(val))
                f.close()
                if str(val)=="1":                    
                    print("Alert: Updating to the Application")
                    post(1)
                else:
                    post(0)
        except KeyboardInterrupt:
            print("Quit")



if __name__ =="__main__":
    try:
        print("Driver Drowsiness detection")
        print("Slect power ON button in APP to start detection")
        while True:
                power,volume = request()
                if(power=='1'):
                    print("Power ON")
                    if(volume=='1'):
                        exists = os.path.isfile('ret.txt')
                        if exists:
                            os.remove('ret.txt')
                        f = open('ret.txt', 'a')
                        f.write('30')
                        f.close()
                        print("Volume: 30")
                    elif(volume=='2'):
                        exists = os.path.isfile('ret.txt')
                        if exists:
                            os.remove('ret.txt')
                        f = open('ret.txt', 'a')
                        f.write('60')
                        f.close()
                        print("Volume: 60")
                    elif(volume=='3'):
                        exists = os.path.isfile('ret.txt')
                        if exists:
                            os.remove('ret.txt')
                        f = open('ret.txt', 'a')
                        f.write('100')
                        f.close()
                        print("Volume: 100")
                    print("Drowsy Detection is ON")
                    GPIO.output(flag,1)
                    thread1 = connect(1, "check", 1)
                    thread1.start()
                    #thread1.join()
                    detection()
                    '''t2 = threading.Thread(target=connect)
                    t2.start()
                    t2.join()'''
                    print("Detection Stopped")
            

    except KeyboardInterrupt:
            print("Closing Application")
