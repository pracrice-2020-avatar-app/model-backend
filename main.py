import face_detection
import open_mvg_pipline
import open_mvs_pipline



from parameter import *

def main(requestId):
    print(requestId)
    face_detection.detect_face(requestId)
    open_mvg_pipline.detect_sfm(requestId)
    open_mvs_pipline.detect_ply(requestId, 'sequential')

if __name__ == '__main__':
    config = get_parameters()
    main(config.Id)
