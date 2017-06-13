## RUBiS

This is a modified implementation of RUBiS (RUBiS: Rice University Bidding System) that supports PHP7, PDO connections, and faster initialization of the database (it takes hours instead of days).

More information on the RUBiS project, and for the original source code, check [here](http://rubis.ow2.org/).

Requirements:

1. PHP7
2. MySQL (Other database servers may be supported. They were not tested, though)
3. Apache
4. Java (for building and executing the client)
5. Python (optional, for setting up the `rubis.properties` file)

Installation steps:

1. Clone this repository
    * `git clone https://github.com/uillianluiz/RUBiS.git`
2. Install MySQL
    * `sudo apt-get install mysql-server`
3. Install Apache2
    * `sudo apt-get install apache2`
3. Install PHP7
    * `sudo apt-get install php libapache2-mod-php php-mcrypt php-mysql`
    * If later on you have MySQL PDO driver problems, check [this thread out](https://stackoverflow.com/a/42929132).
4. Install Java
    * `sudo apt-get install default-jdk`
5. Install Python (optional)
    * `sudo apt-get install python`

Configuration steps:

1. Create a rubis database
    * `mysql -uroot -p`
    * `CREATE DATABASE rubis;`
2. Import rubis tables and default data to the database
    * `cd database`
    * `mysql -uroot rubis -p < rubis.sql`
    * `mysql -uroot rubis -p < categories.sql`
    * `mysql -uroot rubis -p < regions.sql`
4. Modify the database connection configuration
    * `nano PHP/DBConnection.php`
5. Copy the PHP folder to the apache location
    * `sudo cp -r PHP/ /var/www/html/`
6. Go to [this url](http://localhost/PHP/BrowseCategories.php) and check if it works fine.
6. Configure the rubis client properties (optional, you may manually edit the `rubis.properties` file)
    * `cd Client`
    * `python generateProperties.py`
7. Initialize the database
    * Updated and faster method:
        * `cd Client`
        * Modify your database connection in the file (lines 24, 26 and 27):
          * `nano edu/rice/rubis/client/InitDBSQL.java`
        * `make client`
        * `make initDBSQL PARAM="all"`
    * Original method:
        * `cd Client`
        * `make client`
        * `make initDB PARAM="all"`

Emulator execution:

* You may modify the `workload_transition_table` or other configurations on the `rubis.properties` file.
* To execute the benchmark:
    * `make emulator`

Extra options:

* The regions and categories may be altered. Check file `database/README` for more information.
