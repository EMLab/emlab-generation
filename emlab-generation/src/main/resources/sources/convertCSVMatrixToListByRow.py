#!/usr/bin/python
# -*- coding: utf-8 -*-

import csv, sys

def csv2list(filename):
    reader = csv.reader(open("./" + filename, 'rb'), delimiter=',')
    data = []
    for row in reader:
	for element in row:
		cell = []
		cell.append(element)
    		data.append(cell)
    return data

def writeListToCsv(data, outfilename):
    outwriter = csv.writer(open(outfilename, 'wb'))
    for row in data:
        outwriter.writerow(row)

#main function
try:    args = sys.argv[1:]
except: args = []
infilename = str(args[0])
data = csv2list(infilename)
writeListToCsv(data, infilename[:-4] + "_asList.csv")
