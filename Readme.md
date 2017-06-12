## RUBiS

This is a modified implementation of RUBiS (RUBiS: Rice University Bidding System) that supports PHP7 and PDO connections.

More information on the RUBiS project, and the original source code is found [here](http://rubis.ow2.org/).


Requirements:

1. PHP7 or PHP5
2. MySQL (Other versions, including SQL Server, may be supported)
3. Apache

Installation steps:

1. Clone this repository
    * `git clone https://github.com/uillianluiz/RUBiS.git`
2. Install MySQL
    * `sudo apt-get install mysql-server`
3. Install Apache2
    * `sudo apt-get install apache2`
3. Install PHP
    * `sudo apt-get install php libapache2-mod-php php-mcrypt php-mysql`
    * If you want to install PHP5, check [here](https://askubuntu.com/questions/761713/how-can-i-downgrade-from-php-7-to-php-5-6-on-ubuntu-16-04/762161#762161) for a detailed instructions.

Configuration steps:

1. Import rubis tables and default data to the database `rubis`
    * `cd database`
    * `mysql -uroot rubis -p < rubis.sql`
    * `mysql -uroot rubis -p < categories.sql`
    * `mysql -uroot rubis -p < regions.sql`
2. Modify the database connection configuration
    * `nano PHP/DBConnection.php`
3. Copy the PHP folder to the apache location
    * `cp -r PHP/ /var/www/html/`
4. Configure the rubis client properties
    * `cd Client`
    * `python generateProperties.py`
5. Initializate the database
    * `cd Cliente`
    * `make initDB PARAM="all"`

Emulator execution:

* `make emulator`

Extra options:

* The regions and categories may be altered. Check file `database/README` for more information.
* The workload may be modified by changing the file `Client/rubis.properties`.
* You can use the PHP5 version. Just copy the `PHP5.6` folder instead, renaming it to `PHP`. Also, modify the database connection in the file `PHPprinter.php`
