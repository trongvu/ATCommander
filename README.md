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

## Update for ROOTED master device  
If you have ROOTED device acting as Master device, so there is no condition for slave device.  
This app can work even slave one is user device, without USB debugging option and locked device also.  
I committed the change to handle this case, [ee7af53] (https://github.com/trongvu/ATCommander/tree/ee7af5318cf8c2fe56b67cd5fce2eafc63eeffe2)  
This commit will need configuration_switch binary preloaded on your master device (ROOTED one).  
Refer to [libusb](https://github.com/trongvu/libusb) for more details.  

## Test Command
| AT Command | Description |
| ------------- | ------------- |
| ATZ  | Reset modem  |
| ATE1  | Enable echo  |
| ATE0  | Disable echo  |
| AT+GMM  | Get model name |
| AT+GSN  | Get IMEI number |
