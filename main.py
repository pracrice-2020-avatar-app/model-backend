import face_detection
import open_mvg_pipline
import open_mvs_pipline
import os
import subprocess
import open3d
import numpy as np
import math



from parameter import *

def main(requestId):
    # base_dir = os.path.dirname(__file__)
    # for file in sorted(os.listdir(base_dir + '/Rest/Model_images/photos/' + 'set' + str(requestId)),
    #                    key=lambda u: 10000 if 'mask' in u else int(u[:-4])):
    #     if 'mask' in file:
    #         continue
    #     image = cv2.imread(base_dir + '/Rest/Model_images/photos/' + 'set' + str(requestId) + '/' + file)
    #     cv2.imwrite(base_dir + '/for-mvg/' + 'set' + str(requestId) + '/' + file, image)
    try:
        #face_detection.detect_face(requestId)
        print(0)
    except Exception:
        print("face detection cant be made")
    open_mvg_pipline.detect_sfm(requestId)
    open_mvs_pipline.detect_ply(requestId, 'sequential')
    base_dir = os.path.dirname(__file__)
    RESULT_DIR = base_dir + "/mvg-output/output_set" + str(requestId) + "/reconstruction_sequential/mvs_sequential"
    BLENDER_BIN = "C:/Program Files/Blender Foundation/Blender 2.83"
    MVG_DIR = base_dir + "/mvg-output/output_set" + str(requestId) + "/reconstruction_sequential"
    pcd = open3d.io.read_point_cloud(MVG_DIR + "/colorized.ply")
    array = np.asarray(pcd.points)
    colorized_data_1 = array[-5].tolist()
    array = array.tolist()
    sum = [0] * 3
    for i in array:
        for j in range(3):
            sum[j] += i[j]

    centre = [i / len(array) for i in sum]
    print(centre)
    #colorized_data_1 = [0,0,0]
    view = [0] * 3
    for i in range(3):
        view[i] = centre[i] - colorized_data_1[i]
    print(view, colorized_data_1)
    for i in range(3):
        for j in range(3):
            if i == j:
                continue
            print(math.atan2(view[i], -view[j]), i, j)
    def cel(x):
        return (360 + int(x)) % 360
    rotation = ','.join(map(str, map(cel, map(math.degrees, [math.atan2(-view[1], view[2]), math.atan2(view[0], -view[2]), math.atan2(-view[1], view[0])]))))
    print(rotation)

    coords = ','.join(map(str, colorized_data_1))
    #coords = "0,0,0"
    #print("3. Do Incremental/Sequential reconstruction")  # set manually the initial pair to avoid the prompt question
    pPreview = subprocess.Popen(["blender", "-b", "-P", os.path.join(base_dir, "preview_image.py"), "--", "--paths", RESULT_DIR + "/scene_dense_mesh_texture.ply", "--dimensions", "900", "--camera-coords", coords, '-r', rotation])
    pPreview.wait()

if __name__ == '__main__':
    log = open("C:/Users/Kolldun/IdeaProjects/model-backend/log.txt", 'a')
    try:
        config = get_parameters()
        config.Id = 6
        log.write(str(config.Id) + " Started\n")
        main(config.Id)
        log.write(str(config.Id) + ' Success\n')
        log.close()
    except Exception:
        log.write(str(config.Id) + ' Error\n')
        log.close()
        #raise (ValueError("Error"))
        exit(1)