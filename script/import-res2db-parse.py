# -*- coding: utf-8 -*-
import mysql.connector
import pandas as pd
import os
import socket
import time
import json
import xml.etree.ElementTree as ET
import csv
from tqdm import tqdm

# Get the current machine's hostname
hostname = socket.gethostname()

# MySQL database connection configuration
db_config = {
    "host": "127.0.0.1",    # Replace with your MySQL host name
    "port": 4000,           # Replace with your MySQL port
    "user": "root",         # Replace with your database username
    "password": "",         # Replace with your database password
    "database": "web3bench" # Replace with your database name
}

# Connect to the MySQL database
conn = mysql.connector.connect(**db_config)
# Get a database cursor
cursor = conn.cursor()

# Create the table SQL statement, including the new 'hostname' column
create_table_sql = '''
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
# Execute the create table SQL statement
cursor.execute(create_table_sql)
# Commit the changes
conn.commit()

# Get current batch id from the database
cursor.execute('SELECT MAX(batch_id) FROM res_table;')
result = cursor.fetchone()
batch_id = result[0] + 1 if result is not None else 1

# Specify the directory containing CSV files
csv_directory = '../results/'
# Iterate through all CSV files in the directory and insert into the database
for csv_file in os.listdir(csv_directory):
    if csv_file.endswith('.csv'):
        csv_path = os.path.join(csv_directory, csv_file)
        # Read the CSV file content
        df = pd.read_csv(csv_path)
        # Add the 'hostname' column
        df['hostname'] = hostname
        # Insert the data into the database
        chunk_size = 1000  # Adjust the chunk size as needed
        for i in tqdm(range(0, len(df), chunk_size), desc="Inserting from " + csv_path):
            chunk = df.iloc[i:i + chunk_size]
            insert_sql = '''
            INSERT INTO res_table 
            (batch_id, hostname, txn_type_index, txn_name, start_time_s, latency_us, worker_id, phase_id)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s);
            '''
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
            cursor.executemany(insert_sql, values)
            # Commit the inserted data after each chunk
            conn.commit()

# Get the test time from the xml config file
xml_config_file = "../config/runthread1.xml"
tree = ET.parse(xml_config_file)
time_element = tree.find(".//time")
assert time_element is not None, "The time in config.xml should not be None"
test_time = int(time_element.text)
print(f"Get the test time from the xml config file {xml_config_file}")
print(f"Test time: {test_time} minutes")

# The latency limit of each transaction type
# The key is the transaction type name, and the value is the latency limit in seconds
latency_limit_file = open("latency-limit.json", "r")
latency_limit = json.load(latency_limit_file)

# Check if all nodes have finished inserting data by counting the number of rows in the database table
# The total number of rows will not increase if all nodes have finished inserting data
def get_count():
    cursor.execute('SELECT COUNT(*) FROM res_table;')
    result = cursor.fetchone()
    assert result is not None, "The result of count(*) should not be None"
    return result[0]

pre_count = get_count()
while True:
    time.sleep(10)
    current_count = get_count()
    if current_count == pre_count:
        break
    else:
        pre_count = current_count
print('All nodes have finished inserting data.')

# Get the summary of the results from the database
# The results are grouped by transaction name
select_sql = '''
SELECT txn_name, 
    SUM(latency_us) / 1000000 AS total_latency_s,
    COUNT(*) as txn_count,
    AVG(latency_us / 1000000) AS average_latency_s,
    APPROX_PERCENTILE(latency_us / 1000000, 99) AS p99_latency_s,
    COUNT(*) / %s AS qps,
    COUNT(*) / %s AS tps,
    EXP(AVG(LN(latency_us / 1000000))) AS geometric_mean_latency_s,
    FROM_UNIXTIME(MIN(start_time_s)) AS start_time,
    FROM_UNIXTIME(MAX(start_time_s + latency_us / 100000)) AS end_time
FROM res_table
WHERE batch_id = %s
GROUP BY txn_name
ORDER BY txn_name;
'''
cursor.execute(select_sql, (test_time * 60, test_time * 60, batch_id))
sum_stats = {}
for row in cursor.fetchall():
    type_name = row[0]
    sum_stats[type_name] = {
        "Total Latency(s)": row[1], 
        "Number of Requests": row[2], 
        "QPS": row[5], 
        "TPS": row[6], 
        "P99 Latency(s)": row[4], 
        "Geometric Mean Latency(s)": row[7], 
        "Avg Latency(s)": row[3], 
        "Avg Latency Limit(s)": "N/A" if latency_limit[type_name] == 0 else str(latency_limit[type_name]),
        "Pass/Fail": "N/A" if latency_limit[type_name] == 0 else "Pass" if row[4] <= latency_limit[type_name] else "Fail",
        "Start Time": row[8],
        "End Time": row[9]
    }

# Calculate the total statistics
select_total_sql = '''
SELECT 
    SUM(latency_us) / 1000000 AS total_latency_s,
    COUNT(*) as txn_count,
    AVG(latency_us / 1000000) AS average_latency_s,
    APPROX_PERCENTILE(latency_us / 1000000, 99) AS p99_latency_s,
    COUNT(*) / %s AS qps,
    COUNT(*) / %s AS tps,
    EXP(AVG(LN(latency_us / 1000000))) AS geometric_mean_latency_s,
    FROM_UNIXTIME(MIN(start_time_s)) AS start_time,
    FROM_UNIXTIME(MAX(start_time_s + latency_us / 100000)) AS end_time
FROM res_table
WHERE batch_id = %s;
'''
cursor.execute(select_total_sql, (test_time * 60, test_time * 60, batch_id))
total_row = cursor.fetchone()
assert total_row is not None, "The total statistics should not be None"
sum_stats["Total"] = {
    "Total Latency(s)": total_row[0], 
    "Number of Requests": total_row[1], 
    "QPS": total_row[4], 
    "TPS": total_row[5], 
    "P99 Latency(s)": total_row[3], 
    "Geometric Mean Latency(s)": total_row[6], 
    "Avg Latency(s)": total_row[2], 
    "Avg Latency Limit(s)": "N/A", 
    "Pass/Fail": "N/A", 
    "Start Time": total_row[7], 
    "End Time": total_row[8]
}

# Output the summary of the results to database table
# Create the table SQL statement
create_table_sql = '''
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
# Execute the create table SQL statement
cursor.execute(create_table_sql)
# Commit the changes
conn.commit()

# The output CSV file name
export_csv_file = f"./summary-{batch_id}.csv"

# Export the summary of the results to a CSV file
with open(export_csv_file, mode="w", newline="") as file:
    fieldnames = [
        "Type Name", 
        "Total Latency(s)", 
        "Number of Requests", 
        "QPS", 
        "TPS", 
        "P99 Latency(s)", 
        "Geometric Mean Latency(s)", 
        "Avg Latency(s)", 
        "Avg Latency Limit(s)", 
        "Pass/Fail", 
        "Start Time", 
        "End Time"
    ]
    writer = csv.DictWriter(file, fieldnames=fieldnames)
    writer.writeheader()
    # Print the summary of the results to the console
    print("Summary of the results:")
    print(f"{'Type Name':<10} {'Total Latency(s)':<20} {'Number of Requests':<20} {'QPS':<10} {'TPS':<10} {'P99 Latency(s)':<20} {'Geometric Mean Latency(s)':<30} {'Avg Latency(s)':<20} {'Avg Latency Limit(s)':<20} {'Pass/Fail':<10} {'Start Time':<30} {'End Time':<30}")
    for type_name, stats in sum_stats.items():
        # Write the summary of the results to the CSV file
        writer.writerow({
            "Type Name": type_name,
            "Total Latency(s)": stats["Total Latency(s)"],
            "Number of Requests": stats["Number of Requests"],
            "QPS": stats["QPS"],
            "TPS": stats["TPS"],
            "P99 Latency(s)": stats["P99 Latency(s)"],
            "Geometric Mean Latency(s)": stats["Geometric Mean Latency(s)"],
            "Avg Latency(s)": stats["Avg Latency(s)"],
            "Avg Latency Limit(s)": stats["Avg Latency Limit(s)"],
            "Pass/Fail": stats["Pass/Fail"],
            "Start Time": stats["Start Time"],
            "End Time": stats["End Time"]
        })
        # Print the summary of the results to the console
        print(f"{type_name:<10} {stats['Total Latency(s)']:<20} {stats['Number of Requests']:<20} {stats['QPS']:<10} {stats['TPS']:<10} {stats['P99 Latency(s)']:<20} {stats['Geometric Mean Latency(s)']:<30} {stats['Avg Latency(s)']:<20} {stats['Avg Latency Limit(s)']:<20} {stats['Pass/Fail']:<10} {str(stats['Start Time']):<30} {str(stats['End Time']):<30}")
        # Insert the data into the database (batch_id and txn_name are primary keys)
        insert_sql = '''
        INSERT INTO sum_table
        (batch_id, txn_name, total_latency_s, txn_count, average_latency_s, p99_latency_s, qps, tps, geometric_mean_latency_s, avg_latency_limit_s, pass_fail, start_time, end_time)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s);
        '''
        # try to insert the data
        cursor.execute(insert_sql, (
            batch_id,
            type_name,
            stats["Total Latency(s)"],
            stats["Number of Requests"],
            stats["Avg Latency(s)"],
            stats["P99 Latency(s)"],
            stats["QPS"],
            stats["TPS"],
            stats["Geometric Mean Latency(s)"],
            stats["Avg Latency Limit(s)"],
            stats["Pass/Fail"],
            stats["Start Time"],
            stats["End Time"]
        ))
        # Commit the inserted or updated data
        conn.commit()
print(f"Export the summary of the results to {export_csv_file}\n")

# Close the database connection
conn.close()
