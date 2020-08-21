#include <fstream>
#include <iostream>
#include <sstream>
#include <tuple>
#include <vector>

int main(int argc, char **argv) { //TODO: ловить ошибки и исключения
	std::ifstream in(argv[1]);
	std::ofstream out(argv[2]);
	std::stringstream out_ss;
	size_t vertices_count = 0, faces_count = 0;
	size_t parser_faces_index = 0;
	std::string line;
	while (std::getline(in, line) && line != "end_header") {
		std::istringstream iss(line);
		std::string token;
		iss >> token;
		if (token == "element") {
			iss >> token;
			out_ss << "element " << token << " ";
			if (token == "vertex") {
				iss >> vertices_count;
				out_ss << vertices_count;
			} else if (token == "face") {
				iss >> faces_count;
				parser_faces_index = out_ss.tellp();
			}
			out_ss << std::endl;
		} else {
			out_ss << line << std::endl;
		}
	}
	out_ss << line << std::endl;
	while (vertices_count > 0) { //вершины не интересуют
		std::getline(in, line);
		out_ss << line << std::endl;
		vertices_count--;
	}
	std::vector<std::tuple<int, int, int, int, int>> face_vertices(faces_count);
	for (auto &face_vertex : face_vertices) {
		in >> std::get<0>(face_vertex) >> std::get<1>(face_vertex) >> std::get<2>(face_vertex) >>
		std::get<3>(face_vertex) >> std::get<4>(face_vertex);
	}
	in.close();
	std::sort(face_vertices.begin(), face_vertices.end(), [](const auto &tuple1, const auto &tuple2) {
		if (std::get<3>(tuple1) == std::get<3>(tuple2)) {
			if (std::get<1>(tuple1) == std::get<1>(tuple2)) {
				return std::get<2>(tuple1) < std::get<2>(tuple2);
			} else {
				return std::get<1>(tuple1) < std::get<1>(tuple2);
			}
		} else {
			return std::get<3>(tuple1) < std::get<3>(tuple2);
		}
	});
	size_t faces_count_new = 1;
	while (faces_count_new < faces_count &&
	std::get<3>(face_vertices[faces_count_new]) - std::get<3>(face_vertices[0]) < 2000) {
		faces_count_new++;
	}
	faces_count_new = 3000;
	out.write(out_ss.str().c_str(), parser_faces_index);
	out << faces_count_new;
	out.write(out_ss.str().substr(parser_faces_index).c_str(), out_ss.str().length() - parser_faces_index);
	for (size_t index = 0; index < faces_count_new; index++) {
		out << std::get<0>(face_vertices[index]) << ' ' << std::get<1>(face_vertices[index]) << ' ' <<
		std::get<2>(face_vertices[index]) << ' ' << std::get<3>(face_vertices[index]) << ' ' <<
		std::get<4>(face_vertices[index]) << std::endl;
	}
	out.flush();
	out.close();
	return 0;
}
