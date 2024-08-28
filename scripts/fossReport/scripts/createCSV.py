#!/usr/bin/env python3
 
import sys
import csv
from operator import getitem
 
def generateCSV(path, reportFileObj):
 
    fileCounter = 0  # JUST A FILE COUNTER
 
    csv_filename = path + 'SC_FOSS_Details_Report_CSV.csv'
 
    # Define header
    headers = ["A FOSS product number and R-State", "B Ericsson 3PP license product no and name", "C obligationFullfilment", "D FOSS Usage Description", "E Primary", "F Linking"]

    # Open the CSV file for writing
    with open(csv_filename, mode='w', newline='', encoding='utf-8') as csvfile:
        csvwriter = csv.writer(csvfile)
        # Write the header
        csvwriter.writerow(headers)
        sortedObj = sorted(reportFileObj['FOSS'].items(), key=lambda x: getitem(x[1], 'FOSSName'))
 
        # Write data rows
        for i in range(0, len(sortedObj)):  # FOR EVERY PRIM(CAX) IN THE FOSS REPORT LIST
            row_data = []
 
            # FOSS product number and R-State
            if 'RState' in sortedObj[i][1]:
                col0 = sortedObj[i][1]['PRIMNumber(CAX/CTX)'] + " " + sortedObj[i][1]['RState']
            else:
                col0 = sortedObj[i][1]['PRIMNumber(CAX/CTX)'] + " " + "R1A"
            row_data.append(col0)
 
            # Ericsson 3PP license product no and name
            if sortedObj[i][1]['ChoiceOfLicense'].startswith("FAL11"):
                row_data.append(sortedObj[i][1]['ChoiceOfLicense'])
            else:
                row_data.append(find_value_in_string(sortedObj[i][1]['ChoiceOfLicense']))

 
            # obligationFullfilment
            row_data.append(sortedObj[i][1]['FulfillmentOfLicenseObligations'])
 
            # FOSS Usage Description
            row_data.append(sortedObj[i][1]['FunctionalityUsedInTheEricssonProduct'])
 
            # Primary
            if sortedObj[i][1]['Primary'] == "Y":
                row_data.append("X")
            else:
                row_data.append("")
 
            # Linking
            linking = ""
            if sortedObj[i][1]['EricssonCodeLinkedWithTheFOSSCode'] == "Dynamically":
                linking = "Dynamic"
            elif sortedObj[i][1]['EricssonCodeLinkedWithTheFOSSCode'] == "Statically":
                linking = "Static"
            elif sortedObj[i][1]['EricssonCodeLinkedWithTheFOSSCode'] == "NotLinked":
                linking = "Not Linked"
            else:
                linking = sortedObj[i][1]['EricssonCodeLinkedWithTheFOSSCode']
            row_data.append(linking)
 
            # Write the row to CSV
            csvwriter.writerow(row_data)
 
    sys.stdout.write('SC_FOSS_Details_Report_CSV.csv')
    sys.stdout.flush()
 
    print("-" * 100)
    print("CSV file created with name: SC_FOSS_Details_Report_CSV.csv under: " + path + " containing " + str(len(sortedObj)) + " dependencies.")
    print("-" * 100)

def find_value_in_string(license):

    # Define Licence Dictionary with Product numbers
    licensesDict = {
"FAL 115 9000":["Other FOSS licenses"],
"FAL1159011":["Microsoft Public License (MS-PL)"],
"FAL1159012/10":["Common Public License 1.0 (CPL-1.0)"],
"FAL1159013/10":["Eclipse Public License 1.0 (EPL-1.0)"],
"FAL1159014":["Ruby License (Ruby)"],
"FAL1159015/20":["Python License 2.0 (Python-2.0)"],
"FAL1159016":["Sleepycat License (Sleepycat)"],
"FAL1159013/20":["Eclipse Public License 2.0 (EPL-2.0)", "Eclipse Public License 2.0"],
"FAL1159018":["W3C Software Notice and License (2002-12-31) (W3C)"],
"FAL 115 9001/1":["GNU General Public License v1.0 only (GPL-1.0-only)"],
"FAL 115 9001/2":["GNU General Public License v2.0 only (GPL-2.0-only)"],
"FAL 115 9001/3":["GNU General Public License v3.0 only (GPL-3.0-only)"],
"FAL 115 9001/4":["GNU General Public License v2.0 w/Classpath exception (GPL-2.0-with-classpath-exception)", "GPL-2.0-with-classpath-exception", "GNU General Public License v2.0 w/Classpath exception"],
"FAL 115 9001/5":["GNU General Public License v2.0 w/Font exception (GPL-2.0-with-font-exception)"],
"FAL 115 9001/6":["GNU General Public License v2.0 w/Autoconf exception (GPL-2.0-with-autoconf-exception)"],
"FAL 115 9001/7":["GNU General Public License v2.0 w/GCC Runtime Library exception (GPL-2.0-with-GCC-exception)"],
"FAL 115 9001/8":["GNU General Public License v2.0 w/Bison exception (GPL-2.0-with-bison-exception)"],
"FAL 115 9001/9":["GNU General Public License v3.0 w/GCC Runtime Library exception (GPL-3.0-with-GCC-exception)"],
"FAL 115 9001/1L":["GNU General Public License v1.0 or later (GPL-1.0-or-later)"],
"FAL 115 9001/10":["GNU General Public License v3.0 w/Autoconf exception (GPL-3.0-with-autoconf-exception)"],
"FAL 115 9001/2L":["GNU General Public License v2.0 or later (GPL-2.0-or-later)"],
"FAL 115 9001/3L":["GNU General Public License v3.0 or later (GPL-3.0-or-later)"],
"FAL 115 9002/3":["GNU Lesser General Public License v3.0 only"],
"FAL 115 9002/20":["GNU Library General Public License v2 only (LGPL-2.0-only)"],
"FAL 115 9002/21":["GNU Lesser General Public License v2.1 only (LGPL-2.1-only)", "LGPL-2.1-only", "GNU Lesser General Public License 2.1 (LGPL2.1)"],
"FAL 115 9002/30":["GNU Lesser General Public License v3.0 only (LGPL-3.0-only)"],
"FAL 115 9002/20L":["GNU Library General Public License v2 or later (LGPL-2.0-or-later)"],
"FAL 115 9002/21L":["GNU Lesser General Public License v2.1 or later (LGPL-2.1-or-later)"],
"FAL 115 9002/30L":["GNU Lesser General Public License v3.0 or later (LGPL-3.0-or-later)"],
"FAL 115 9003/1":["BSD 2-Clause \"Simplified\" License (BSD-2-Clause)","BSD 2-Clause"],
"FAL 115 9003/2":["BSD 3-Clause \"New\" or \"Revised\" License (BSD-3-Clause)","BSD-3-Clause", "BSD 3-Clause (BSD-3-Clause)", "BSD 3-Clause"],
"FAL 115 9003/3":["BSD 4-Clause \"Original\" or \"Old\" License (BSD-4-Clause)"],
"FAL 115 9003/10":["BSD Zero Clause License (0BSD)"],
"FAL 115 9003/11":["BSD 2-Clause FreeBSD License (BSD-2-Clause-FreeBSD)"],
"FAL 115 9003/12":["BSD-2-Clause Plus Patent License (BSD-2-Clause-Patent)"],
"FAL 115 9003/13":["BSD Source Code Attribution (BSD-Source-Code)"],
"FAL 115 9003/14":["BSD 2-Clause NetBSD License (BSD-2-Clause-NetBSD)"],
"FAL 115 9003/21":["BSD with attribution (BSD-3-Clause-Attribution)"],
"FAL 115 9003/22":["BSD 3-Clause Clear License (BSD-3-Clause-Clear)"],
"FAL 115 9003/23":["Lawrence Berkeley National Labs BSD variant license (BSD-3-Clause-LBNL)"],
"FAL 115 9003/24":["BSD 3-Clause No Nuclear License (BSD-3-Clause-No-Nuclear-License)"],
"FAL 115 9003/25":["BSD 3-Clause No Nuclear License 2014 (BSD-3-Clause-No-Nuclear-License-2014)"],
"FAL 115 9003/26":["BSD 3-Clause No Nuclear Warranty (BSD-3-Clause-No-Nuclear-Warranty)"],
"FAL 115 9003/27":["BSD 3-Clause Open MPI variant (BSD-3-Clause-Open-MPI)"],
"FAL 115 9003/31":["BSD-4-Clause (University of California-Specific) (BSD-4-Clause-UC)"],
"FAL 115 9003/32":["BSD Protection License (BSD-Protection)"],
"FAL 115 9003/111":["BSD 1-Clause License (BSD-1-Clause)"],
"FAL 115 9004/10":["Apache License 1.0 (Apache-1.0)"],
"FAL 115 9004/11":["Apache License 1.1 (Apache-1.1)"],
"FAL 115 9004/20":["Apache License 2.0 (Apache-2.0)", "Apache-2.0", "Apache License 2.0"],
"FAL 115 9005/10":["Mozilla Public License 1.0 (MPL-1.0)"],
"FAL 115 9005/11":["Mozilla Public License 1.1 (MPL-1.1)"],
"FAL 115 9005/20":["Mozilla Public License 2.0 (MPL-2.0)", "MPL-2.0"],
"FAL 115 9005/21":["Mozilla Public License 2.0 (no copyleft exception) (MPL-2.0-no-copyleft-exception)"],
"FAL 115 9006/3":["GNU Affero General Public License v3.0 only (AGPL-3.0-only)"],
"FAL 115 9006/10":["Affero General Public License v1.0 only (AGPL-1.0-only)"],
"FAL 115 9006/3L":["GNU Affero General Public License v3.0 or later (AGPL-3.0-or-later)"],
"FAL 115 9006/10L":["Affero General Public License v1.0 or later (AGPL-1.0-or-later)"],
"FAL 115 9007/10":["Common Development and Distribution License 1.0 (CDDL-1.0)"],
"FAL 115 9007/11":["Common Development and Distribution License 1.1 (CDDL-1.1)"],
"FAL 115 9008":["MIT License (MIT)", "MIT License"],
"FAL 115 9008/1":["MIT No Attribution (MIT-0)"],
"FAL 115 9008/2":["Enlightenment License (e16) (MIT-advertising)"],
"FAL 115 9008/3":["CMU License (MIT-CMU)"],
"FAL 115 9008/4":["enna License (MIT-enna)"],
"FAL 115 9008/5":["feh License (MIT-feh)"],
"FAL 115 9008/6":["MIT +no-false-attribs license (MITNFA)"],
"FAL 115 9009":["Artistic License 2.0 (Artistic-2.0)"],
"FAL 115 9009/10":["Artistic License 1.0 (Artistic-1.0)"],
"FAL 115 9009/20":["Artistic License 2.0 (Artistic-2.0)"],
"FAL 115 9009/CL8":["Artistic License 1.0 w/clause 8 (Artistic-1.0-cl8)"],
"FAL 115 9009/PER":["Artistic License 1.0 (Perl) (Artistic-1.0-Perl)"],
"FAL 115 9010":["PostgreSQL License (PostgreSQL)"],
"FAL 115 9011":["Microsoft Public License (MS-PL)"],
"FAL 115 9012/10":["Common Public License 1.0 (CPL-1.0)"],
"FAL 115 9013/10":["Eclipse Public License 1.0 (EPL-1.0)", "Eclipse Public License (EPL) 1.0"],
"FAL 115 9013/20":["Eclipse Public License 2.0 (EPL-2.0)", "Eclipse Public License (EPL) 2.0"],
"FAL 115 9014":["Ruby License (Ruby)"],
"FAL 115 9015/20":["Python License 2.0 (Python-2.0)"],
"FAL 115 9016":["Sleepycat License (Sleepycat)"],
"FAL 115 9017":["EU DataGrid Software License (EUDatagrid)"],
"FAL 115 9018":["W3C Software Notice and License (2002-12-31) (W3C)"],
"FAL 115 9018/1":["W3C Software Notice and License (1998-07-20) (W3C-19980720)"],
"FAL 115 9018/2":["W3C Software Notice and Document License (2015-05-13) (W3C-20150513)"],
"FAL 115 9019":["Attribution Assurance License (AAL)"],
"FAL 115 9020":["Abstyles License (Abstyles)"],
"FAL 115 9021":["Adobe Systems Incorporated Source Code License Agreement (Adobe-2006)"],
"FAL 115 9022":["Adobe Glyph List License (Adobe-Glyph)"],
"FAL 115 9023":["Amazon Digital Services License (ADSL)"],
"FAL 115 9024/11":["Academic Free License v1.1 (AFL-1.1)"],
"FAL 115 9024/12":["Academic Free License v1.2 (AFL-1.2)"],
"FAL 115 9024/20":["Academic Free License v2.0 (AFL-2.0)"],
"FAL 115 9024/21":["Academic Free License v2.1 (AFL-2.1)"],
"FAL 115 9024/30":["Academic Free License v3.0 (AFL-3.0)"],
"FAL 115 9025":["Afmparse License (Afmparse)"],
"FAL 115 9026":["Aladdin Free Public License (Aladdin)"],
"FAL 115 9027":["AMD's plpa_map.c License (AMDPLPA)"],
"FAL 115 9028":["Apple MIT License (AML)"],
"FAL 115 9029":["Academy of Motion Picture Arts and Sciences BSD (AMPAS)"],
"FAL 115 9030":["ANTLR Software Rights Notice (ANTLR-PD)"],
"FAL 115 9031":["Adobe Postscript AFM License (APAFML)"],
"FAL 115 9032/10":["Adaptive Public License 1.0 (APL-1.0)"],
"FAL 115 9033/10":["Apple Public Source License 1.0 (APSL-1.0)"],
"FAL 115 9033/11":["Apple Public Source License 1.1 (APSL-1.1)"],
"FAL 115 9033/12":["Apple Public Source License 1.2 (APSL-1.2)"],
"FAL 115 9033/20":["Apple Public Source License 2.0 (APSL-2.0)"],
"FAL 115 9034":["Bahyph License (Bahyph)"],
"FAL 115 9035":["Barr License (Barr)"],
"FAL 115 9036":["Beerware License (Beerware)"],
"FAL 115 9037/10":["BitTorrent Open Source License v1.0 (BitTorrent-1.0)"],
"FAL 115 9037/11":["BitTorrent Open Source License v1.1 (BitTorrent-1.1)"],
"FAL 115 9038":["SQLite Blessing (blessing)"],
"FAL 115 9039/100":["Blue Oak Model License 1.0.0 (BlueOak-1.0.0)"],
"FAL 115 9040":["Borceux license (Borceux)"],
"FAL 115 9041/10":["Boost Software License 1.0 (BSL-1.0)", "Boost Software License 1.0"],
"FAL 115 9042/105":["bzip2 and libbzip2 License v1.0.5 (bzip2-1.0.5)"],
"FAL 115 9042/106":["bzip2 and libbzip2 License v1.0.6 (bzip2-1.0.6)"],
"FAL 115 9043/10":["Cryptographic Autonomy License 1.0 (CAL-1.0)"],
"FAL 115 9043/100":["Cryptographic Autonomy License 1.0 (Combined Work Exception) (CAL-1.0-Combined-Work-Exception)"],
"FAL 115 9044":["Caldera License (Caldera)"],
"FAL 115 9045/11":["Computer Associates Trusted Open Source License 1.1 (CATOSL-1.1)"],
"FAL 115 9046/10":["Creative Commons Attribution 1.0 Generic (CC-BY-1.0)"],
"FAL 115 9046/20":["Creative Commons Attribution 2.0 Generic (CC-BY-2.0)"],
"FAL 115 9046/25":["Creative Commons Attribution 2.5 Generic (CC-BY-2.5)"],
"FAL 115 9046/30":["Creative Commons Attribution 3.0 Unported (CC-BY-3.0)"],
"FAL 115 9046/40":["Creative Commons Attribution 4.0 International (CC-BY-4.0)"],
"FAL 115 9047/10":["Creative Commons Attribution Non Commercial 1.0 Generic (CC-BY-NC-1.0)"],
"FAL 115 9047/20":["Creative Commons Attribution Non Commercial 2.0 Generic (CC-BY-NC-2.0)"],
"FAL 115 9047/25":["Creative Commons Attribution Non Commercial 2.5 Generic (CC-BY-NC-2.5)"],
"FAL 115 9047/30":["Creative Commons Attribution Non Commercial 3.0 Unported (CC-BY-NC-3.0)"],
"FAL 115 9047/40":["Creative Commons Attribution Non Commercial 4.0 International (CC-BY-NC-4.0)"],
"FAL 115 9048/10":["Creative Commons Attribution Non Commercial No Derivatives 1.0 Generic (CC-BY-NC-ND-1.0)"],
"FAL 115 9048/20":["Creative Commons Attribution Non Commercial No Derivatives 2.0 Generic (CC-BY-NC-ND-2.0)"],
"FAL 115 9048/25":["Creative Commons Attribution Non Commercial No Derivatives 2.5 Generic (CC-BY-NC-ND-2.5)"],
"FAL 115 9048/30":["Creative Commons Attribution Non Commercial No Derivatives 3.0 Unported (CC-BY-NC-ND-3.0)"],
"FAL 115 9048/40":["Creative Commons Attribution Non Commercial No Derivatives 4.0 International (CC-BY-NC-ND-4.0)"],
"FAL 115 9049/10":["Creative Commons Attribution Non Commercial Share Alike 1.0 Generic (CC-BY-NC-SA-1.0)"],
"FAL 115 9049/20":["Creative Commons Attribution Non Commercial Share Alike 2.0 Generic (CC-BY-NC-SA-2.0)"],
"FAL 115 9049/25":["Creative Commons Attribution Non Commercial Share Alike 2.5 Generic (CC-BY-NC-SA-2.5)"],
"FAL 115 9049/30":["Creative Commons Attribution Non Commercial Share Alike 3.0 Unported (CC-BY-NC-SA-3.0)"],
"FAL 115 9049/40":["Creative Commons Attribution Non Commercial Share Alike 4.0 International (CC-BY-NC-SA-4.0)"],
"FAL 115 9050/10":["Creative Commons Attribution No Derivatives 1.0 Generic (CC-BY-ND-1.0)"],
"FAL 115 9050/20":["Creative Commons Attribution No Derivatives 2.0 Generic (CC-BY-ND-2.0)"],
"FAL 115 9050/25":["Creative Commons Attribution No Derivatives 2.5 Generic (CC-BY-ND-2.5)"],
"FAL 115 9050/30":["Creative Commons Attribution No Derivatives 3.0 Unported (CC-BY-ND-3.0)"],
"FAL 115 9050/40":["Creative Commons Attribution No Derivatives 4.0 International (CC-BY-ND-4.0)"],
"FAL 115 9051/10":["Creative Commons Attribution Share Alike 1.0 Generic (CC-BY-SA-1.0)"],
"FAL 115 9051/20":["Creative Commons Attribution Share Alike 2.0 Generic (CC-BY-SA-2.0)"],
"FAL 115 9051/25":["Creative Commons Attribution Share Alike 2.5 Generic (CC-BY-SA-2.5)"],
"FAL 115 9051/30":["Creative Commons Attribution Share Alike 3.0 Unported (CC-BY-SA-3.0)"],
"FAL 115 9051/40":["Creative Commons Attribution Share Alike 4.0 International (CC-BY-SA-4.0)"],
"FAL 115 9052":["Creative Commons Public Domain Dedication and Certification (CC-PDDC)"],
"FAL 115 9053/10":["Creative Commons Zero v1.0 Universal (CC0-1.0)"],
"FAL 115 9054/10":["Community Data License Agreement Permissive 1.0 (CDLA-Permissive-1.0)"],
"FAL 115 9055/10":["Community Data License Agreement Sharing 1.0 (CDLA-Sharing-1.0)"],
"FAL 115 9056/10":["CeCILL Free Software License Agreement v1.0 (CECILL-1.0)"],
"FAL 115 9056/11":["CeCILL Free Software License Agreement v1.1 (CECILL-1.1)"],
"FAL 115 9056/20":["CeCILL Free Software License Agreement v2.0 (CECILL-2.0)"],
"FAL 115 9056/21":["CeCILL Free Software License Agreement v2.1 (CECILL-2.1)"],
"FAL 115 9057":["CeCILL-B Free Software License Agreement (CECILL-B)"],
"FAL 115 9058":["CeCILL-C Free Software License Agreement (CECILL-C)"],
"FAL 115 9059/11":["CERN Open Hardware Licence v1.1 (CERN-OHL-1.1)"],
"FAL 115 9059/12":["CERN Open Hardware Licence v1.2 (CERN-OHL-1.2)"],
"FAL 115 9059/20P":["CERN Open Hardware Licence Version 2 - Permissive (CERN-OHL-P-2.0)"],
"FAL 115 9059/20S":["CERN Open Hardware Licence Version 2 - Strongly Reciprocal (CERN-OHL-S-2.0)"],
"FAL 115 9059/20W":["CERN Open Hardware Licence Version 2 - Weakly Reciprocal (CERN-OHL-W-2.0)"],
"FAL 115 9060":["Clarified Artistic License (ClArtistic)"],
"FAL 115 9061":["CNRI Jython License (CNRI-Jython)"],
"FAL 115 9062":["CNRI Python License (CNRI-Python)"],
"FAL 115 9063":["CNRI Python Open Source GPL Compatible License Agreement (CNRI-Python-GPL-Compatible)"],
"FAL 115 9064/11":["Condor Public License v1.1 (Condor-1.1)"],
"FAL 115 9065/030":["copyleft-next 0.3.0 (copyleft-next-0.3.0)"],
"FAL 115 9065/031":["copyleft-next 0.3.1 (copyleft-next-0.3.1)"],
"FAL 115 9066/10":["Common Public Attribution License 1.0 (CPAL-1.0)"],
"FAL 115 9067/102":["Code Project Open License 1.02 (CPOL-1.02)"],
"FAL 115 9068":["Crossword License (Crossword)"],
"FAL 115 9069":["CrystalStacker License (CrystalStacker)"],
"FAL 115 9070/10":["CUA Office Public License v1.0 (CUA-OPL-1.0)"],
"FAL 115 9071":["Cube License (Cube)"],
"FAL 115 9072":["curl License (curl)"],
"FAL 115 9073/10":["Deutsche Freie Software Lizenz (D-FSL-1.0)"],
"FAL 115 9074":["diffmark license (diffmark)"],
"FAL 115 9075":["DOC License (DOC)"],
"FAL 115 9076":["Dotseqn License (Dotseqn)"],
"FAL 115 9077":["DSDP License (DSDP)"],
"FAL 115 9078":["dvipdfm License (dvipdfm)"],
"FAL 115 9079/10":["Educational Community License v1.0 (ECL-1.0)"],
"FAL 115 9079/20":["Educational Community License v2.0 (ECL-2.0)"],
"FAL 115 9080/10":["Eiffel Forum License v1.0 (EFL-1.0)"],
"FAL 115 9080/20":["Eiffel Forum License v2.0 (EFL-2.0)"],
"FAL 115 9081":["eGenix.com Public License 1.1.0 (eGenix)"],
"FAL 115 9082":["Entessa Public License v1.0 (Entessa)"],
"FAL 115 9083/11":["Erlang Public License v1.1 (ErlPL-1.1)"],
"FAL 115 9084/20":["Etalab Open License 2.0 (etalab-2.0)"],
"FAL 115 9085/10":["European Union Public License 1.0 (EUPL-1.0)"],
"FAL 115 9085/11":["European Union Public License 1.1 (EUPL-1.1)"],
"FAL 115 9085/12":["European Union Public License 1.2 (EUPL-1.2)"],
"FAL 115 9086":["Eurosym License (Eurosym)"],
"FAL 115 9087":["Fair License (Fair)"],
"FAL 115 9088/10":["Frameworx Open License 1.0 (Frameworx-1.0)"],
"FAL 115 9089":["FreeImage Public License v1.0 (FreeImage)"],
"FAL 115 9090":["FSF All Permissive License (FSFAP)"],
"FAL 115 9091":["FSF Unlimited License (FSFUL)"],
"FAL 115 9092":["FSF Unlimited License (with License Retention) (FSFULLR)"],
"FAL 115 9093":["Freetype Project License (FTL)"],
"FAL 115 9094/11":["GNU Free Documentation License v1.1 only (GFDL-1.1-only)"],
"FAL 115 9094/12":["GNU Free Documentation License v1.2 only (GFDL-1.2-only)"],
"FAL 115 9094/13":["GNU Free Documentation License v1.3 only (GFDL-1.3-only)"],
"FAL 115 9094/11L":["GNU Free Documentation License v1.1 or later (GFDL-1.1-or-later)"],
"FAL 115 9094/12L":["GNU Free Documentation License v1.2 or later (GFDL-1.2-or-later)"],
"FAL 115 9094/13L":["GNU Free Documentation License v1.3 or later (GFDL-1.3-or-later)"],
"FAL 115 9095":["Giftware License (Giftware)"],
"FAL 115 9096":["Inno Setup License"],
"FAL 115 9097":["GL2PS License (GL2PS)"],
"FAL 115 9098":["3dfx Glide License (Glide)"],
"FAL 115 9099":["Glulxe License (Glulxe)"],
"FAL 115 9100":["gnuplot License (gnuplot)"],
"FAL 115 9101":["gSOAP Public License v1.3b (gSOAP-1.3b)"],
"FAL 115 9102":["Haskell Language Report License (HaskellReport)"],
"FAL 115 9103/21":["Hippocratic License 2.1 (Hippocratic-2.1)"],
"FAL 115 9104/1":["Historical Permission Notice and Disclaimer (HPND)"],
"FAL 115 9104/2":["Historical Permission Notice and Disclaimer - sell variant (HPND-sell-variant)"],
"FAL 115 9105":["IBM PowerPC Initialization and Boot Software (IBM-pibs)"],
"FAL 115 9106":["ICU License (ICU)"],
"FAL 115 9107":["Independent JPEG Group License (IJG)"],
"FAL 115 9108":["ImageMagick License (ImageMagick)"],
"FAL 115 9109":["iMatix Standard Function Library Agreement (iMatix)"],
"FAL 115 9110":["Imlib2 License (Imlib2)"],
"FAL 115 9111":["Info-ZIP License (Info-ZIP)"],
"FAL 115 9112/1":["Intel Open Source License (Intel)"],
"FAL 115 9112/2":["Intel ACPI Software License Agreement (Intel-ACPI)"],
"FAL 115 9113/10":["Interbase Public License v1.0 (Interbase-1.0)"],
"FAL 115 9114":["IPA Font License (IPA)"],
"FAL 115 9115/10":["IBM Public License v1.0 (IPL-1.0)"],
"FAL 115 9116":["ISC License (ISC)"],
"FAL 115 9117/20":["JasPer License (JasPer-2.0)"],
"FAL 115 9118":["Japan Network Information Center License (JPNIC)"],
"FAL 115 9119":["JSON License (JSON)"],
"FAL 115 9120/12":["Licence Art Libre 1.2 (LAL-1.2)"],
"FAL 115 9120/13":["Licence Art Libre 1.3 (LAL-1.3)"],
"FAL 115 9121":["Latex2e License (Latex2e)"],
"FAL 115 9122":["Leptonica License (Leptonica)"],
"FAL 115 9123":["Lesser General Public License For Linguistic Resources (LGPLLR)"],
"FAL 115 9124":["libpng License (Libpng)"],
"FAL 115 9125/20":["PNG Reference Library version 2 (libpng-2.0)"],
"FAL 115 9126/10":["libselinux public domain notice (libselinux-1.0)"],
"FAL 115 9127":["libtiff License (libtiff)"],
"FAL 115 9128/1":["Licence Libre du Quebec Permissive version 1.1 (LiLiQ-P-1.1)"],
"FAL 115 9128/2":["Licence Libre du Quebec Reciprocite version 1.1 (LiLiQ-R-1.1)"],
"FAL 115 9128/3":["Licence Libre du Quebec Reciprocite forte version 1.1"],
"FAL 115 9129":["Linux Kernel Variant of OpenIB.org license (Linux-OpenIB)"],
"FAL 115 9130/10":["Lucent Public License Version 1.0 (LPL-1.0)"],
"FAL 115 9130/102":["Lucent Public License v1.02 (LPL-1.02)"],
"FAL 115 9131/10":["LaTeX Project Public License v1.0 (LPPL-1.0)"],
"FAL 115 9131/11":["LaTeX Project Public License v1.1 (LPPL-1.1)"],
"FAL 115 9131/12":["LaTeX Project Public License v1.2 (LPPL-1.2)"],
"FAL 115 9131/13A":["LaTeX Project Public License v1.3a (LPPL-1.3a)"],
"FAL 115 9131/13C":["LaTeX Project Public License v1.3c (LPPL-1.3c)"],
"FAL 115 9132":["MakeIndex License (MakeIndex)"],
"FAL 115 9133":["The MirOS Licence (MirOS)"],
"FAL 115 9134":["Motosoto License (Motosoto)"],
"FAL 115 9135":["mpich2 License (mpich2)"],
"FAL 115 9136":["Microsoft Reciprocal License (MS-RL)"],
"FAL 115 9137":["Matrix Template Library License (MTLL)"],
"FAL 115 9138/10":["Mulan Permissive Software License], Version 1 (MulanPSL-1.0)"],
"FAL 115 9138/20":["Mulan Permissive Software License], Version 2 (MulanPSL-2.0)"],
"FAL 115 9139":["Multics License (Multics)"],
"FAL 115 9140":["Mup License (Mup)"],
"FAL 115 9141/13":["NASA Open Source Agreement 1.3 (NASA-1.3)"],
"FAL 115 9142":["Naumen Public License (Naumen)"],
"FAL 115 9143/10":["Net Boolean Public License v1 (NBPL-1.0)"],
"FAL 115 9144/20":["Non-Commercial Government Licence (NCGL-UK-2.0)"],
"FAL 115 9145":["University of Illinois/NCSA Open Source License (NCSA)"],
"FAL 115 9146":["Net-SNMP License (Net-SNMP)"],
"FAL 115 9147":["NetCDF license (NetCDF)"],
"FAL 115 9148":["Newsletr License (Newsletr)"],
"FAL 115 9149":["Nethack General Public License (NGPL)"],
"FAL 115 9150/10":["Norwegian Licence for Open Government Data (NLOD-1.0)"],
"FAL 115 9151":["No Limit Public License (NLPL)"],
"FAL 115 9152":["Nokia Open Source License (Nokia)"],
"FAL 115 9153":["Netizen Open Source License (NOSL)"],
"FAL 115 9154":["Noweb License (Noweb)"],
"FAL 115 9155/10":["Netscape Public License v1.0 (NPL-1.0)"],
"FAL 115 9155/11":["Netscape Public License v1.1 (NPL-1.1)"],
"FAL 115 9156/30":["Non-Profit Open Software License 3.0 (NPOSL-3.0)"],
"FAL 115 9157":["NRL License (NRL)"],
"FAL 115 9158":["NTP License (NTP)"],
"FAL 115 9158/1":["NTP No Attribution (NTP-0)"],
"FAL 115 9159/10":["Open Use of Data Agreement v1.0 (O-UDA-1.0)"],
"FAL 115 9160":["Open CASCADE Technology Public License (OCCT-PL)"],
"FAL 115 9161/20":["OCLC Research Public License 2.0 (OCLC-2.0)"],
"FAL 115 9162/10":["ODC Open Database License v1.0 (ODbL-1.0)"],
"FAL 115 9163/10":["Open Data Commons Attribution License v1.0 (ODC-By-1.0)"],
"FAL 115 9164/10":["SIL Open Font License 1.0 (OFL-1.0)"],
"FAL 115 9164/11":["SIL Open Font License 1.1 (OFL-1.1)"],
"FAL 115 9164/100":["SIL Open Font License 1.0 with no Reserved Font Name (OFL-1.0-no-RFN)"],
"FAL 115 9164/101":["SIL Open Font License 1.0 with Reserved Font Name (OFL-1.0-RFN)"],
"FAL 115 9164/111":["SIL Open Font License 1.1 with no Reserved Font Name (OFL-1.1-no-RFN)"],
"FAL 115 9164/112":["SIL Open Font License 1.1 with Reserved Font Name (OFL-1.1-RFN)"],
"FAL 115 9165/10":["OGC Software License], Version 1.0 (OGC-1.0)"],
"FAL 115 9166/10":["Open Government Licence v1.0 (OGL-UK-1.0)"],
"FAL 115 9166/20":["Open Government Licence v2.0 (OGL-UK-2.0)"],
"FAL 115 9166/30":["Open Government Licence v3.0 (OGL-UK-3.0)"],
"FAL 115 9166/C20":["Open Government Licence - Canada (OGL-Canada-2.0)"],
"FAL 115 9167":["Open Group Test Suite License (OGTSL)"],
"FAL 115 9168/11":["Open LDAP Public License v1.1 (OLDAP-1.1)"],
"FAL 115 9168/12":["Open LDAP Public License v1.2 (OLDAP-1.2)"],
"FAL 115 9168/13":["Open LDAP Public License v1.3 (OLDAP-1.3)"],
"FAL 115 9168/14":["Open LDAP Public License v1.4 (OLDAP-1.4)"],
"FAL 115 9168/20":["Open LDAP Public License v2.0 (or possibly 2.0A and 2.0B) (OLDAP-2.0)"],
"FAL 115 9168/21":["Open LDAP Public License v2.1 (OLDAP-2.1)"],
"FAL 115 9168/22":["Open LDAP Public License v2.2 (OLDAP-2.2)"],
"FAL 115 9168/23":["Open LDAP Public License v2.3 (OLDAP-2.3)"],
"FAL 115 9168/24":["Open LDAP Public License v2.4 (OLDAP-2.4)"],
"FAL 115 9168/25":["Open LDAP Public License v2.5 (OLDAP-2.5)"],
"FAL 115 9168/26":["Open LDAP Public License v2.6 (OLDAP-2.6)"],
"FAL 115 9168/27":["Open LDAP Public License v2.7 (OLDAP-2.7)"],
"FAL 115 9168/28":["Open LDAP Public License v2.8 (OLDAP-2.8)"],
"FAL 115 9168/201":["Open LDAP Public License v2.0.1 (OLDAP-2.0.1)"],
"FAL 115 9168/221":["Open LDAP Public License v2.2.1 (OLDAP-2.2.1)"],
"FAL 115 9168/222":["Open LDAP Public License 2.2.2 (OLDAP-2.2.2)"],
"FAL 115 9169":["Open Market License (OML)"],
"FAL 115 9170":["OpenSSL License (OpenSSL)"],
"FAL 115 9171/10":["Open Public License v1.0 (OPL-1.0)"],
"FAL 115 9172/21":["OSET Public License version 2.1 (OSET-PL-2.1)"],
"FAL 115 9173/10":["Open Software License 1.0 (OSL-1.0)"],
"FAL 115 9173/11":["Open Software License 1.1 (OSL-1.1)"],
"FAL 115 9173/20":["Open Software License 2.0 (OSL-2.0)"],
"FAL 115 9173/21":["Open Software License 2.1 (OSL-2.1)"],
"FAL 115 9173/30":["Open Software License 3.0 (OSL-3.0)"],
"FAL 115 9174/6":["The Parity Public License 6.0.0 (Parity-6.0.0)"],
"FAL 115 9174/7":["The Parity Public License 7.0.0 (Parity-7.0.0)"],
"FAL 115 9175/10":["ODC Public Domain Dedication & License 1.0 (PDDL-1.0)"],
"FAL 115 9176/30":["PHP License v3.0 (PHP-3.0)"],
"FAL 115 9176/301":["PHP License v3.01 (PHP-3.01)"],
"FAL 115 9177":["Plexus Classworlds License (Plexus)"],
"FAL 115 9178/1":["PolyForm Noncommercial License 1.0.0 (PolyForm-Noncommercial-1.0.0)"],
"FAL 115 9178/2":["PolyForm Small Business License 1.0.0 (PolyForm-Small-Business-1.0.0)"],
"FAL 115 9179/20":["Python Software Foundation License 2.0 (PSF-2.0)"],
"FAL 115 9180":["psfrag License (psfrag)"],
"FAL 115 9181":["psutils License (psutils)"],
"FAL 115 9182":["Qhull License (Qhull)"],
"FAL 115 9183/10":["Q Public License 1.0 (QPL-1.0)"],
"FAL 115 9184":["Rdisc License (Rdisc)"],
"FAL 115 9185/11":["Red Hat eCos Public License v1.1 (RHeCos-1.1)"],
"FAL 115 9186/11":["Reciprocal Public License 1.1"],
"FAL 115 9186/15":["Reciprocal Public License 1.5 (RPL-1.5)"],
"FAL 115 9187/10":["RealNetworks Public Source License v1.0 (RPSL-1.0)"],
"FAL 115 9188":["RSA Message-Digest License (RSA-MD)"],
"FAL 115 9189":["Ricoh Source Code Public License (RSCPL)"],
"FAL 115 9190":["Sax Public Domain Notice (SAX-PD)"],
"FAL 115 9191":["Saxpath License (Saxpath)"],
"FAL 115 9192":["SCEA Shared Source License (SCEA)"],
"FAL 115 9193":["Sendmail License (Sendmail)"],
"FAL 115 9193/1":["Sendmail License 8.23 (Sendmail-8.23)"],
"FAL 115 9194/10":["SGI Free Software License B v1.0 (SGI-B-1.0)"],
"FAL 115 9194/11":["SGI Free Software License B v1.1 (SGI-B-1.1)"],
"FAL 115 9194/20":["SGI Free Software License B v2.0 (SGI-B-2.0)"],
"FAL 115 9195/05":["Solderpad Hardware License v0.5 (SHL-0.5)"],
"FAL 115 9195/051":["Solderpad Hardware License], Version 0.51 (SHL-0.51)"],
"FAL 115 9196/20":["Simple Public License 2.0 (SimPL-2.0)"],
"FAL 115 9197/11":["Sun Industry Standards Source License v1.1 (SISSL)"],
"FAL 115 9197/12":["Sun Industry Standards Source License v1.2 (SISSL-1.2)"],
"FAL 115 9198":["Standard ML of New Jersey License (SMLNJ)"],
"FAL 115 9199":["Secure Messaging Protocol Public License (SMPPL)"],
"FAL 115 9200/11":["SNIA Public License 1.1 (SNIA)"],
"FAL 115 9201/1":["Spencer License 86 (Spencer-86)"],
"FAL 115 9201/2":["Spencer License 94 (Spencer-94)"],
"FAL 115 9201/3":["Spencer License 99 (Spencer-99)"],
"FAL 115 9202/10":["Sun Public License v1.0 (SPL-1.0)"],
"FAL 115 9203/1":["SSH OpenSSH license (SSH-OpenSSH)"],
"FAL 115 9203/2":["SSH short notice (SSH-short)"],
"FAL 115 9204/10":["Server Side Public License], v 1 (SSPL-1.0)"],
"FAL 115 9205/113":["SugarCRM Public License v1.1.3 (SugarCRM-1.1.3)"],
"FAL 115 9206":["Scheme Widget Library (SWL) Software License Agreement (SWL)"],
"FAL 115 9207/10":["TAPR Open Hardware License v1.0 (TAPR-OHL-1.0)"],
"FAL 115 9208":["TCL/TK License (TCL)"],
"FAL 115 9209":["TCP Wrappers License (TCP-wrappers)"],
"FAL 115 9210":["TMate Open Source License (TMate)"],
"FAL 115 9211/11":["TORQUE v2.5+ Software License v1.1 (TORQUE-1.1)"],
"FAL 115 9212":["Trusster Open Source License (TOSL)"],
"FAL 115 9213/10":["Technische Universitaet Berlin License 1.0 (TU-Berlin-1.0)"],
"FAL 115 9213/11":["Technische Universitaet Berlin License 2.0 (TU-Berlin-2.0)"],
"FAL 115 9213/20":["Technische Universitaet Berlin License 2.0"],
"FAL 115 9214/10":["Upstream Compatibility License v1.0 (UCL-1.0)"],
"FAL 115 9215/1":["Unicode License Agreement - Data Files and Software (2015) (Unicode-DFS-2015)"],
"FAL 115 9215/2":["Unicode License Agreement - Data Files and Software (2016) (Unicode-DFS-2016)"],
"FAL 115 9216":["Unicode Terms of Use (Unicode-TOU)"],
"FAL 115 9217":["The Unlicense (Unlicense)"],
"FAL 115 9218/10":["Universal Permissive License v1.0 (UPL-1.0)"],
"FAL 115 9219":["Vim License (Vim)"],
"FAL 115 9220":["VOSTROM Public License for Open Source (VOSTROM)"],
"FAL 115 9221/10":["Vovida Software License v1.0 (VSL-1.0)"],
"FAL 115 9222/10":["Sybase Open Watcom Public License 1.0 (Watcom-1.0)"],
"FAL 115 9223":["Wsuipa License (Wsuipa)"],
"FAL 115 9224":["Do What The F*ck You Want To Public License (WTFPL)"],
"FAL 115 9225":["X11 License (X11)"],
"FAL 115 9226":["Xerox License (Xerox)"],
"FAL 115 9227/11":["XFree86 License 1.1 (XFree86-1.1)"],
"FAL 115 9228":["xinetd License (xinetd)"],
"FAL 115 9229":["X.Net License (Xnet)"],
"FAL 115 9230":["XPP License (xpp)"],
"FAL 115 9231":["XSkat License (XSkat)"],
"FAL 115 9232/10":["Yahoo! Public License v1.0 (YPL-1.0)"],
"FAL 115 9232/11":["Yahoo! Public License v1.1 (YPL-1.1)"],
"FAL 115 9233":["Zed License (Zed)"],
"FAL 115 9234/20":["Zend License v2.0 (Zend-2.0)"],
"FAL 115 9235/13":["Zimbra Public License v1.3 (Zimbra-1.3)"],
"FAL 115 9235/14":["Zimbra Public License v1.4 (Zimbra-1.4)"],
"FAL 115 9236":["zlib License (Zlib)"],
"FAL 115 9236/1":["zlib/libpng License with Acknowledgement (zlib-acknowledgement)"],
"FAL 115 9237/11":["Zope Public License 1.1 (ZPL-1.1)"],
"FAL 115 9237/20":["Zope Public License 2.0 (ZPL-2.0)"],
"FAL 115 9237/21":["Zope Public License 2.1 (ZPL-2.1)"],
"FAL 115 9238":["Nunit License (Nunit)"],
"FAL 115 9239":["wxWindows Library License (wxWindows)"],
"FAL 115 9240/20":["eCos license version 2.0 (eCos-2.0)"],
"FAL 115 9241/10":["Eclipse Distribution License 1.0", "Eclipse Distribution License 1.0 (EDL-1.0)"],
"FAL 115 9242":["Matplotlib"],
"FAL 115 9243":["Unicode"],
"FAL 115 9244":["Original SSLeay License"],
"FAL 115 9245":["UnboundID LDAP SDK Free Use License"],
"FAL 115 9246":["Repoze Public License"],
"FAL 115 9247":["libgd License 2018"],
"FAL 115 9248":["Apache License v2.0 with LLVM Exceptions (Apache-2.0 WITH LLVM-exception)"],
"FAL 115 9249":["w3m License (w3m)"],
"FAL 115 9250":["The ACE License"],
"FAL 115 9251":["GNU General Public License v2.0 or later with Linux-syscall-note (GPL-2.0-or-later WITH Linux-syscall-note)"],
"FAL 115 9252":["HTML Tidy License (HTMLTIDY)"],
"FAL 115 9253":["HSQLDB License"],
"FAL 115 9254":["JDOM License"],
"FAL 115 9255":["BSD 2-Clause with views sentence (BSD-2-Clause-Views)"],
"FAL 115 9256":["GNU General Public License v2.0 or later with Bootloader Distribution Exception (GPL-2.0-or-later WITH Bootloader-exception)"],
"FAL 115 9257":["GNU General Public License v2.0 only with Universal FOSS Exception Version 1.0 (GPL-2.0-only WITH Universal-FOSS-exception-1.0)"],
"FAL 115 9258":["Unicode License v3 (Unicode-3.0)"],
"FAL 115 9993":["Public Domain"],
"FAL 115 9994":["Non FOSS Freeware"],
"FAL 115 9995":["Tools], Free Runtime"],
"FAL 115 9995/05":["Solderpad Hardware License v0.5"],
"FAL 115 9996":["HdS-tracking (only 3PPT internal statistical usage) Hardware-dependent SW"],
"FAL 115 9997":["SW bundled with HW], not handled by SW Supply (only 3PPT internal statistical usage) Hardware-dependent SW"],
"FAL 115 9998":["Dual/Triple-License"],
"FAL 115 9999":["FAL structure not defined (not handled by Supply)"],
    }
    for key, value in licensesDict.items():
        if license in value:
            return key + " (" + value[0] + ")"
    return "Product Number missing" 


