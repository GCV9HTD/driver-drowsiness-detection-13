from scipy.spatial import distance as dist
from imutils.video import VideoStream
from imutils.video import FPS
from imutils import face_utils
from threading import Thread
import numpy as np
import argparse
import imutils
import time
import dlib
import cv2
import RPi.GPIO as GPIO
import os

BUZ=19
flag = 20
GPIO.setmode(GPIO.BCM)     
GPIO.setwarnings(False)
GPIO.setup(flag,GPIO.IN)
GPIO.setup(BUZ,GPIO.OUT)
pwm = GPIO.PWM(BUZ, 1000)
pwm.start(0)

#determine ear
def eye_aspect_ratio(eye):	
	A = dist.euclidean(eye[1], eye[5])
	B = dist.euclidean(eye[2], eye[4])
	C = dist.euclidean(eye[0], eye[3])
	ear = (A + B) / (2.0 * C)
	return ear

def detection():
        ap = argparse.ArgumentParser()
        ap.add_argument("-p", "--shape-predictor", required=False,
                help="path to facial landmark predictor")
        ap.add_argument("-a", "--alarm", type=str, default="",
                help="path alarm .WAV file")
        ap.add_argument("-w", "--webcam", type=int, default=0,
                help="index of webcam on system")
        args = vars(ap.parse_args())

        # initialise values
        THRESH = 0.28
        CONSEC_FRAMES = 8
        COUNTER = 0

        PREDICTOR_PATH = "shape_predictor_68_face_landmarks.dat"
        detector = dlib.get_frontal_face_detector()
        
        predictor = dlib.shape_predictor(PREDICTOR_PATH)

        (lStart, lEnd) = face_utils.FACIAL_LANDMARKS_IDXS["left_eye"]
        (rStart, rEnd) = face_utils.FACIAL_LANDMARKS_IDXS["right_eye"]

        vs = VideoStream(src=args["webcam"]).start()
        fps = FPS().start()
        time.sleep(1.0)
        pwm.ChangeDutyCycle(100)
        time.sleep(1)
        pwm.ChangeDutyCycle(0)
        print("[INFO] Drowsy detection started")
        while GPIO.input(flag)==1:
        #while True:
                frame = vs.read()
                frame = imutils.resize(frame, width=800, height=600)
                gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
                rects = detector(gray, 0)
                for rect in rects:		
                        shape = predictor(gray, rect)
                        shape = face_utils.shape_to_np(shape)	
                        leftEye = shape[lStart:lEnd]
                        rightEye = shape[rStart:rEnd]
                        leftEAR = eye_aspect_ratio(leftEye)
                        rightEAR = eye_aspect_ratio(rightEye)		
                        ear = (leftEAR + rightEAR) / 2.0		
                        leftEyeHull = cv2.convexHull(leftEye)
                        rightEyeHull = cv2.convexHull(rightEye)
                        cv2.drawContours(frame, [leftEyeHull], -1, (0, 255, 0), 1)
                        cv2.drawContours(frame, [rightEyeHull], -1, (0, 255, 0), 1)
                        
                        if ear < EYE_AR_THRESH:
                                COUNTER += 1
                                if COUNTER >= EYE_AR_CONSEC_FRAMES:
                                        print("DROWSINESS ALERT!")                                                                               
                                        exists = os.path.isfile('alert.txt')
                                        if exists:
                                            os.remove('alert.txt')
                                        f = open('alert.txt', 'a')
                                        f.write("1")
                                        f.close()
                                        f = open('ret.txt', 'r')
                                        data = f.read()
                                        f.close()
                                        pwm.ChangeDutyCycle(float(data))				
                                        cv2.putText(frame, "DROWSINESS ALERT!", (70, 30),
                                                cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 0, 255), 2)
                        else:
                                COUNTER = 0                               
                                exists = os.path.isfile('alert.txt')
                                if exists:
                                        os.remove('alert.txt')
                                f = open('alert.txt', 'w')
                                f.close()
                                pwm.ChangeDutyCycle(0)
                        cv2.putText(frame, "EAR: {:.2f}".format(ear), (300, 30),
                                cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 0, 255), 2)	

                cv2.imshow("Frame", frame)
                key = cv2.waitKey(1) & 0xFF 
                if key == ord("q"):
                        break
        vs.stop()
        print("[INFO] Drowsy detection stopped")
        cv2.destroyAllWindows()

#detection()
