# -*- coding: utf-8 -*-

import pandas as pd
import csv
import math

#________________________________________modify section_______________________________________________ 
# data = "test"
data = ""
#________________________________________modify section_______________________________________________  

# Output the results to a CSV file
export_csv_file = data + "res.csv"

type_category = {
    "R1" : 1 , 
    "R21": 21, "R22": 22, "R23": 23, "R24": 24,
    "R31": 31, "R32": 32, "R33": 33, "R34": 34, "R35": 35,
    "W11": 11, "W12": 12, "W13": 13, "W14": 14, 
    "W2": 2, "W3": 3, "W4" : 4,
    "W51": 51, "W52": 52, 
    "W6" : 6
}

load_stats = {}

if data != "":
    data = data + "/"

print("Results directory: results/" + data)
print("Parsing results...")
# Read results from the CSV file
csv_files = [ "../results/" + data + "thread1.csv",
              "../results/" + data + "R21.csv",
              "../results/" + data + "R22.csv",
              "../results/" + data + "R23.csv",
              "../results/" + data + "R24.csv",
              "../results/" + data + "thread2.csv"
            ]

for file in csv_files:
    df = pd.read_csv(file, names=[
        "Transaction Type Index",
        "Transaction Name",
        "Start Time (microseconds)",
        "Latency (microseconds)",
        "Worker Id (start number)",
        "Phase Id (index in config file)"
        ], skiprows=1)
    for index, row in df.iterrows():
        type_name = row["Transaction Name"]
        l_time = row["Latency (microseconds)"]
        category = type_category.get(type_name)
        if category:
            if category not in load_stats:
                load_stats[category] = {"Total Latency": 0, "Number of Requests": 0, "Latencies": []}
            load_stats[category]["Total Latency"] += l_time
            load_stats[category]["Number of Requests"] += 1
            load_stats[category]["Latencies"].append(l_time)

# Open the CSV file and write the header
with open(export_csv_file, mode="w", newline="") as file:
    fieldnames = [
        "Type Name", 
        "Total Latency", 
        "Number of Requests", 
        "QPS", 
        "TPS", 
        "Avg Latency(us)", 
        "Avg Latency(s)", 
        "P99 Latency", 
        "Geometric Mean"
    ]
    writer = csv.DictWriter(file, fieldnames=fieldnames)
    writer.writeheader()

    for type_name, category in type_category.items():
        stats = load_stats.get(category)
        if stats:
            total_time = stats["Total Latency"]
            num = stats["Number of Requests"]
            latencies = stats["Latencies"]
            avg_time_us = total_time / num if num != 0 else 0
            avg_time_s = avg_time_us / 1000000  # Convert microseconds to seconds
            qps = num / avg_time_s
            tps = qps
            p99_latency = sorted(latencies)[int(0.99 * len(latencies))] / 1000000 # 99th percentile latency
            geometric_mean = math.exp(sum(math.log(lat) for lat in latencies) / len(latencies)) / 1000000 # Geometric mean of latencies
            writer.writerow({
                "Type Name": type_name,
                "Total Latency": total_time,
                "Number of Requests": num,
                "QPS": qps,
                "TPS": tps,
                "Avg Latency(us)": avg_time_us,
                "Avg Latency(s)": avg_time_s,
                "P99 Latency": p99_latency,
                "Geometric Mean": geometric_mean
            })

# Calculate the total statistics
total_total_latency = sum(stats["Total Latency"] for stats in load_stats.values())
total_num_requests = sum(stats["Number of Requests"] for stats in load_stats.values())
total_latencies = [lat for stats in load_stats.values() for lat in stats["Latencies"]]
total_avg_time_us = total_total_latency / total_num_requests if total_num_requests != 0 else 0
total_avg_time_s = total_avg_time_us / 1000000
total_qps = total_num_requests / 3600
total_tps = total_qps
total_p99_latency = sorted(total_latencies)[int(0.99 * len(total_latencies))] / 1000000
total_geometric_mean = math.exp(sum(math.log(lat) for lat in total_latencies) / len(total_latencies)) / 1000000

# Append the total data to the CSV file
with open(export_csv_file, mode="a", newline="") as file:
    writer = csv.DictWriter(file, fieldnames=fieldnames)
    writer.writerow({
        "Type Name": "Total",
        "Total Latency": total_total_latency,
        "Number of Requests": total_num_requests,
        "QPS": total_qps,
        "TPS": total_tps,
        "Avg Latency(us)": total_avg_time_us,
        "Avg Latency(s)": total_avg_time_s,
        "P99 Latency": total_p99_latency,
        "Geometric Mean": total_geometric_mean
    })

print(f"Data has been exported to '{export_csv_file}'")
