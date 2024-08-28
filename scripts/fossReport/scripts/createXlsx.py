#!/usr/bin/env python3

import sys
from subprocess import call
import xlsxwriter
from operator import getitem
import string


def generateXlsx(path,reportFileObj):

    fileCounter=0                                               #JUST A FILE COUNTER

    workbook = xlsxwriter.Workbook(path+'SC_FOSS_Details_Report.xlsx')

    format = workbook.add_format()
    format.set_align('vcenter')
    format.set_align('left')

    worksheet = workbook.add_worksheet()
    worksheet.set_column('A:P', 30) # Columns F-H width set to 30.
    
    cell_format_header = workbook.add_format()
    cell_format_header.set_bold(True) # Also turns bold on.
    cell_format_header.set_bg_color('cyan')
    # Start from the first cell.
    # Rows and columns are zero indexed.
    row = 0
    column = 0
    listOfColumnSize = []
    content = ["Community Name & SW Name/Function Designation", "SW Version", "Ericsson FOSS Generic Prod No", "Primary (Y/N)",
                "FOSS Usage Description", "Selected License(s)", "Linking", "STAKO code / UsageClass", "STAKO Reason/ UsageClassMotivation",
                "USED Encryption Algorithms", "USED Encryption Protocols", "Encryption Usage Description", "FOSS URL (source download)",
                "Included in Ericsson Prod No *"]

    # iterating through content list
    for item in content :     
        # write operation perform
        worksheet.write(row, column, item, cell_format_header)
        listOfColumnSize.append(len(item))
        # incrementing the value of column by one
        # with each iterations.
        column += 1
        #worksheet.write(row, column, 'Community Name & SW Name/Function Designation'])
        #worksheet.write(row, column + 1, 'SW Version'])
    row += 1
    column = 0
    sortedObj=sorted(reportFileObj['FOSS'].items(), key = lambda x: getitem(x[1], 'FOSSName'))

    for i in range(0,len(sortedObj)):                       #FOR EVERY PRIM(CAX) IN THE FOSS REPORT LIST            
        worksheet.write(row, column, sortedObj[i][1]['FOSSName'])
        listOfColumnSize[0]=max(listOfColumnSize[0],len(sortedObj[i][1]['FOSSName']))
        
        worksheet.write(row, column + 1, sortedObj[i][1]['FOSSVersion'])
        listOfColumnSize[1]=max(listOfColumnSize[1],len(str(sortedObj[i][1]['FOSSVersion'])))

        worksheet.write(row, column + 2, sortedObj[i][1]['PRIMNumber(CAX/CTX)'])

        listOfColumnSize[2]=max(listOfColumnSize[2],len(sortedObj[i][1]['PRIMNumber(CAX/CTX)']))

        worksheet.write(row, column + 3, sortedObj[i][1]['Primary'])
        listOfColumnSize[3]=max(listOfColumnSize[3],len(sortedObj[i][1]['Primary']))

        worksheet.write(row, column + 4, sortedObj[i][1]['FunctionalityUsedInTheEricssonProduct'])
        listOfColumnSize[4]=max(listOfColumnSize[4],len(sortedObj[i][1]['FunctionalityUsedInTheEricssonProduct']))

        worksheet.write(row, column + 5, sortedObj[i][1]['ChoiceOfLicense'])
        listOfColumnSize[5]=max(listOfColumnSize[5],len(sortedObj[i][1]['ChoiceOfLicense']))

        worksheet.write(row, column + 6, sortedObj[i][1]['EricssonCodeLinkedWithTheFOSSCode'])
        listOfColumnSize[6]=max(listOfColumnSize[6],len(sortedObj[i][1]['EricssonCodeLinkedWithTheFOSSCode']))

        worksheet.write(row, column + 7, sortedObj[i][1]['STAKOCode'])
        listOfColumnSize[7]=max(listOfColumnSize[7],len(sortedObj[i][1]['STAKOCode']))

        if (not str(sortedObj[i][1]['STAKOReason']) == "()"):
            worksheet.write(row, column + 8, sortedObj[i][1]['STAKOReason'])
            listOfColumnSize[8]=max(listOfColumnSize[8],len(sortedObj[i][1]['STAKOReason']))

        if (not str(sortedObj[i][1]['CryptographicAlgorithmsUsed']) == "()"):
            worksheet.write(row, column + 9, sortedObj[i][1]['CryptographicAlgorithmsUsed'])
            listOfColumnSize[9]=max(listOfColumnSize[9],len(sortedObj[i][1]['CryptographicAlgorithmsUsed']))

        if (not str(sortedObj[i][1]['SecureProtocolsUsed']) == "()"):
            worksheet.write(row, column + 10, sortedObj[i][1]['SecureProtocolsUsed'])
            listOfColumnSize[10]=max(listOfColumnSize[10],len(sortedObj[i][1]['SecureProtocolsUsed']))

        encryptionusage = ""
        if (not str(sortedObj[i][1]['CryptographicAlgorithmsUsedDescription']) == "NA"):
            encryptionusage = encryptionusage + sortedObj[i][1]['CryptographicAlgorithmsUsedDescription'] + " "
        if (not str(sortedObj[i][1]['SecureProtocolsUsedDescription']) == "NA"):
            encryptionusage = encryptionusage + sortedObj[i][1]['SecureProtocolsUsedDescription']
        worksheet.write(row, column + 11, encryptionusage)
        listOfColumnSize[11]=max(listOfColumnSize[11],len(encryptionusage))

        worksheet.write(row, column + 12, sortedObj[i][1]['FOSSURL'])
        listOfColumnSize[12]=max(listOfColumnSize[12],len(sortedObj[i][1]['FOSSURL']))
        row += 1             
    
    alph=string.ascii_uppercase
    alph_list=list(alph)
    columnList=[]

    for char in alph_list:
        columnList.append(char+":"+char)

    for i in range(0,len(listOfColumnSize)):
        if (i==4 or i==5 or i==12):
            worksheet.set_column(columnList[i], 30)
        else:
            worksheet.set_column(columnList[i], listOfColumnSize[i])
    
    worksheet.set_column(columnList[1], listOfColumnSize[1], format)
    workbook.close()
    sys.stdout.write('SC_FOSS_Details_Report.xlsx')
    sys.stdout.flush()


    print("-"*100)
    print("xlsx file created with name: SC_FOSS_Details_Report.xlsx under: "+path +" containing " + str(len(sortedObj))+ " dependencies.")
    print("-" * 100)   

