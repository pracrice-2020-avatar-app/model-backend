import os
import subprocess
import sys
import shutil

os.chdir(os.path.dirname(os.path.abspath(__file__)))
base_path = os.path.abspath(__file__)





def detect_ply(requestId, sfmType):
    input_dir = os.path.join('./mvg-output/', 'set', str(requestId))
    output_dir = os.path.join('./mvg-output/', 'set', str(requestId), sfmType)
    print ("5. MVG2MVS")
    os.chdir(os.path.join(input_dir))
    pRecons = subprocess.Popen( [os.path.join(OPENMVG_MVS_BIN, "openMVG_main_openMVG2openMVS"),  "-i", "sfm_data.bin", "-o", "scene.mvs", "-d", "scene_undistorted_images"] )
    pRecons.wait()

    print ("6. Dense point cloud")
    pRecons = subprocess.Popen( [os.path.join(OPENMVG_MVS_BIN, "DensifyPointCloud"),  os.path.join(reconstruction_dir, "scene.mvs")] )
    pRecons.wait()
