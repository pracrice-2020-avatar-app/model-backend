import os
import cv2
import subprocess
import numpy as np
import shutil

base_dir = os.path.dirname(__file__)

def rename_set_images(setId):
    i = -1
    u = sorted(os.listdir(base_dir + '/photo/' + 'set' + str(setId)))
    for file in u:
        i += 1
        if file[:-4] != str(i) and str(i) + '.JPG' not in u and str(i) +'.jpg' not in u:
            print(file, i)
            os.rename(base_dir + '/photo/' + 'set' + str(setId) + '/' + file, base_dir + '/photo/' + 'set' + str(setId) + '/' + str(i) + file[-4:])

def delete_dirs(list):
    for i in list:
        if os.path.exists(i):
            shutil.rmtree(i)
