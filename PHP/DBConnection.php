<?php

class DBConnection {

    protected $host = "localhost";
    protected $host_type = "mysql";
    protected $username = "root";
    protected $password = "";
    protected $database = "rubis";
    protected $connection = NULL;
    protected $status = FALSE;

    function connect() {
        try {
            $this->connection = new PDO("$this->host_type:host=$this->host;dbname=$this->database", $this->username, $this->password);
            $this->status = TRUE;
        } catch (PDOException $e) {
            echo $e->getMessage();
            $this->connection = NULL;
            $this->status = FALSE;
        }
    }

    function close_connection() {
        $this->connection = NULL;
        $this->status = FALSE;
    }

    function get_connection() {
        return $this->connection;
    }

    function get_status() {
        return $this->status;
    }

    function get_host_type(){
        return $this->host_type;
    }

}
