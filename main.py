import face_detection
import open_mvg_pipline
import open_mvs_pipline
import os
import subprocess



from parameter import *

def main(requestId):
    print(requestId)
    face_detection.detect_face(requestId)
    open_mvg_pipline.detect_sfm(requestId)
    open_mvs_pipline.detect_ply(requestId, 'sequential')
    base_dir = os.path.dirname(__file__)
    RESULT_DIR = base_dir + "/mvg-output/output_set" + str(requestId) + "/reconstruction_sequential/mvs_sequential"
    BLENDER_BIN = "C:/Program Files/Blender Foundation/Blender 2.83"
    #print("3. Do Incremental/Sequential reconstruction")  # set manually the initial pair to avoid the prompt question
    pPreview = subprocess.Popen(["blender", "-b", "-P", os.path.join(base_dir, "preview_image.py"), "--", "--paths", RESULT_DIR + "/scene_dense_mesh_texture.ply", "--dimensions", "900"])
    pPreview.wait()

if __name__ == '__main__':
    try:
        config = get_parameters()
        main(config.Id)
    except Exception:
        raise (ValueError("Error"))