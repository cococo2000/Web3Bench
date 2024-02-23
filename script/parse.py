# -*- coding: utf-8 -*-
import pandas as pd
import os
import csv
import math
import json
import defusedxml.ElementTree as ET
from tqdm import tqdm
import mysql.connector
import argparse
import socket
from datetime import datetime

# MySQL database connection configuration
db_config = {
    "host": "127.0.0.1",    # Replace with your MySQL host name
    "port": 4000,           # Replace with your MySQL port
    "user": "root",         # Replace with your database username
    "password": "",         # Replace with your database password
    "database": "web3bench" # Replace with your database name
}
# The number of rows to insert at a time
chunk_size = 10000  # Adjust the chunk size as needed

# The output CSV file name
fieldnames = [
    "Type Name", 
    "Total Latency(s)", 
    "Number of Requests", 
    "QPS", 
    "TPS", 
    "P99 Latency(s)", 
    "Geometric Mean Latency(s)", 
    "Avg Latency(s)", 
    "Avg Latency Limit", 
    "Pass/Fail", 
    "Start Time",
    "End Time"
]

def get_args():
    parser = argparse.ArgumentParser()
    # --datadir: the directory of the original csv data files
    parser.add_argument("--datadir", type=str, default="", help="The directory of the original csv data files. If it is empty, the script will parse all the csv files in the results directory. If it is not empty, the script will parse all the csv files in ../results/<datadir>")
    # --exportcsv: the name of the exported csv file, default is res
    parser.add_argument("--exportcsv", type=str, default="summary", help="The name of the exported csv file, default is summary. The name of the exported csv file is <exportcsv>.csv, and it will be exported to the current directory")
    # --exportdb: if it is empty, the script will not export the data to the MySQL database
    # If it is not empty, the script will export the data to the MySQL database.
    # The value of --exportdb can be sum or all
    # sum: export the sum of the data to the MySQL database
    # all: export all the data (including the original data and the sum of the data) to the MySQL database
    parser.add_argument("--exportdb", type=str, default="", help="If it is empty, the script will not export the data to the MySQL database. If it is not empty, the script will export the data to the MySQL database. The value of --exportdb can be sum or all. sum: export the sum of the data to the MySQL database. all: export all the data (including the original data and the sum of the data) to the MySQL database")
    # --testtime: the test time in minutes, default is 0
    parser.add_argument("--testtime", type=int, default=0, help="The test time in minutes, default is 0. If it is 0, the script will get the test time from the xml config file. If it is not 0, the script will use the value as the test time")
    args = parser.parse_args()
    return args

# Get the test time in minutes, used to calculate the QPS and TPS
# Get the test time from the xml config file
def get_test_time():
    xml_config_file = "../config/runthread1.xml"
    tree = ET.parse(xml_config_file)
    time_element = tree.find(".//time")
    assert time_element is not None, "The time in config.xml should not be None"
    test_time = int(time_element.text)
    print(f"Get the test time from the xml config file {xml_config_file}")
    print(f"Test time: {test_time} minutes")
    return test_time
# # Get the test time from the shell
# def get_test_time_shell():
#     print("Please enter the test time in minutes:")
#     test_time = int(input())
#     print(f"Test time: {test_time} minutes")
#     return test_time

# Get the latency limit of each transaction type
# The key is the transaction type name, and the value is the latency limit in seconds
def get_latency_limit():
    latency_limit_file = open("latency-limit.json", "r")
    return json.load(latency_limit_file)

# Get all original latency data from the csv files
def get_original_data(csv_directory):
    stats = {}
    print("Results directory: " + csv_directory)
    print("Parsing results...")
    for file in os.listdir(csv_directory):
        if file.endswith(".csv"):
            file = os.path.join(csv_directory, file)
            df = pd.read_csv(file)
            for index, row in tqdm(df.iterrows(), desc="Parsing " + file):
                type_name = row["Transaction Name"]
                l_time = row["Latency (microseconds)"] / 1000000 # Convert microseconds to seconds
                start_time = row["Start Time (microseconds)"]
                if l_time == 0: # Ignore latency = 0
                    continue
                if type_name not in stats:
                    stats[type_name] = {"Total Latency": 0, "Number of Requests": 0, "Latencies": [], "Start Time": [], "End Time": []}
                stats[type_name]["Total Latency"] += l_time
                stats[type_name]["Number of Requests"] += 1
                stats[type_name]["Latencies"].append(l_time)
                stats[type_name]["Start Time"].append(start_time)
                stats[type_name]["End Time"].append(start_time + l_time)
    return stats

# Parse all the data
def parse_data(all_stats, latency_limit):
    results = []
    for type_name, latency_limit in latency_limit.items():
        stats = all_stats.get(type_name)
        if stats:
            total_time = stats["Total Latency"]
            num = stats["Number of Requests"]
            latencies = stats["Latencies"]
            avg_time_s = total_time / num if num != 0 else 0
            qps = num / (test_time * 60) # Queries per second
            tps = qps
            p99_latency = sorted(latencies)[int(0.99 * len(latencies))] # 99th percentile latency
            geometric_mean = math.exp(sum(math.log(lat) for lat in latencies) / len(latencies)) # Geometric mean of latencies
            start_time = min(stats["Start Time"])
            start_time = datetime.fromtimestamp(start_time)
            end_time = max(stats["End Time"])
            end_time = datetime.fromtimestamp(end_time)
            # Add the statistics to the results
            results.append({
                "Type Name": type_name,
                "Total Latency(s)": f"{total_time:f}",
                "Number of Requests": num,
                "QPS": f"{qps:f}",
                "TPS": f"{tps:f}",
                "P99 Latency(s)": f"{p99_latency:f}",
                "Geometric Mean Latency(s)": f"{geometric_mean:f}",
                "Avg Latency(s)": f"{avg_time_s:f}",
                "Avg Latency Limit": "N/A" if latency_limit == 0 else f"<={latency_limit}s",
                "Pass/Fail": "N/A" if latency_limit == 0 else "Pass" if avg_time_s <= latency_limit else "Fail", 
                "Start Time": start_time,
                "End Time": end_time
            })
    # Calculate the total statistics
    total_total_latency = sum(stats["Total Latency"] for stats in all_stats.values())
    total_num_requests = sum(stats["Number of Requests"] for stats in all_stats.values())
    total_latencies = [lat for stats in all_stats.values() for lat in stats["Latencies"]]
    total_avg_time_s = total_total_latency / total_num_requests if total_num_requests != 0 else 0
    total_qps = total_num_requests / (test_time * 60) # Queries per second
    total_tps = total_qps
    total_p99_latency = sorted(total_latencies)[int(0.99 * len(total_latencies))] # 99th percentile latency
    total_geometric_mean = math.exp(sum(math.log(lat) for lat in total_latencies) / len(total_latencies))
    total_start_time = min([min(stats["Start Time"]) for stats in all_stats.values()])
    total_start_time = datetime.fromtimestamp(total_start_time)
    total_end_time = max([max(stats["End Time"]) for stats in all_stats.values()])
    total_end_time = datetime.fromtimestamp(total_end_time)
    # Add the total statistics to the results
    results.append({
        "Type Name": "Total",
        "Total Latency(s)": f"{total_total_latency:f}",
        "Number of Requests": total_num_requests,
        "QPS": f"{total_qps:f}",
        "TPS": f"{total_tps:f}",
        "P99 Latency(s)": f"{total_p99_latency:f}",
        "Geometric Mean Latency(s)": f"{total_geometric_mean:f}",
        "Avg Latency(s)": f"{total_avg_time_s:f}",
        "Avg Latency Limit": "N/A",
        "Pass/Fail": "N/A", 
        "Start Time": total_start_time,
        "End Time": total_end_time
    })
    return results

# Export the data to the CSV file
def export_to_csv(export_csv_file, results):
    with open(export_csv_file, mode="w", newline="") as file:
        writer = csv.DictWriter(file, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(results)
    print(f"Data has been exported to '{export_csv_file}'")

# Create the sum table SQL statement
create_sum_table_sql = '''
CREATE TABLE IF NOT EXISTS sum_table (
    batch_id                    BIGINT,
    txn_name                    VARCHAR(10),
    total_latency_s             DECIMAL(20, 6),
    txn_count                   BIGINT,
    average_latency_s           DECIMAL(20, 6),
    p99_latency_s               DECIMAL(20, 6),
    qps                         DECIMAL(20, 6),
    tps                         DECIMAL(20, 6),
    geometric_mean_latency_s    DECIMAL(20, 6),
    avg_latency_limit_s         VARCHAR(10),
    pass_fail                   VARCHAR(10),
    start_time                  timestamp(6),
    end_time                    timestamp(6),
    PRIMARY KEY (batch_id, txn_name)
);
'''
# Create the sum table if it does not exist
def create_sum_table(cursor, conn):
    cursor.execute(create_sum_table_sql)
    conn.commit()

# Insert into the sum table SQL statement
sum_table_insert_sql = '''
INSERT INTO sum_table
    (batch_id, txn_name, total_latency_s, txn_count, average_latency_s, p99_latency_s, qps, tps, geometric_mean_latency_s, avg_latency_limit_s, pass_fail, start_time, end_time)
VALUES
    (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s);
'''
# Export the sum of the data to the MySQL database
def export_sum_to_db(cursor, conn, batch_id, results):
    for result in results:
        cursor.execute(sum_table_insert_sql, 
                        (batch_id, 
                        result["Type Name"], 
                        result["Total Latency(s)"], 
                        result["Number of Requests"], 
                        result["Avg Latency(s)"], 
                        result["P99 Latency(s)"], 
                        result["QPS"], 
                        result["TPS"], 
                        result["Geometric Mean Latency(s)"], 
                        result["Avg Latency Limit"], 
                        result["Pass/Fail"], 
                        result["Start Time"], 
                        result["End Time"]))

# Create the original data table SQL statement, including the new 'hostname' column
create_res_table_sql = '''
CREATE TABLE IF NOT EXISTS res_table (
    batch_id        BIGINT,
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    hostname        VARCHAR(30),
    txn_type_index  BIGINT,
    txn_name        VARCHAR(10),
    start_time_s    DECIMAL(20, 6),
    latency_us      BIGINT,
    worker_id       INT,
    phase_id        INT
);
'''
# Create the original data table if it does not exist
def create_res_table(cursor, conn):
    cursor.execute(create_res_table_sql)
    conn.commit()

# Insert into the original data table SQL statement
res_table_insert_sql = '''
INSERT INTO res_table 
    (batch_id, hostname, txn_type_index, txn_name, start_time_s, latency_us, worker_id, phase_id)
VALUES 
    (%s, %s, %s, %s, %s, %s, %s, %s);
'''
# Convert the original data to the format of the original data table
# Export the original data to the MySQL database
def export_original_to_db(cursor, conn, batch_id, data_directory):
    # Get the current machine's hostname
    hostname = socket.gethostname()
    # Iterate through all CSV files in the directory and insert into the database
    for csv_file in os.listdir(data_directory):
        if csv_file.endswith('.csv'):
            csv_path = os.path.join(data_directory, csv_file)
            # Read the CSV file content
            df = pd.read_csv(csv_path)
            # Add the 'hostname' column
            df['hostname'] = hostname
            # Insert the data into the database
            for i in tqdm(range(0, len(df), chunk_size), desc="Inserting from " + csv_path):
                chunk = df.iloc[i:i + chunk_size]
                # Convert the dataframe to a list of tuples
                values = [(
                    batch_id, 
                    row['hostname'], 
                    row['Transaction Type Index'],
                    row['Transaction Name'], 
                    row['Start Time (microseconds)'],
                    row['Latency (microseconds)'], 
                    row['Worker Id (start number)'],
                    row['Phase Id (index in config file)']
                    ) for _, row in chunk.iterrows()]
                # Execute batch insert
                cursor.executemany(res_table_insert_sql, values)
                # Commit the inserted data after each chunk
                conn.commit()


# Get current batch id from the database
def get_batch_id(cursor):
    cursor.execute('SELECT MAX(batch_id) FROM sum_table;')
    result = cursor.fetchone()
    batch_id = result[0] + 1 if result is not None and result[0] is not None else 1
    return batch_id


# Main function
if __name__ == "__main__":
    args = get_args()
    # Get the test time
    if args.testtime != 0:
        test_time = args.testtime
        print(f"Test time: {test_time} minutes")
    else:
        test_time = get_test_time()
    # Get the latency limit
    latency_limit = get_latency_limit()

    # Get the original data from the csv files
    data_directory = os.path.join("../results/", args.datadir)
    if args.datadir != "":
        assert os.path.exists(data_directory), f"The csv data directory {data_directory} does not exist"
    original_stats = get_original_data(data_directory)

    # Parse the original csv data
    results = parse_data(original_stats, latency_limit)

    # Export the data to the MySQL database
    # Batch id: the number of the results in the database + 1
    batch_id = 0
    if args.exportdb.lower() == "sum" or args.exportdb.lower() == "all":
        # Connect to the MySQL database
        conn = mysql.connector.connect(**db_config)
        # Get a database cursor
        cursor = conn.cursor()
        # Create the sum table if it does not exist
        create_sum_table(cursor, conn)
        # Get the current batch id from the database
        batch_id = get_batch_id(cursor)
        # Insert the sum data into the sum table
        print("Export the sum of the data to the MySQL database")
        export_sum_to_db(cursor, conn, batch_id, results)
        conn.commit()
        if args.exportdb.lower() == "all":
            # Create the original data table if it does not exist
            create_res_table(cursor, conn)
            # Insert the original data into the original data table
            print("Export all the original data to the MySQL database")
            export_original_to_db(cursor, conn, batch_id, data_directory)
    # Export the data to the CSV file
    export_csv_file = args.exportcsv + ".csv"
    if batch_id != 0:
        export_csv_file = args.exportcsv + "_" + str(batch_id) + ".csv"
    export_to_csv(export_csv_file, results)
