import cv2
import os
import shutil

os.chdir(os.path.dirname(os.path.abspath(__file__)))
input_eval_dir = os.path.abspath("")
files = os.listdir(input_eval_dir + "/videos")
#print(files)
for video_u in files:
    vidcap = cv2.VideoCapture('videos/' + video_u)
    success,img = vidcap.read()
    count = 0
    if not os.path.exists(os.path.join(input_eval_dir, video_u[:-4])):
        os.mkdir(os.path.join(input_eval_dir, video_u[:-4]))
    else:
        shutil.rmtree(os.path.join(input_eval_dir, video_u[:-4]))
        os.mkdir(os.path.join(input_eval_dir, video_u[:-4]))
    frame = 0 #after first frame read, so frame 0 will be saved, next every 10th
    success = True
    #print(input_eval_dir, success, image)
    while success:

        if frame % (int(vidcap.get(cv2.CAP_PROP_FRAME_COUNT)) // 20) == 0:
           #print(os.path.join(input_eval_dir, "frame%d.jpg" % count))
           print(img.shape)
           #img = cv2.imread("path_to_image.jpg")

           # rotate ccw
           out = cv2.transpose(img)
           out = cv2.flip(out, flipCode=1)

           # rotate cw
           out = cv2.transpose(img)
           out = cv2.flip(out, flipCode=0)
           cv2.imwrite(os.path.join(input_eval_dir, video_u[:-4], "frame%d.jpg" % count), out)     # save frame as JPEG file
           #cv2.waitKey(0)
           count += 1
        success,img = vidcap.read()
        frame += 1