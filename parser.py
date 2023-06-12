import json
BLE_NAME='<Oxyfit Device>'
HOST_NAME='<Android Smartphone>'
GREEN = '\033[32m'
YELLOW = '\033[33m'
RESET = '\033[0m'
# Opening JSON file
f  = open('captures/settings.json')
data = json.load(f)
for d in data:
    if 'btatt.value' not in d['_source']['layers']['btatt'].keys():
        continue
    val = d['_source']['layers']['btatt']['btatt.value']
    val = val.replace(':', '')
    dec_msg = ""
    bytes_obj = bytes.fromhex(val)
    for b in bytes_obj:
        try:
            dec_msg += b.to_bytes(4, 'little').decode('ascii')
        except UnicodeDecodeError:
            dec_msg += '.'
    if d['_source']['layers']['bthci_acl']['bthci_acl.src.name'] == HOST_NAME:
        print(f'{GREEN}>  {val}  {dec_msg}{RESET}')

    if d['_source']['layers']['bthci_acl']['bthci_acl.src.name'] == BLE_NAME:
        print(f'{YELLOW}  <  {val}  {dec_msg}{RESET}')  
