import os
import subprocess
import tempfile
import sys
import shutil
import time


def detect_ply(requestId, sfmType):
    os.chdir(os.path.dirname(os.path.abspath(__file__)))
    base_path = os.path.abspath(__file__)
    OPENMVG_MVG_BIN = 'C:/Program Files/openMVG/bin'
    OPENMVG_MVS_BIN = 'C:/dev/OpenMVS/build/bin/vc15/x64/Release'

    input_dir = os.path.abspath('./mvg-output/output_set' + str(requestId))
    input_dir = os.path.join(input_dir, 'reconstruction_' + sfmType)
    output_dir = os.path.abspath('./mvg-output/output_set' + str(requestId))
    output_dir = os.path.join(input_dir, 'mvs_' + sfmType)
    if not os.path.exists(output_dir):
        os.mkdir(output_dir)
    time.sleep(3)
    print ("1. MVG2MVS")
    os.chdir(os.path.join(input_dir))
    pRecons = subprocess.Popen( [os.path.join(OPENMVG_MVG_BIN, "openMVG_main_openMVG2openMVS"),  "-i", os.path.join(input_dir, "sfm_data.bin"), "-o", os.path.join(output_dir, "scene.mvs"), "-d", os.path.join(output_dir, "scene_undistorted_images")] )
    pRecons.wait()
    input_dir = output_dir
    print ("2. Dense point cloud")
    os.chdir(os.path.join(input_dir))
    pRecons = subprocess.Popen( [os.path.join(OPENMVG_MVS_BIN, "DensifyPointCloud"), 'scene.mvs', '--resolution-level', '3', '--sample-mesh', '0', '--max-resolution', '5000'])
    pRecons.wait()
    print("3. Reconstruction mesh")
    os.chdir(os.path.join(input_dir))
    pRecons = subprocess.Popen( [os.path.join(OPENMVG_MVS_BIN, "ReconstructMesh"), 'scene_dense.mvs', '--free-space-support', '0', '--decimate', '0.1'])


    pRecons.wait()
    # pRecons = subprocess.Popen( [os.path.join(OPENMVG_MVS_BIN, "RefineMesh"), 'scene_dense_mesh.mvs', '--max-face-area', '15'])
    # print("4. Refine mesh")
    # pRecons.wait()

    print("4. Texture mesh")
    os.chdir(os.path.join(input_dir))

    pRecons = subprocess.Popen([os.path.join(OPENMVG_MVS_BIN, "TextureMesh"), 'scene_dense_mesh.mvs'])
    pRecons.wait