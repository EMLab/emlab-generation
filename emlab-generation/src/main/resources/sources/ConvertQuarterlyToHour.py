import csv, sys


def csv2list(filename):
    reader = csv.reader(open("./" + filename, 'rb'), delimiter=';')
    data = []
    for row in reader:
    	data.append(row)
    return data

def extractColumn(datam, columnNo):
    column = []
    for row in data:
        column.append(row[columnNo])
    return column

def convertToHourly(quarterlyColumn):
    hourlyColumn = []
    del quarterlyColumn[0]
    for i in range(0, len(quarterlyColumn) / 4):
        cell = []
        cell.append((float(quarterlyColumn[4 * i]) + float(quarterlyColumn[4 * i + 1]) + float(quarterlyColumn[4 * i + 2]) + float(quarterlyColumn[4 * i + 3])) / 4)
        hourlyColumn.append(cell)
    return hourlyColumn     
        
        
def writeListToCsv(column, outfilename):
    outwriter = csv.writer(open(outfilename, 'wb'))
    for row in column:
        outwriter.writerow(row)
        


#main function
try:    args = sys.argv[1:]
except: args = []
infilename = str(args[0])
columnNo = 3
data = csv2list(infilename)
#for row in table:
#	print row
quarterlyColumn = extractColumn(data, columnNo)
hourlyColumn = convertToHourly(quarterlyColumn)
#for row in hourlyColumn:
#    print row
writeListToCsv(hourlyColumn, infilename[:-4] + "Hourly.csv")
