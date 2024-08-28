#! /usr/bin/python
batchString=""
for i in range(1,101):
 urls = "[method, csa + occ_uri_1 + (occ1_ref + " + str(i) + ") + \"/update\", body, headers],[method, csa + occ_uri_2 + (occ2_ref + " + str(i) +") + \"/update\", body, headers],[method, csa + occ_uri_3 + (occ3_ref + " + str(i) + ") + \"/update\", body, headers],\n"
 batchString = batchString.__add__(urls)

batchString = batchString[:-2]
print(batchString)
