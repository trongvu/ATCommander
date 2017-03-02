# ATCommander [![Build Status](https://travis-ci.org/trongvu/ATCommander.svg?branch=master)](https://travis-ci.org/trongvu/ATCommander)
This is android Application which helps to send AT command to slave device (connected via OTG cable).  
It supports to connect directly to modem device without unlock the phone.  

## Requirment
Slave device must be Samsung Android device.  
Slave device has Developer mode & USB debugging ON. 
We also can connect in case of Developer mode & USB debugging are not enable, but Master device should have root access.  

## How to use
1. Install application on Master device
2. Connect slave device via OTG cable
3. Confirm access permission, then wait
4. Send test AT command below

## Test Command
| AT Command | Description |
| ------------- | ------------- |
| ATZ  | Reset modem  |
| ATE1  | Enable echo  |
| ATE0  | Disable echo  |
| AT+GMM  | Get model name |
| AT+GSN  | Get IMEI number |
