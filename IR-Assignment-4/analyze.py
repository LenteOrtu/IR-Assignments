import numpy as np
import sys

def string_to_float(data):
    for i in range(1, 5):
        data = data.replace(f"P_{i * 5}", "")
    data = data.replace("map", "").replace("all", "")

    return float(data)

def map_file_to_np(file_path):
    map_values = []
    param_values = []
    with open(file_path, "r") as data:
        for i, line in enumerate(data):
            if i % 12 == 0:
                param_values.append(line.replace("-", "").strip())
            if (i+1) % 12 == 0:
                map_value = string_to_float(line)
                map_values.append(map_value)
    
    return np.array(map_values), np.array(param_values)

def prec_file_to_np(file_path):
    total_prec_values = []
    param_values = []

    with open(file_path, "r") as data:
        lines = [line for line in data]

        for i in range(len(lines) // 48):
            curr_prec_values = []
            curr_prec_params = []
            for j in range(4):
                val = string_to_float(lines[i * 48 + 12 * (j+1) - 1])
                curr_prec_values.append(val)
                curr_prec_params.append(lines[i * 48].replace("-", "").strip())
            total_prec_values.append(np.array(curr_prec_values))
            param_values.append(np.array(curr_prec_params))
    
    return np.array(total_prec_values), np.array(param_values)

def analysis(values_prec, values_map, params_prec, params_map, percentile):
    idxes_prec = np.where(values_prec >= np.percentile(values_prec, percentile, axis=0))
    idxes_map = np.where(values_map >= np.percentile(values_map, percentile, axis=0))
    
    return set(params_prec[idxes_prec]).intersection(set(params_map[idxes_map]))

def main():
    file_name = sys.argv[1]
    percentile = int(sys.argv[2])
    map_values, map_params = map_file_to_np(f"{file_name}_map.txt")
    prec_values, prec_params = prec_file_to_np(f"{file_name}_prec.txt")
    res = analysis(prec_values, map_values, prec_params, map_params, percentile)
    print(f'Results: {res}')

if __name__ == "__main__":
    main()