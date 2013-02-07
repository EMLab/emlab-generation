#!/usr/bin/python
# -*- coding: utf-8 -*-

import  xlrd
import  re
import  os, sys, os.path 
import  csv #, yaml

#
# Adapted the script from 
# http://code.activestate.com/recipes/546518-simple-conversion-of-excel-files-into-csv-and-yaml/
# to extract the load duration curve data from the typical ENTSO-E format (as of 2012).
#It only gives a correct result if the time column 03B:00 is empty!!
#
#Output is a 365x24 csv file. An error message is given, if the dimensions are wrong.
#
#
# Joern C. Richstein - j.c.richstein@tudelft.nl
#


# Read excel sheet into list of 2-d arrays
#
def xlrd_xls2array(infilename):
    """ Returns a list of sheets; each sheet is a dict containing
    * sheet_name: unicode string naming that sheet
    * sheet_data: 2-D table holding the converted cells of that sheet
    """    
    book       = xlrd.open_workbook(infilename)
    sheets     = []
    formatter  = lambda(t,v): format_excelval(book,t,v,False)
    
    for sheet_name in book.sheet_names():
        if sheet_name=='hourly_load_values':
            raw_sheet = book.sheet_by_name(sheet_name)
            data      = []
            for row in range(raw_sheet.nrows):
                (types, values) = (raw_sheet.row_types(row), raw_sheet.row_values(row))
                data.append(map(formatter, zip(types, values)))
            #Remove ENTSO-E Row-Headings
            del data[0:7]
            if data.__len__() != 365:
                print 'Data is of false length, only ' + str(data.__len__()) + " days present!"
                sys.exit(1)
            data = transpose(data)
            #Delete empty columns and empty 03B:00 row. Pay attention if there's something in it!!
            del data[5]
            removeBlankRows(data)
            #Delete Header columns
            del data[0:2]
            #Check length, exit program if false length
            if data.__len__() != 24:
                print 'Data is of false length, only ' + str(data.__len__()) + " hours present!"
                sys.exit(1)
            data = transpose(data)
            sheets.append({ 'sheet_name': sheet_name, 'sheet_data': data })
    return sheets
    
def tupledate_to_isodate(tupledate):
    """
    Turns a gregorian (year, month, day, hour, minute, nearest_second) into a
    standard YYYY-MM-DDTHH:MM:SS ISO date.  If the date part is all zeros, it's
    assumed to be a time; if the time part is all zeros it's assumed to be a date;
    if all of it is zeros it's taken to be a time, specifically 00:00:00 (midnight).

    Note that datetimes of midnight will come back as date-only strings.  A date
    of month=0 and day=0 is meaningless, so that part of the coercion is safe.
    For more on the hairy nature of Excel date/times see http://www.lexicon.net/sjmachin/xlrd.html
    """
    (y,m,d, hh,mm,ss) = tupledate
    nonzero = lambda n: n!=0
    date = "%04d-%02d-%02d"  % (y,m,d)    if filter(nonzero, (y,m,d))                else ''
    time = "T%02d:%02d:%02d" % (hh,mm,ss) if filter(nonzero, (hh,mm,ss)) or not date else ''
    return date+time

def format_excelval(book, type, value, wanttupledate):
    """ Clean up the incoming excel data """
    ##  Data Type Codes:
    ##  EMPTY   0
    ##  TEXT    1 a Unicode string 
    ##  NUMBER  2 float 
    ##  DATE    3 float 
    ##  BOOLEAN 4 int; 1 means TRUE, 0 means FALSE 
    ##  ERROR   5 
    returnrow = []
    if   type == 2: # TEXT
        if value == int(value): value = int(value)
    elif type == 3: # NUMBER
        datetuple = xlrd.xldate_as_tuple(value, book.datemode)
        value = datetuple if wanttupledate else tupledate_to_isodate(datetuple)
    elif type == 5: # ERROR
        value = xlrd.error_text_from_code[value]
    return value

#
# Save to CSV
#

def camelize(s):
    """Makes a reasonable attempt at turning an arbitrary string
    into an identifier-safe CamelCasedString"""
    h = unicode(s)
    h = re.sub(r'(?:[_\s]+)([a-z])',
               lambda m: m.group(1).upper(), h)
    h = re.sub(r'[\-\.]+', '_', h)
    h = re.sub(r'\W',      '',  h)
    return h

def utf8ize(l):
    """Make string-like things into utf-8, leave other things alone
    """
    return [unicode(s).encode("utf-8") if hasattr(s,'encode') else s for s in l]

def dump_csv(table, outdir, outfilename):
    stream = file(os.path.join(outdir, outfilename), 'wb')
    csvout = csv.writer(stream, delimiter=',', doublequote=False, escapechar='\\')
    csvout.writerows( map(utf8ize, table) )
    stream.close()

def save_csv_tables(tables, outdir, outfilebase):
    for (sheet_idx, sheet) in enumerate(tables):
        outfilename = "%s_%s.csv" % (outfilebase,'LC')
        dump_csv(sheet['sheet_data'], outdir, outfilename)

def dump_yaml(tables, outdir, outfilename):
    stream = file(os.path.join(outdir, outfilename+'.yaml'), 'wb')
    yaml.safe_dump(tables, stream, allow_unicode=True)    
    stream.close()    
    
def transpose(grid):
    return zip(*grid)

def removeBlankRows(grid):
    return [list(row) for row in grid if any(row)]

#
# Process files listed on command line, or all .xls files in current dir if no
# args given
#
re_excelfilename = re.compile(r'(\.xls)$')
try:    args = sys.argv[1:]
except: args = []
if len(args) < 1:
    infilenames = filter(re_excelfilename.search, os.listdir("."))
    infilenames.sort()
else:
    infilenames = args

for infilename in infilenames:
    tables = xlrd_xls2array(infilename)
    (outdir, infilebase) = os.path.split(infilename)
    outfilebase = re_excelfilename.sub('', infilebase)    
    save_csv_tables(tables, outdir, outfilebase)
#   dump_yaml      (tables, outdir, outfilebase)
## end of http://code.activestate.com/recipes/546518/ }}}

