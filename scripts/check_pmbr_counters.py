#!/bin/python

import re
import json
import sys
import xml.etree.ElementTree as ET
from xml.dom import minidom

PROTO_DIR=sys.path[0] + '/../'

jsonBsf = 'esc/bsf/bsfmgr/src/main/resources/pmbr/configs/group.json'
xmlBsf = 'enm/yang_data/pm/ericsson-bsf-pm-group-instances.xml'

jsonRlf = 'esc/rlf/src/main/resources/pmbr/configs/group.json'
xmlRlf = 'enm/yang_data/pm/ericsson-rlf-pm-group-instances.xml'

jsonScp = 'esc/scp/scpmgr/src/main/resources/pmbr/configs/group.json'
xmlScp = 'enm/yang_data/pm/ericsson-scp-pm-group-instances.xml'

jsonSepp = 'esc/sepp/seppmgr/src/main/resources/pmbr/configs/group.json'
xmlSepp = 'enm/yang_data/pm/ericsson-sepp-pm-group-instances.xml'



def findDiffs(xmlFile, jsonFile):
    jsonCounters=set()
    xmlCounters=set()
    patternXml = re.compile("<id>([A-Za-z0-9_]*)<\/id>")

    for i, line in enumerate(open(PROTO_DIR + xmlFile)):
        for match in re.finditer(patternXml, line):
            xmlCounters.add(match.group(1))
    with open(PROTO_DIR + jsonFile) as ff:
        jsonObj=json.load(ff)
        for i in jsonObj:
            for j in i['measurement-type']:
                jsonCounters.add(j['id'])

    print 'sum of json counters in '+ jsonFile + ': ' + str(len(jsonCounters))
    print 'sum of xml counters in '+ xmlFile + ': ' + str(len(xmlCounters)) + '\n'
    print '>>>>> Counters contained in .xml and not in .json: ' + str(len(xmlCounters - jsonCounters)) + ' <<<<<'
    for  i in (xmlCounters-jsonCounters):
        print i

    print '\n>>>>> Counters contained in .json and not in .xml: ' + str(len(jsonCounters - xmlCounters)) + ' <<<<<'
    for i in (jsonCounters-xmlCounters):
        print i

def fixDiffs(xmlFile, jsonFile):
    with open(PROTO_DIR + jsonFile) as jsonFilef:
        jsonObj=json.load(jsonFilef)
        tree = ET.parse(PROTO_DIR + xmlFile)
        root = tree.getroot()
        description = {}
        ns = {'p': 'urn:rdns:com:ericsson:oammodel:ericsson-pm', 'c': "http://tail-f.com/ns/config/1.0"}
        for group in root.findall('p:pm/p:group', ns):
            try:
                n = group.find('p:name', ns).text
                d = group.find('p:description', ns).text
                description[n] = d
            except:
                print("cannot find " + group.tag)
        root = ET.Element("config", xmlns="http://tail-f.com/ns/config/1.0")
        pm = ET.SubElement(root, "pm", xmlns="urn:rdns:com:ericsson:oammodel:ericsson-pm")
        for i in jsonObj:
            group = ET.SubElement(pm, "group")
            ET.SubElement(group, "name").text = str(i['name'])
            try:
                ET.SubElement(group, "description").text = description[str(i['name'])]
            except KeyError:
                print("can not find: " + i['name'])
            ET.SubElement(group, "validity").text = 'true'
            ET.SubElement(group, "version").text = str(i['version'])
            for j in i['measurement-type']:
                mt = ET.SubElement(group, "measurement-type")
                ET.SubElement(mt, 'id').text = str(j['id'])
                ET.SubElement(mt, 'measurement-name').text = str(j['measurement-name'])
                ET.SubElement(mt, 'size').text = str(j['size'])
                ET.SubElement(mt, 'collection-method').text = str(j['collection-method'])
                ET.SubElement(mt, 'description').text = j['description']
                ET.SubElement(mt, 'condition').text = str(j['condition'])
                ET.SubElement(mt, 'aggregation').text = str(j['aggregation'])
                ET.SubElement(mt, 'measurement-status').text = str(j['measurement-status'])
                ET.SubElement(mt, 'multiplicity').text = str(j['multiplicity'])
                ET.SubElement(mt, 'reset-at-gran-period').text = str(j['reset-at-gran-period']).lower()
        xmlstr = minidom.parseString(ET.tostring(root)).toprettyxml(indent="    ", encoding='UTF-8')
        with open(PROTO_DIR + xmlFile, "w") as f:
            f.write(xmlstr)



def main():
    print '\n#############################'
    print '############ BSF ############'
    print '#############################\n'
    findDiffs(xmlBsf,jsonBsf)
    #fixDiffs(xmlBsf,jsonBsf)
    print '#############################'
    print '############ RLF ############'
    print '#############################\n'
    findDiffs(xmlRlf,jsonRlf)
    #fixDiffs(xmlRlf, jsonRlf)
    print '#############################'
    print '############ SCP ############'
    print '#############################\n'
    findDiffs(xmlScp,jsonScp)
    #fixDiffs(xmlScp, jsonScp)
    print '\n#############################'
    print '############ SEPP ###########'
    print '#############################\n'
    findDiffs(xmlSepp,jsonSepp)
    #fixDiffs(xmlSepp, jsonSepp)



if __name__ == "__main__":
    main()
