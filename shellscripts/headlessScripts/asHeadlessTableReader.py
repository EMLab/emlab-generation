import csv
import glob
import re
import sys
import json

def find_runIds_based_on_logfiles_and_runname(path, runName):
    listOfQueryPaths = glob.glob(path + runName + "/*.log")
    listOfRunIds = []
    for query in listOfQueryPaths:
        m = re.search('(?<={0}{1}/).*'.format(path, runName), query)
        n = re.sub("^(.*).log$", "\\1", m.group(0))
        listOfRunIds.append(n)
    return listOfRunIds

def read_tableQuery_of_runid(path, runName, runId, tableName):
    resultDict = {}
    filename = runId + "-" + "TABLE_" + tableName
    filepath = path + runName + "/" + filename
    json_data = open(filepath, 'r')
    line = json_data.readline()
    jsonLine = json.loads(line[:-2], encoding="ascii")
    wrapperCounter = 0
    tempJsonLine = jsonLine
    #Unwrapping the json lists, in case it is wrapped
    while (tempJsonLine is not None) and len(tempJsonLine) == 1 and \
     isinstance(tempJsonLine, (list)) and \
     (not isinstance(tempJsonLine[0][0], (basestring))):
        tempJsonLine = tempJsonLine[0]
        wrapperCounter = wrapperCounter + 1
    headers = tempJsonLine[0]
    tempJsonLine = tempJsonLine[1:]
    headerNumber = 0
    for header in headers:
            resultDict[(headers[headerNumber]).encode("ascii")] = []
            headerNumber = headerNumber + 1
    for subelement in tempJsonLine:
        headerNumber = 0
        if not len(subelement) == len(headers):
            raise NameError("Table length differs from header length!")
        for content in subelement:
            resultDict[(headers[headerNumber]).encode("ascii")].append(content)
            headerNumber = headerNumber + 1
    line = json_data.readline()
    while line and (line is not None) and isinstance(line, (list)):
        counter = 0
        tempJsonLine = json.loads(line[:-2], encoding="ascii")
        while counter < wrapperCounter:
            tempJsonLine = tempJsonLine[0]
            counter = counter + 1
        tempJsonLine = tempJsonLine[1:]
        for subelement in tempJsonLine:
            headerNumber = 0
            if not len(subelement) == len(headers):
                raise NameError("Table length differs from header length!")
            for content in subelement:
                resultDict[(headers[headerNumber]).
                encode("ascii")].append(content)
                headerNumber = headerNumber + 1
        line = json_data.readline()
    return resultDict


def write_first_tableResultDict_to_csv(path, runName, runId, tableName,
resultDict):
    queryNames = ["runId"]
    queryNames = set(queryNames)
    queryNames.update(resultDict.keys())
    print(queryNames)
    #queryNames.update(resultDict.keys())
    noOfTicks = len(resultDict.items()[1][1])
    with open(path + runName + "-" + tableName + ".csv", 'w') as csvfile:
        csvwriter = csv.DictWriter(csvfile, fieldnames=queryNames)
        csvwriter.writeheader()
        i = 0
        while i < noOfTicks:
            singleTickDict = {}
            for key, value in resultDict.iteritems():
                singleTickDict.update({key: value[i]})
            singleTickDict.update({"runId": runId})
            csvwriter.writerow(singleTickDict)
            i = i + 1

def write_following_tableResultDict_to_csv(path, runName, runId, tableName,
resultDict):
    queryNames = resultDict.keys()
    #queryNames.update(resultDict.keys())
    noOfTicks = len(resultDict.items()[1][1])
    with open(path + runName + "-" + tableName + ".csv", 'rb') as csvtoupdate:
        dictReader = csv.DictReader(csvtoupdate)
        queryNames = dictReader.fieldnames
    with open(path + runName + "-" + tableName + ".csv", 'a') as csvfile:
        csvwriter = csv.DictWriter(csvfile, fieldnames=queryNames)
        i = 0
        while i < noOfTicks:
            singleTickDict = {}
            for key, value in resultDict.iteritems():
                singleTickDict.update({key: value[i]})
            singleTickDict.update({"runId": runId})
            csvwriter.writerow(singleTickDict)
            i = i + 1


def write_tableQuery_to_csv(path, runName, tableName):
    runIds = find_runIds_based_on_logfiles_and_runname(path, runName)
    print(runIds)
    totalRunIdNo = len(runIds)
    j = 0
    for runId in runIds:
        print(runId)
        resultDict = read_tableQuery_of_runid(path, runName, runId, 
        tableName)
        if j == 0:
            print("First runId")
            write_first_tableResultDict_to_csv(path, runName, 
            runId, tableName, resultDict)
        else:
            print("other runId)")
            write_following_tableResultDict_to_csv(path, 
            runName, runId, tableName, resultDict)
        j = j + 1
        percentage = 1.0 * j / totalRunIdNo * 100
        print((str(percentage) + "% done."))


def main(path, runName, tableName):
    if(not path.endswith("/")):
        path = path + "/"
    write_tableQuery_to_csv(path, runName, tableName)

if __name__ == "__main__":
    if len(sys.argv[1:]) == 3:
        main(sys.argv[1], sys.argv[2], sys.argv[3])
    elif len(sys.argv[1:]) == 2:
        f=sys.argv[1]
        f=f[:-1]
        print(f)
        fs=f.split("/")
        print(fs)
        f=f.replace(fs[len(fs)-1],"")
        print(f[len(fs)-1])
        print(f,fs[-1],sys.argv[2])
        main(f,fs[-1],sys.argv[2])
    else:
        print("This script needs to be called with: outputPath, \
        runName, tableName")

#path='/home/jrichstein/Desktop/emlabGen/output/'
#runName="scriptTest"
#tableName='PowerPlantDispatchPlans'
#runId="scriptTest-1"

#main(path, runName, runId, tableName)
