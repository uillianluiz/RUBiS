# RUBiS Emulator setup

### SSH password-less
When you will execute the emulator, you will be asked for logging in the remote server. Thus, you may want to configure the SSH to be able to login without password.

Follow the steps:

* `ssh-keygen` - go through the configuration
* `ssh-copy-id username@hostname` - Copy key to all hosts, including localhost, changing username and hostname to the correct ones.

### Necessary packages

When executing the emulator, RUBiS should generate a detailed monitoring information from resource utilization in the hosts.

These are the necessary packages:

* Gnuplot:
  * `sudo apt-get install gnuplot`
* awk
  * `sudo apt-get install gawk`
* Sysstat (required in both client and server hosts)
  * `sudo apt-get install sysstat`

## Necessary permissions

You must give permission to execute the `sh` and `awk` files:

* `cd Client/bench`
* `chmod +x *.sh`
* `chmod +x *.awk`
