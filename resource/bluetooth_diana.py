import bluetooth
import sys
import time

#bd_addr="00:16:53:47:CE:27"

#bd_addr="30:14:08:20:29:06" # charlie bluetooth address
bd_addr = "00:06:66:6C:A5:B6" # diana bluetooth address

port = 1
sock=bluetooth.BluetoothSocket( bluetooth.RFCOMM )
sock.connect((bd_addr, port))
print 'Connected'
sock.settimeout(1.0)

STX = 0x02  # start tx byte
ETX = 0x03  # end tx byte


def value_to_chars(value):
    if value >= 0:
        a = 50
    else:
        a = 49
        value = 100+value

    b = int(value/10)+48
    c = (value % 10)+48

    return a, b, c


def byte_comm(val_x, val_y):
    (a, b, c) = value_to_chars(val_x)
    (d, e, f) = value_to_chars(val_y)
    print a, d

    return buffer(bytearray([STX, a, b, c, d, e, f, ETX]))

time.sleep(0.2)

fwd_comm = byte_comm(-15, 5)
sock.send(fwd_comm)


# zero_comm = byte_comm(0, 0)
# sock.send(zero_comm)
time.sleep(0.05)
# sock.send(zero_comm)

# print 'ddd'
# time.sleep(2)
#
# for i in range(10):
#     sock.send("x01")
#

print 'Sent data'
sock.close()
#data = sock.recv(1)
#print 'received [%s]'%data
#time.sleep(0.5)

# sock=bluetooth.BluetoothSocket( bluetooth.RFCOMM )
# #sock.connect((bd_addr, port))
#
# sock.send(zero_comm)
# print 'Sent data'
# sock.close()