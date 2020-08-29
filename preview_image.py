
from bpy import context, data, ops
import bpy
import argparse
from datetime import datetime
import logging
import math
import os
import sys

# some default colors for adding a stamp to your render (Red, Green, Blue, Opacity)
WATERMARK_WHITE = (255, 255, 255, 1)
WATERMARK_TRANSLUCENT_WHITE = (255, 255, 255, .75)
WATERMARK_BLACK = (0, 0, 0, 1)
WATERMARK_TRANSLUCENT_BLACK = (0, 0, 0, .75)
DEFAULT_CAMERA_COORDS = (1.7, 0, 1)  # by default our camera sits 10 units above the origin
DEFAULT_CAMERA_ROTATION = (10, -145, 171)  # the camera points down on our mesh
DEFAULT_LIGHT = (7, 7, 7)

class Mesh2Img(object):
    print(data.lights)
    DEFAULT_OUTPUT_TEMPLATE = "{filepath}_{width}.{ext}"  # this will generate the image next to the original mesh file
    IMAGE_FORMATS = {
        'bmp': 'BMP',
        'jpg': 'JPEG',
        'png': 'PNG',
        'tif': 'TIFF'
    }
    MESH_TYPES = {
        '.ply': ops.import_mesh.ply,
    }

    def __init__(self, paths=None, dimensions=None, material="", image_format=None, verbose=False,
                 output_template=DEFAULT_OUTPUT_TEMPLATE, max_dim=9.0, camera_coords=DEFAULT_CAMERA_COORDS,
                 camera_rotation=DEFAULT_CAMERA_ROTATION, jpeg_quality=80):

        if paths is not None:
            if isinstance(paths, str):  # if they gave us just 1 path instead of a list of paths
                paths = [paths]
        else:
            paths = []
        self.filepaths = paths
        self.materials = []
        self._job_templates = []
        self.verbose = self._verbose = bool(verbose)
        self.max_dim = max_dim
        self.camera_coords = camera_coords
        self.camera_rotation = camera_rotation
        self.execute_time = datetime.now().strftime('%Y-%m-%d_%H%M%S')
        if dimensions:
            for d in dimensions:
                self.add_job_template(d, output_template=output_template, image_format=image_format,
                                      jpeg_quality=jpeg_quality)

    @property
    def verbose(self):
        return self._verbose

    @verbose.setter
    def verbose(self, value):
        self._verbose = bool(value)
        if value:
            logging.getLogger().setLevel(logging.DEBUG)
        else:
            logging.getLogger().setLevel(logging.WARNING)
    def add_job_template(self, dimensions, output_template=DEFAULT_OUTPUT_TEMPLATE, image_format='png',
                         jpeg_quality=80):
        self._job_templates.append(JobTemplate(dimensions, output_template, image_format, jpeg_quality=jpeg_quality))

    @classmethod
    def open_mesh(cls, filepath):
        ext = os.path.splitext(filepath)[1].lower()
        cls.MESH_TYPES[ext](filepath=filepath)
        mesh = context.selected_objects[0]
        ops.object.select_all(action='DESELECT')
        mesh.select_set(state=True)
        ops.object.origin_set(type='GEOMETRY_ORIGIN')
        return mesh

    def start(self):
        # prepare the scene
        delete_object_by_name("Cube", ignore_errors=True)  # factory default Blender has a cube in the default scene
        camera_params = self.camera_coords + self.camera_rotation
        set_camera(*camera_params)  # take picture from 10 units away
        set_light(*DEFAULT_LIGHT)

        for filepath in self.filepaths:
            if os.path.isdir(filepath):
                self._process_dir(filepath)
            else:
                self._process_file(filepath)

    def _process_dir(self, filepath):

        for folder, subfolders, filenames in os.walk(filepath):  # recurse directory
            logging.debug("Entering %s", folder)
            for filename in filenames:  # for each file in this directory
                ext = os.path.splitext(filename)[1].lower()
                if ext in self.MESH_TYPES:  # if this is a known mesh file type
                    file = os.path.join(folder, filename)  # this is the full path to that file
                    self._process_file(file)  # process this file now

    def _process_file(self, filepath, leave_mesh_open=True):

        mesh = self.open_mesh(filepath)
        scale_mesh(mesh, max_dim=self.max_dim)
        if self.materials:
            self._apply_material(mesh, self.materials)

        for jt in self._job_templates:
            logging.debug("Applying %s to %s", jt, filepath)
            output_path = jt.get_output_path(filepath, exec_time=self.execute_time)
            self.save_image(output_path, width=jt.width, height=jt.height, file_format=jt.image_format,
                            jpeg_quality=jt.jpeg_quality)
        if not leave_mesh_open:
            self._delete_mesh(mesh)

    @staticmethod
    def _apply_material(mesh, material_names):

        if isinstance(material_names, str):
            material_names = [material_names]
        materials = []
        for material_name in material_names:
            try:
                material = data.materials[material_name]
                materials.append(material)
            except KeyError:
                raise ValueError("The material name '%s' was not found.", material_name)
        # all the materials were successfully found so now we will claer the object's existing material
        mesh.data.materials.clear()
        for material in materials:
            mesh.data.materials.append(material)

    @staticmethod
    def _delete_mesh(mesh):
        name = mesh.name
        ops.object.select_all(action='DESELECT')
        mesh.select_set(state=True)
        status = ops.object.delete()
        if 'FINISHED' not in status:
            raise Exception("Failed to delete mesh %s." % name)

    @classmethod
    def command_line(cls):

        try:
            index = sys.argv.index("--") + 1  # ignore anything before the '--' in the blender.exe invocation
        except ValueError:
            index = len(sys.argv)

        parser = argparse.ArgumentParser('Mesh2Img',
                                         description="A utility for generating image previews of STL and PLY files "
                                                     "using Blender's Python scripting engine.")
        parser.add_argument('-d', '--dimensions', type=str, nargs='+', required=True,
                            help='Provide either at least 1 dimension or pair of dimensions to specify the size of the '
                                 'images to generate. i.e. `-d 400 800,600 2048` would create a 400x400, 800x600, and '
                                 '2048x2048 image for each STL or PLY file found.')
        parser.add_argument('-p', '--paths', type=str, nargs='+', required=True,
                            help='The path(s) to the mesh file(s). If a directory is given, all PLY and STL files in '
                                 'the entire directory tree are processed. A mixed list of file paths and folder paths '
                                 'can be given.')
        parser.add_argument('-v', '--verbose', action='store_true',
                            help="See more output logging to the command line.")
        parser.add_argument('-i', '--image-format', default='png', choices=cls.IMAGE_FORMATS.keys(), type=str,
                            help="Specify what image format to output as.")
        parser.add_argument('--jpeg-quality', default=80, type=int,
                            help="The JPEG quality if `jpg` was chosen as the image-format.")
        parser.add_argument('-o', '--output-template', default=cls.DEFAULT_OUTPUT_TEMPLATE, type=str,
                            help="Define how you'd like output images to be named and where to put them. Valid "
                                 "placeholders include: {basename} (filename without extension), {date} (exact time "
                                 "that particular image was made as YYYY-mm-dd_HHMMSS), {exec_time} (the time this "
                                 "script began. Good for a folder name.), {ext} (image format extension)"
                                 " {filepath} (the full path of the input file except the extension), {height} (height "
                                 "of the output image in pixels), {src_ext} (the extension of the input file), "
                                 "{width} (width of the output image in pixels)")
        parser.add_argument('-x', '--max-dim', default=9.0, type=float,
                            help="Limit the size of the mesh to not exceed this length on any axis. Setting it too "
                                 "high will make it too large to fit in the image. Setting it too low will leave a lot "
                                 "of empty margin in the image.")
        parser.add_argument('-c', '--camera-coords', default=",".join(map(str, DEFAULT_CAMERA_COORDS)), type=str,
                            help="Where to position the camera. X,Y,Z separated by commas.")
        parser.add_argument('-r', '--camera-rotation', default=",".join(map(str, DEFAULT_CAMERA_ROTATION)), type=str,
                            help='The rotation of the camera in degrees for X,Y,Z.')
        parser.add_argument('-m', '--material', type=str,
                            help="One or more names of materials to apply to the mesh before rendering. "
                                 "Material must exist in your default scene already. Separate names by comma.")

        args = parser.parse_args(sys.argv[index:]).__dict__

        # we're going to fix up the dimensions list real quick
        dimensions = []
        for d in args['dimensions']:
            split = d.split(',')  # is a pair
            if len(split) == 1:  # is it just one element?
                dimensions.append(split[0])  # just put the one element in there
            else:
                dimensions.append(split)  # put it in as an (width, height) pair
        args['dimensions'] = dimensions  # replace with the new list we just made

        args['camera_coords'] = [float(c) for c in args['camera_coords'].split(',')]
        args['camera_rotation'] = [float(c) for c in args['camera_rotation'].split(',')]

        return args

    @classmethod
    def save_image(cls, filepath, width, height=None, file_format='png', antialiasing_samples=16,
                   resolution_percentage=100, jpeg_quality=100, pngcompression=100, color_depth=8,
                   allow_transparency=True, watermark=None, watermark_size=18, watermark_metadata=False,
                   watermark_foreground=WATERMARK_WHITE, watermark_background=WATERMARK_TRANSLUCENT_BLACK):

        logging.info("Saving image %s", filepath)
        logging.debug("... with arguments: %s" % str(locals()))
        render = data.scenes['Scene'].render
        render.filepath = filepath
        if antialiasing_samples:
            print(True)
            render.simplify_gpencil_antialiasing = True
            #render.antialiasing_samples = str(antialiasing_samples)
        else:
            render.use_antialiasing = False
        render.resolution_percentage = resolution_percentage
        render.resolution_x = width
        render.resolution_y = height if height is not None else width
        settings = render.image_settings
        try:
            settings.file_format = cls.IMAGE_FORMATS[file_format]
        except KeyError:
            raise ValueError("%s was not an expected image format." % file_format)
        settings.quality = jpeg_quality
        settings.compression = pngcompression
        settings.color_depth = str(color_depth)
        color_mode = 'RGBA' if allow_transparency and file_format == 'png' else 'RGB'
        settings.color_mode = color_mode
        render.use_stamp = watermark is not None
        if watermark:
            render.stamp_background = watermark_background
            render.stamp_foreground = watermark_foreground
            render.stamp_font_size = watermark_size
            for attr in dir(render):
                if attr.startswith('use_stamp_'):
                    setattr(render, attr, watermark_metadata)
            render.use_stamp_note = True
            render.stamp_note_text = watermark
        ops.render.render(write_still=True)


class JobTemplate(object):
    def __init__(self, dimensions, output_template, image_format='png', jpeg_quality=80):

        if not image_format:
            image_format = 'png'
        try:
            width, height = dimensions  # a tuple with width and height
        except ValueError:
            width = height = dimensions  # just a single dimension (square output image)
        self.width = int(width)
        self.height = int(height)
        self.output_template = output_template
        self.image_format = image_format
        self.jpeg_quality = jpeg_quality

    def get_output_path(self, input_filepath, exec_time=None):

        date = datetime.now().strftime('%Y-%m-%d_%H%M%S')  # the current time in the format `YYYY-mm-dd_HHMMSS`
        if not exec_time:
            exec_time = date
        filepath, src_ext = os.path.splitext(input_filepath)
        basename = os.path.basename(filepath)
        ext = self.image_format.lower()
        return self.output_template.format(basename=basename, date=date, exec_time=exec_time, ext=ext,
                                           filepath=filepath, height=self.height, src_ext=src_ext, width=self.width)

    def __str__(self):
        return "JobTemplate(%s)" % str(self.__dict__)


def delete_object_by_name(name, ignore_errors=False):

    try:
        logging.debug("Attempting to delete object '%s'" % name)
        obj = data.objects[name]
    except KeyError as ex:
        if ignore_errors:  # are we ignoring errors?
            logging.debug("Didn't delete '%s'. Probably didn't exist. Error ignored." % name)
            return False  # just report that we weren't successful
        raise ex  # object doesn't exist so raise this exception
    ops.object.select_all(action='DESELECT')
    obj.select_set(state=True)
    status = ops.object.delete()
    success = 'FINISHED' in status
    if success:
        logging.debug("Successfully deleted '%s'", name)
    else:
        logging.debug("'%s' couldn't be deleted. Status was: %s", name, status)
    return success


def scale_mesh(mesh, max_dim=9.0):

    logging.debug("Scaling mesh %s to a maximum of %s in any direction" % (mesh.name, max_dim))
    max_length = max(mesh.dimensions)
    if max_length == 0:
        logging.debug("No scaling for %s because its dimensions are %s" % (mesh.name, repr(mesh.dimensions)))
        return  # skip scaling
    scale_factor = 1 / (max_length / max_dim)
    mesh.scale = (scale_factor, scale_factor, scale_factor)
    x, y, z = [i for i in mesh.dimensions]
    new_dimensions = "X=%s, Y=%s, Z=%s" % (x, y, z)
    logging.debug("Scale factor for mesh %s is %s. Its new dimensions are %s",
                  mesh.name, scale_factor, [i for i in new_dimensions])


def set_camera(x=0, y=0, z=10, rotation_x=0, rotation_y=0, rotation_z=0, camera_name='Camera'):

    camera = data.objects[camera_name]
    print(x, y, z)
    camera.location = (x, y, z)
    rx, ry, rz = math.radians(rotation_x), math.radians(rotation_y), math.radians(rotation_z)
    camera.rotation_euler = (rx, ry, rz)
    print(camera.rotation_euler)

def set_light(x=0, y=0, z=0):
    view_layer = bpy.context.view_layer
    light_data = bpy.data.lights.new(name="New Light", type='SUN')
    light_object = bpy.data.objects.new(name="New Light", object_data=light_data)
    view_layer.active_layer_collection.collection.objects.link(light_object)
    light_object.data.energy = 100
    light_object.location = (x, y, z)
    light_object.select_set(True)
    view_layer.objects.active = light_object



if __name__ == "__main__":  # start execution here
    old_level = logging.getLogger().level
    cliargs = Mesh2Img.command_line()
    Mesh2Img(**cliargs).start()  # pass in all the paths given on the command line
    logging.getLogger().setLevel(old_level)