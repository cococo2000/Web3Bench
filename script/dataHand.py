import pandas as pd
from collections import OrderedDict

# 创建一个字典，将"type name"映射到类别
type_category = {
    "R1" : 1 , "W4" : 4 , "W6" : 6 , "W11": 11, "W12": 12, "W13": 13, "W14": 14, 
    "R21": 21, "R22": 22, "R23": 23, "R24": 24,
    "R31": 31, "R32": 32, "R33": 33, "R34": 34, "R35": 35, "W2": 2, "W3": 3, "W51": 51, "W52": 52
}

# 创建一个字典用于保存每个负载类型的统计结果
load_stats = {}

data = "vmtest-8.2/"
# 指定CSV文件的路径列表
csv_files = [ "/home/gzx/olxpbench/results/" + data + "thread1.csv"
             ,"/home/gzx/olxpbench/results/" + data + "R21.csv"
             ,"/home/gzx/olxpbench/results/" + data + "R22.csv"
             ,"/home/gzx/olxpbench/results/" + data + "R23.csv"
             ,"/home/gzx/olxpbench/results/" + data + "R24.csv"
             ,"/home/gzx/olxpbench/results/" + data + "thread2.csv"]

# 读取CSV文件并按"type name"分类求和
for file in csv_files:
    df = pd.read_csv(file, names=["Transaction Type Index","Transaction Name","Start Time (microseconds)","Latency (microseconds)","Worker Id (start number)","Phase Id (index in config file)"], skiprows=1)  # 读取CSV文件为DataFrame
    for index, row in df.iterrows():
        type_name = row["Transaction Name"]
        l_time = row["Latency (microseconds)"]
        category = type_category.get(type_name)
        if category:
            # 对于每个负载类型，如果字典中还没有对应的条目，则初始化它
            if category not in load_stats:
                load_stats[category] = {"Latency Time Total": 0, "Number of Occurrences": 0}
            # 更新该负载类型的统计结果
            load_stats[category]["Latency Time Total"] += l_time
            load_stats[category]["Number of Occurrences"] += 1

# 输出每个负载类型的统计结果
print("Type Name, Latency Time Total, Number of Occurrences, Avg Time")
for type_name, category in type_category.items():
    stats = load_stats.get(category)
    if stats:
        total_time = stats["Latency Time Total"]
        num = stats["Number of Occurrences"]
        avg_time = total_time / num if num != 0 else 0
        print(f"{type_name:5},{total_time:15},{num:10},{avg_time:15.3f}")
    else:
        print(f"{type_name:<8},0                 ,0                    ,0.000         ")
