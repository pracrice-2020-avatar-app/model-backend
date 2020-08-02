import argparse


def get_parameters():

    parser = argparse.ArgumentParser()

    parser.add_argument('--Id', type=int, default='0')
    return parser.parse_args()