"""
Query IDs:
0: Bloom membership check
1: CM data
3: Item insertion
"""
import os

import sys


def main():
    startIP = sys.argv[1]
    endIP = sys.argv[2]
    repeats = sys.argv[3]

    if not os.path.exists("datasets/"):
        os.makedirs("datasets/")

    with open("datasets/" + "data" + ".txt", 'w') as inputFile, \
            open("datasets/" + "queries" + ".txt", 'w') as queryFile, \
            open("datasets/" + "_notes" + ".txt", 'w') as notes:

        try:
            ip_idx = 0
            query_idx = 0
            cnt = 0

            for address in ipRange(startIP, endIP):
                for i in range(0, repeats):
                    inputFile.write("{0}\n".format(address))
                    ip_idx += 1
                queryFile.write("{0} {1}\n".format(query_idx, address))
                query_idx += 1

                cnt += 1

            notes.write("Total inserted IPs: {0}\nTotal queries: {1}".format(ip_idx, query_idx))

        except KeyboardInterrupt:
            print("Ctrl+C!")


def ipRange(start_ip, end_ip):
    start = list(map(int, start_ip.split(".")))
    end = list(map(int, end_ip.split(".")))
    temp = start
    ip_range = [start_ip]

    while temp != end:
        start[3] += 1
        for i in (3, 2, 1):
            if temp[i] == 256:
                temp[i] = 0
                temp[i - 1] += 1
        ip_range.append(".".join(map(str, temp)))

    return ip_range


if __name__ == '__main__':
    main()
