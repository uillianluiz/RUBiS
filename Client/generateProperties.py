import os
current_path = os.path.dirname(os.path.realpath(__file__))
parrent_path = os.path.abspath(os.path.join(current_path, os.pardir))

phpHost = raw_input("PHP server host [localhost]: ")
if phpHost == '':
    phpHost = "localhost"

dbHost = raw_input("Database server host [localhost]: ")
if dbHost == '':
    dbHost = "localhost"

properties = """# HTTP server information
httpd_hostname = {0}
httpd_port = 80

# Precise which version to use. Valid options are : PHP, Servlets, EJB
httpd_use_version = PHP

ejb_server = none
ejb_html_path = /ejb_rubis_web
ejb_script_path = /ejb_rubis_web/servlet

servlets_server = none
servlets_html_path = /Servlet_HTML
servlets_script_path = /servlet

php_html_path = /PHP
php_script_path = /PHP

# Workload: precise which transition table to use
workload_remote_client_nodes =
workload_remote_client_command = /usr/local/java/jdk1.3.1/bin/java -classpath RUBiS edu.rice.rubis.client.ClientEmulator
workload_number_of_clients_per_node = 100

workload_transition_table = {2}/workload/transitions.txt
workload_number_of_columns = 27
workload_number_of_rows = 29
workload_maximum_number_of_transitions = 1000
workload_use_tpcw_think_time = yes
workload_number_of_items_per_page = 20
workload_up_ramp_time_in_ms = 300000
workload_up_ramp_slowdown_factor = 2
workload_session_run_time_in_ms = 1800000
workload_down_ramp_time_in_ms = 300000
workload_down_ramp_slowdown_factor = 3


#Database information
database_server = {1}

# Users policy
database_number_of_users = 1000000

# Region & Category definition files
database_regions_file = {2}/database/ebay_regions.txt
database_categories_file = {2}/database/ebay_simple_categories.txt

# Items policy
database_number_of_old_items = 500000
database_percentage_of_unique_items = 80
database_percentage_of_items_with_reserve_price = 40
database_percentage_of_buy_now_items = 10
database_max_quantity_for_multiple_items = 10
database_item_description_length = 7168

# Bids policy
database_max_bids_per_item = 20

# Comments policy
database_max_comments_per_user = 20
database_comment_max_length = 2048


# Monitoring Information
monitoring_debug_level = 0
monitoring_program = /usr/bin/sar
monitoring_options = -n DEV -n SOCK -rubwFB
monitoring_sampling_in_seconds = 5
monitoring_rsh = /usr/bin/ssh
monitoring_scp = /usr/bin/scp
monitoring_gnuplot_terminal = jpeg""".format(phpHost, dbHost, parrent_path)

outputFile = open("rubis.properties", "w")
outputFile.write(properties)
outputFile.close()

print("rubis.properties written")
