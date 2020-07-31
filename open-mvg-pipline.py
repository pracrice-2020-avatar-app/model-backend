#!/usr/bin/python
#! -*- encoding: utf-8 -*-

# This file is part of OpenMVG (Open Multiple View Geometry) C++ library.

# Python script to launch OpenMVG SfM tools on an image dataset
#
# usage : python open-mvg-pipline.py
#

# Indicate the openMVG binary directory
OPENMVG_SFM_BIN = "C:/dev/openMVG/openMVG_build/Windows-AMD64-Release/Release"
# Indicate the openMVG camera sensor width directory
CAMERA_SENSOR_WIDTH_DIRECTORY = "C:/dev/openMVG/src/software/SfM" + "/../../openMVG/exif/sensor_width_database"

import os
import subprocess
import sys
import shutil

def get_parent_dir(directory):
    return os.path.dirname(directory)

os.chdir(os.path.dirname(os.path.abspath(__file__)))
input_eval_dir = os.path.abspath("./for-mvg")
# Checkout an OpenMVG image dataset with Git
if not os.path.exists(input_eval_dir):
  pImageDataCheckout = subprocess.Popen([ "git", "clone", "https://github.com/openMVG/ImageDataset_SceauxCastle.git" ])
  pImageDataCheckout.wait()

nameset = "set6"
output_eval_dir = os.path.join(get_parent_dir(input_eval_dir), 'mvg-output', "output_" + nameset)
input_eval_dir = os.path.join(input_eval_dir, nameset)
if not os.path.exists(output_eval_dir):
  os.mkdir(output_eval_dir)
else:
    shutil.rmtree(output_eval_dir)
    os.mkdir(output_eval_dir)

input_dir = input_eval_dir
output_dir = output_eval_dir
print ("Using input dir  : ", input_dir)
print ("      output_dir : ", output_dir)

matches_dir = os.path.join(output_dir, "matches")
camera_file_params = os.path.join(CAMERA_SENSOR_WIDTH_DIRECTORY, "sensor_width_camera_database.txt")

# Create the ouput/matches folder if not present
if not os.path.exists(matches_dir):
  os.mkdir(matches_dir)

print ("1. Intrinsics analysis")
pIntrisics = subprocess.Popen( [os.path.join(OPENMVG_SFM_BIN, "openMVG_main_SfMInit_ImageListing"),  "-i", input_dir, "-o", matches_dir, "-d", camera_file_params, '-f', str(1.2 * max(4032, 3024))])
pIntrisics.wait()

print ("2. Compute features")
pFeatures = subprocess.Popen( [os.path.join(OPENMVG_SFM_BIN, "openMVG_main_ComputeFeatures"),  "-i", matches_dir+"/sfm_data.json", "-o", matches_dir, "-m", "SIFT", "-f" , "1"] )
pFeatures.wait()

print ("2. Compute matches")
pMatches = subprocess.Popen( [os.path.join(OPENMVG_SFM_BIN, "openMVG_main_ComputeMatches"),  "-i", matches_dir+"/sfm_data.json", "-o", matches_dir, "-f", "1", "-n", "AUTO"] )
pMatches.wait()

reconstruction_dir = os.path.join(output_dir,"reconstruction_sequential")
print ("3. Do Incremental/Sequential reconstruction") #set manually the initial pair to avoid the prompt question
pRecons = subprocess.Popen( [os.path.join(OPENMVG_SFM_BIN, "openMVG_main_IncrementalSfM"),  "-i", matches_dir+"/sfm_data.json", "-m", matches_dir, "-o", reconstruction_dir, "-f", "NONE"] )
pRecons.wait()

print ("5. Colorize Structure")
pRecons = subprocess.Popen( [os.path.join(OPENMVG_SFM_BIN, "openMVG_main_ComputeSfM_DataColor"),  "-i", reconstruction_dir+"/sfm_data.bin", "-o", os.path.join(reconstruction_dir,"colorized.ply")] )
pRecons.wait()

print ("4. Structure from Known Poses (robust triangulation)")
pRecons = subprocess.Popen( [os.path.join(OPENMVG_SFM_BIN, "openMVG_main_ComputeStructureFromKnownPoses"),  "-i", reconstruction_dir+"/sfm_data.bin", "-m", matches_dir, "-o", os.path.join(reconstruction_dir,"robust.ply")] )
pRecons.wait()

print ("5. MVG2MVS")
pRecons = subprocess.Popen( ["openMVG_main_openMVG2openMVS",  "-i", "sfm_data.bin", "-m", matches_dir, "-o", "scene.mvs", "-d", "scene_undistorted_images"] )
pRecons.wait()

print ("6. Dense point cloud")
pRecons = subprocess.Popen(["DensifyPointCloud",  os.path.join(reconstruction_dir, "scene.mvs")])
pRecons.wait()

print ("3. Do Incremental/Sequential reconstruction") #set manually the initial pair to avoid the prompt question

# print ("5. MVG2MVS")
# pRecons = subprocess.Popen( [os.path.join(OPENMVG_SFM_BIN, "openMVG_main_openMVG2openMVS"),  "-i", "sfm_data.bin", "-m", matches_dir, "-o", "scene.mvs", "-d", "scene_undistorted_images"] )
# pRecons.wait()
#
# print ("6. Dense point cloud")
# pRecons = subprocess.Popen( [os.path.join(OPENMVG_SFM_BIN, "DensifyPointCloud"),  os.path.join(reconstruction_dir, "scene.mvs")] )
# pRecons.wait()
#
# print ("3. Do Incremental/Sequential reconstruction") #set manually the initial pair to avoid the prompt question
# pRecons = subprocess.Popen( [os.path.join(OPENMVG_SFM_BIN, "openMVG_main_IncrementalSfM"),  "-i", matches_dir+"/sfm_data.json", "-m", matches_dir, "-o", reconstruction_dir] )
# pRecons.wait()
# # Reconstruction for the global SfM pipeline
# # - global SfM pipeline use matches filtered by the essential matrices
# # - here we reuse photometric matches and perform only the essential matrix filering
print ("2. Compute matches (for the global SfM Pipeline)")
pMatches = subprocess.Popen( [os.path.join(OPENMVG_SFM_BIN, "openMVG_main_ComputeMatches"),  "-i", matches_dir+"/sfm_data.json", "-o", matches_dir, "-r", "0.8", "-g", "e"] )
pMatches.wait()

reconstruction_dir = os.path.join(output_dir,"reconstruction_global")
print ("3. Do Global reconstruction")
pRecons = subprocess.Popen( [os.path.join(OPENMVG_SFM_BIN, "openMVG_main_GlobalSfM"),  "-i", matches_dir+"/sfm_data.json", "-m", matches_dir, "-o", reconstruction_dir] )
pRecons.wait()

print ("5. Colorize Structure")
pRecons = subprocess.Popen( [os.path.join(OPENMVG_SFM_BIN, "openMVG_main_ComputeSfM_DataColor"),  "-i", reconstruction_dir+"/sfm_data.bin", "-o", os.path.join(reconstruction_dir,"colorized.ply")] )
pRecons.wait()

print ("4. Structure from Known Poses (robust triangulation)")
pRecons = subprocess.Popen( [os.path.join(OPENMVG_SFM_BIN, "openMVG_main_ComputeStructureFromKnownPoses"),  "-i", reconstruction_dir+"/sfm_data.bin", "-m", matches_dir, "-o", os.path.join(reconstruction_dir,"robust.ply")] )
pRecons.wait()


