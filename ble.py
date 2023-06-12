from bluepy import btle
import sys

# UUID of the service
SERVICE_UUID = "14839ac4-7d7e-415c-9a42-167340cf2339"
# UUID of the write characteristic
WRITE_CHAR_UUID = "8B00ACE7-EB0B-49B0-BBE9-9AEE0A26E1A3"
# UUID of the notify characteristic
NOTIFY_CHAR_UUID = "0734594A-A8E7-4B1A-A6B1-CD5243059A57"
msg = None
class NotificationDelegate(btle.DefaultDelegate):
    def handleNotification(self, cHandle, data):
       global msg
       if len(data.hex()) == 40:
           msg = data.hex()
       if len(data.hex()) == 2 and len(msg) == 40: 
           msg += data.hex()
           pr = int.from_bytes(bytes.fromhex(msg[16:20]), byteorder="little")
           spo2 = bytes.fromhex(msg)[7]
           batt = bytes.fromhex(msg)[14]
           print(f"Received data on handle {cHandle}: msg: {msg}\n")
           print (f'Pulse Rate: {pr} - SPO2: {spo2}% - bat: {batt}%\n')
       dec_msg = "" 
       bytes_obj = bytes.fromhex(data.hex())
       for b in bytes_obj:
           try:
               dec_msg += b.to_bytes(4, 'little').decode('ascii')
           except UnicodeDecodeError:
               dec_msg += '.'
       print(f"Received data on handle {cHandle}: msg: {data.hex()}--{dec_msg}\n")

def split_cmd(cmd):
    # Convert - use as input to device
    lines = []
    if len(cmd) <= 40:
        lines.append(cmd)
    else:
        startIndex = 0
        while startIndex < len(cmd):
            endIndex = min(startIndex + 40, len(cmd))
            lines.append(cmd[startIndex:endIndex])
            startIndex = endIndex
    return lines

def main():
    print('con to dev')
    # Connect to the device
    device = btle.Peripheral("XXXXX", 'random')
    print('set delgate')
    # Set the delegate to handle notifications
    device.withDelegate(NotificationDelegate())

    # Find the service
    service = device.getServiceByUUID(SERVICE_UUID)

    # Enable notifications on the notify characteristic
    notify_char = service.getCharacteristics(forUUID=NOTIFY_CHAR_UUID)[0]
    notify_handle = notify_char.getHandle()
    notify_cccd_handle = notify_handle + 1
    
    # Find the write characteristic
    write_char = service.getCharacteristics(forUUID=WRITE_CHAR_UUID)[0]
    write_handle = write_char.getHandle()

    # Write data to the write characteristic
    print('send val')
    cmd_list_1 = split_cmd(sys.argv[1])
    cmd_list_2 = split_cmd(sys.argv[2])
    cmd_list_3 = split_cmd(sys.argv[3])

    device.writeCharacteristic(notify_cccd_handle, b"\x01\x00", withResponse=True)

    print 
    for cmd in cmd_list_1:
        print(f'sending command: {bytes.fromhex(cmd)}')
        device.writeCharacteristic(write_handle, bytes.fromhex(cmd), withResponse=True)
    
    if device.waitForNotifications(1.0):
        print('sending info command, i = 4')
        device.writeCharacteristic(write_handle, bytes.fromhex(cmd_list_2[0]), withResponse=True)
    
    while True:
        if device.waitForNotifications(1.0):
            continue
        else:
            break

    print('sending read file end command, i = 5') 
    device.writeCharacteristic(write_handle, bytes.fromhex(cmd_list_2[0]), withResponse=True)

    while True:
        if device.waitForNotifications(1.0):
            continue
        else:
            sys.exit()
if __name__ == "__main__":
    main()
