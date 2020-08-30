import face_detection
import open_mvg_pipline
import open_mvs_pipline
import os
import subprocess



from parameter import *

def main(requestId):
    try:
        face_detection.detect_face(requestId)
    except Exception:
        print("face detection cant be made")
    open_mvg_pipline.detect_sfm(requestId)
    open_mvs_pipline.detect_ply(requestId, 'sequential')
    base_dir = os.path.dirname(__file__)
    RESULT_DIR = base_dir + "/mvg-output/output_set" + str(requestId) + "/reconstruction_sequential/mvs_sequential"
    BLENDER_BIN = "C:/Program Files/Blender Foundation/Blender 2.83"
    #print("3. Do Incremental/Sequential reconstruction")  # set manually the initial pair to avoid the prompt question
    pPreview = subprocess.Popen(["blender", "-b", "-P", os.path.join(base_dir, "preview_image.py"), "--", "--paths", RESULT_DIR + "/scene_dense_mesh_texture.ply", "--dimensions", "900"])
    pPreview.wait()

if __name__ == '__main__':
    log = open("C:/Users/Kolldun/IdeaProjects/model-backend/log.txt", 'a')
    try:
        config = get_parameters()
        log.write(str(config.Id) + " Started\n")
        main(config.Id)
        log.write(str(config.Id) + ' Success\n')
        log.close()
    except Exception:
        log.write(str(config.Id) + ' Error\n')
        log.close()
        #raise (ValueError("Error"))
        exit(1)