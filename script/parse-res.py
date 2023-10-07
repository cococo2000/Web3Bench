# -*- coding: utf-8 -*-

import pandas as pd
from collections import OrderedDict
import csv

#________________________________________modify section_______________________________________________ 
# data = "test"
data = ""
#________________________________________modify section_______________________________________________  

# Output the results to a CSV file
export_csv_file = data + "res.csv"

type_category = {
    "R1" : 1 , "W4" : 4 , "W6" : 6 , "W11": 11, "W12": 12, "W13": 13, "W14": 14, 
    "R21": 21, "R22": 22, "R23": 23, "R24": 24,
    "R31": 31, "R32": 32, "R33": 33, "R34": 34, "R35": 35, "W2": 2, "W3": 3, "W51": 51, "W52": 52
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
    df = pd.read_csv(file, names=["Transaction Type Index","Transaction Name","Start Time (microseconds)","Latency (microseconds)","Worker Id (start number)","Phase Id (index in config file)"], skiprows=1)
    for index, row in df.iterrows():
        type_name = row["Transaction Name"]
        l_time = row["Latency (microseconds)"]
        category = type_category.get(type_name)
        if category:
            if category not in load_stats:
                load_stats[category] = {"Total Latency": 0, "Number of Requests": 0}
            load_stats[category]["Total Latency"] += l_time
            load_stats[category]["Number of Requests"] += 1

# Open the CSV file and write the header
with open(export_csv_file, mode="w", newline="") as file:
    fieldnames = ["Type Name", "Total Latency", "Number of Requests", "Avg Latency(us)", "Avg Latency(s)", "Requests per Hour"]
    writer = csv.DictWriter(file, fieldnames=fieldnames)
    writer.writeheader()

    for type_name, category in type_category.items():
        stats = load_stats.get(category)
        if stats:
            total_time = stats["Total Latency"]
            num = stats["Number of Requests"]
            avg_time_us = total_time / num if num != 0 else 0
            avg_time_s = avg_time_us / 1000000  # Convert microseconds to seconds
            requests_per_hour = 3600.0 / avg_time_s   # Calculate this value
            writer.writerow({
                "Type Name": type_name,
                "Total Latency": total_time,
                "Number of Requests": num,
                "Avg Latency(us)": avg_time_us,
                "Avg Latency(s)": avg_time_s,
                "Requests per Hour": requests_per_hour
            })

print(f"Data has been exported to '{export_csv_file}'")
