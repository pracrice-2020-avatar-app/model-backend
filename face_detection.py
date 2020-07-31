import os
import cv2
import subprocess
import numpy as np
import utilu
from PIL import Image

base_dir = os.path.dirname(__file__)
prototxt_path = os.path.join(base_dir, 'face_detection/model_data/deploy.prototxt')
caffemodel_path = os.path.join(base_dir, 'face_detection/model_data/weights.caffemodel')

model = cv2.dnn.readNetFromCaffe(prototxt_path, caffemodel_path)

def flood_fill_single(base_img, mask, seed_point):
	if all(base_img[seed_point[1]][seed_point[0]] == [0, 0, 0]):
		img = base_img.copy()
		img = img.astype('uint8')
		num,im,mask,rect = cv2.floodFill(img, mask, seed_point, 0, 0, 0, flags=4 | (255 << 8))
	return mask

def intoshape(u, w, h):
	u[0] = max(u[0], 0)
	u[1] = max(u[1], 0)
	u[2] = min(w, u[2])
	u[3] = min(h, u[3])
	return u.copy()

def detect_face(requestId):
	utilu.rename_set_images(requestId)
	if not os.path.exists('face_detection/updated_images/' + 'set' + str(requestId)):
		print('New directory created')
		os.makedirs('face_detection/updated_images/' + 'set' + str(requestId))

	if not os.path.exists('face_detection/faces'):
		print('New directory created')
		os.makedirs('face_detection/faces')
	labelh = dict()
	face_dir = base_dir + '/face_detection'
	count = -1
	base = dict()
	for file in sorted(os.listdir(base_dir + '/photo/' + 'set' + str(requestId)), key = lambda u: 10000 if 'mask' in u else int(u[:-4])):
		if 'mask' in file:
			continue
		count += 1
		image = cv2.imread(base_dir + '/photo/' + 'set' + str(requestId) + '/' + file)
		cv2.imwrite(base_dir + '/for-mvg/' + 'set' + str(requestId) + '/' + file, image)
		base[file[:-4]] = image
		(h, w) = image.shape[:2]
		blob = cv2.dnn.blobFromImage(cv2.resize(image, (300, 300)), 1.0, (300, 300), (104.0, 177.0, 123.0))

		model.setInput(blob)
		detections = model.forward()
		# Create frame around face
		for i in range(0, detections.shape[2]):
			box = detections[0, 0, i, 3:7] * np.array([w, h, w, h])
			(startX, startY, endX, endY) = box.astype('int')

			confidence = detections[0, 0, i, 2]
			wd = abs(startX - endX)
			hd = abs(startY - endY)
			shape = [max(startX - wd // 5 * 2, 0), max(0, startY - hd // 3), min(endX + wd // 3, w), min(h, endY + hd // 10)]
			# If confidence > 0.5, show box around face
			biss = [(shape[0] + shape[2]) // 2, (shape[1] + shape[3]) // 2]
			shape9 = max(shape[2] - shape[0], shape[3] - shape[1])
			shape27 = intoshape([min(biss[0] - shape9 // 2, w - shape9), min(biss[1] - shape9 // 2, h - shape9), max(shape9, biss[0] + shape9 // 2), max(shape9, biss[1] + shape9 // 2)], w, h)
			if (confidence > 0.5):
				frame = image[shape27[1]:shape27[3], shape27[0]:shape27[2]].copy()
				cv2.rectangle(image, (startX, startY), (endX, endY), (255, 255, 255), 2)
				cv2.rectangle(image, tuple(shape27[:2]), tuple(shape27[2:]), (255, 255, 255), 2)
				labelh[int(file[:-4])] = [shape27, shape, shape27[2] - shape27[0]]
		cv2.imwrite(face_dir + '/updated_images/' + 'set' + str(requestId) + '/' + file, image)
		frame = cv2.resize(frame, (512, 512))
		cv2.imwrite(base_dir + '/face-parsing/Data_preprocessing/test_img/' + str(count) + '.jpg', frame)
		print('Image ' + file + ' converted successfully')
	os.chdir(os.path.join(base_dir, 'face-parsing'))
	pRecons = subprocess.Popen(['python', '-u', 'main.py', '--batch_size', '1', '--imsize', '512', '--test_size', str(len(os.listdir(base_dir + '/face-parsing/Data_preprocessing/test_img/')))])
	pRecons.wait()
	count = -1
	face_parsing = base_dir + '/face-parsing/'
	for file in sorted(os.listdir(base_dir + '/face-parsing/' + 'test_color_visualize/'), key = lambda u: 10000 if 'mask' in u else int(u[:-4])):
		if 'mask' in file:
			continue
		count += 1
		image = cv2.imread(face_parsing + '/test_color_visualize/' + file)
		#print(face_parsing + 'test_color_visualize/' + file)
		#print(labelh, count)
		mask = np.zeros((image.shape[0] + 2, image.shape[1] + 2), np.uint8)
		w = image.shape[0]
		h = w
		for i in [(0, 0), (0, w // 2), (h // 2, 0), (h - 1, w // 2), (h // 2, w - 1), (0, h - 1), (w - 1, 0), (h - 1, w - 1)]:
			mask = flood_fill_single(image, mask, i)
		mask_new = cv2.bitwise_not(mask)
		mask_new = mask_new[1:-1, 1:-1]
		print(mask_new.shape, int(file[:-4]), len(labelh))
		mask_new = cv2.resize(mask_new, (labelh[int(file[:-4])][2], labelh[int(file[:-4])][2]))
		base_img = base[file[:-4]].copy()
		mask = np.zeros((base_img.shape[0], base_img.shape[1]), np.uint8)
		xstart, ystart, xend, yend = labelh[int(file[:-4])][0][:]
		mask[ystart:yend, xstart:xend] = mask_new
		mask = (mask > 0) * 255
		#print(roi.shape, mask_new.shape, xstart - xend, ystart - yend)
		#u = cv2.bitwise_and(roi, mask_new)
		#cv2.CvtColor(vis, vis2, cv.CV_GRAY2BGR)
		print(mask)
		path = base_dir + '/for-mvg/' + 'set' + str(requestId) + '/' + file[:-4] + '_mask.png'
		if not os.path.exists(base_dir + '/for-mvg/' + 'set' + str(requestId)):
			#print('New directory created')
			os.makedirs(base_dir + '/for-mvg/' + 'set' + str(requestId))
		Image.fromarray(np.uint8(mask)).save(path)



detect_face(6)