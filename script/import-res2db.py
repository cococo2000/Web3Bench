# -*- coding: utf-8 -*-

import mysql.connector
import pandas as pd
import os
import socket

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
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    hostname        VARCHAR(30),
    txn_type_index  BIGINT,
    txn_name        VARCHAR(10),
    start_time_ms   DECIMAL(20, 6),
    latency_ms      BIGINT,
    worker_id       INT,
    phase_id        INT
);
'''

# Execute the create table SQL statement
cursor.execute(create_table_sql)

# Commit the changes
conn.commit()

# Specify the directory containing CSV files
csv_directory = '../results/'

# Iterate through all CSV files in the directory and insert into the database
for csv_file in os.listdir(csv_directory):
    if csv_file.endswith('.csv'):
        csv_path = os.path.join(csv_directory, csv_file)
        
        # Print the CSV file name
        print('Inserting data from ' + csv_path)
        
        # Use Pandas to read the CSV file content
        df = pd.read_csv(csv_path)
        
        # Add the 'hostname' column
        df['hostname'] = hostname
        
        # Insert the data into the database
        for _, row in df.iterrows():
            insert_sql = '''
            INSERT INTO res_table 
            (hostname, txn_type_index, txn_name, start_time_ms, latency_ms, worker_id, phase_id) 
            VALUES (%s, %s, %s, %s, %s, %s, %s);
            '''
            cursor.execute(insert_sql, (
                row['hostname'],
                row['Transaction Type Index'],
                row['Transaction Name'],
                row['Start Time (microseconds)'],
                row['Latency (microseconds)'],
                row['Worker Id (start number)'],
                row['Phase Id (index in config file)']
            ))
        
        # Commit the inserted data
        conn.commit()

# Close the database connection
conn.close()
