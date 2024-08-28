import copy
import logging
import re

LOG = logging.getLogger(__name__)
logging.basicConfig(format="%(asctime)s %(levelname)s %(message)s", level=logging.INFO)

headers = {
    'title': "Internal PM Counters to External Representation",
    'abstract': "Introduction",
    'groups': 'PM Groups',
    'type': "Measurement Types",
    'metrics': "Internal Metrics",
    'jobs': {
        'measurement-job': "System-Defined PM Measurement Jobs",
        'threshold-job': "System-Defined PM Threshold Jobs"
    }
}


def is_valid(group):
    invalid_groups = [
        "SGSN-MME_ICMP_RouterInstance",
        "SGSN-MME_ICMPv6_RouterInstance",
        "SGSN-MME_IP_PIU",
        "SGSN-MME_IP_RouterInstance",
        "SGSN-MME_IPv6_PIU",
        "SGSN-MME_IPv6_RouterInstance"]
    return group not in invalid_groups


def fix_formatting(string):
    # Remove ## tags from descriptions. For example, #AMF# -> AMF
    string = re.sub('#([A-Za-z\-]+)#', r'\1', string)

    return string.replace("; ", "\n\n")  # ; used as line separator in some description strings


def makelink(heading):
    link = '#' + heading.lower() \
        .replace(' ', '-') \
        .replace('/', '') \
        .replace('.', '') \
        .replace('{', '') \
        .replace(',', '') \
        .replace('}', '') \
        .replace('=', '') \
        .replace(':', '') \
        .replace('\\', '')
    return link


# creates a link to id
def make_reference(heading):
    return "[{}]({})".format(heading, makelink(heading))


# creates an id to reference. Should be always only ONE id
def make_id_to_reference(heading):
    return "{}{}{}".format('{', makelink(heading), '}')


#######################################################
# get list of group index labels, from a mixed list of labels
def get_group_labels(labels):
    all_labels = labels.split(',')
    group_labels = ""
    for l in all_labels:
        if l != '' and '=' not in l:
            group_labels += l + ' '
    return group_labels.strip()


#######################################################
# create list of labels
def get_labels(meas_type):
    labels = ""
    if 'internal-instrumentation-label' in meas_type:
        for label, value in meas_type['internal-instrumentation-label'].iteritems():
            if len(labels) > 1:
                labels += ","
            if value == "":
                labels += "{}".format(label)  # group index labels are shown as 'label'
    return labels


def fix_square_brackets(internal_p):
    fixed_string = internal_p.lower() \
        .replace('[', '\[') \
        .replace(']', '\]')
    return fixed_string


def create_internal_metric_chapter(json_data, paragraph_start="#"):
    with open('test.md', "a") as md_f:
        md_f.write("\n\n{} {}\n\n".format(paragraph_start, headers['metrics']))

        prev_application_name = ""
        for pm_group in sorted(json_data, key=lambda d: d['component-name']):
            if not is_valid(pm_group['name']):
                continue

            if 'description' in pm_group:
                pm_group_description = fix_formatting(pm_group['description'])
            else:
                pm_group_description = "-"
            application_name = pm_group['component-name']
            if application_name != prev_application_name:
                prev_application_name = application_name
                md_f.write("\n\n{} {}\n\n".format(paragraph_start + "#", fix_formatting(application_name)))

            md_f.write(
                "\n\n{} {}\n\n".format(paragraph_start + "##", pm_group['name'])) #make_id_to_reference(pm_group['name'])))

            if 'description' in pm_group:
                md_f.write("\n{}\n".format(pm_group_description))

            # it could be several measurement-names for one internal-metric-name, need to combine them
            temp = []
            it = iter(sorted(pm_group['measurement-type'], key=lambda x: x['internal-metric-name']))
            try:
                value = None
                while True:
                    if not value:
                        value = copy.deepcopy(next(it))
                    next_value = copy.deepcopy(next(it))
                    if value['internal-metric-name'] == next_value['internal-metric-name']:
                        value['measurement-name'] += "; "
                        value['measurement-name'] += next_value['measurement-name']
                        value['internal-target-pattern'] += "; "
                        value['internal-target-pattern'] += next_value['internal-target-pattern']
                    else:
                        temp.append(value)
                        value = next_value
            except StopIteration:
                temp.append(value)
                pass
            except KeyError:
                print("KeyError for key" + value['internal-metric-name'])
                temp.append(value)
                pass
            finally:
                del it

            # write filtered measurement-type
            for pm_mt in temp:
                group_and_in = "{}/{}".format(pm_group['name'], pm_mt['internal-metric-name'])
                # group_and_in id should not be equal measurement-name to create a reference
                # sometimes pm_mt['id'] == pm_mt['internal-metric-name'] == pm_mt['measurement-name']
                md_f.write("\n\n{} {}\n\n".format(
                    paragraph_start + "###",
                    group_and_in,
                    ))

                measurement_types = ""
                for item in pm_mt['measurement-name'].split():
                    group_and_mn = "{}/{}".format(pm_group['name'], item)
                    # group_and_in id should not be equal group_and_mn to create a reference
                    measurement_types += " {} ".format(group_and_mn)

                try:
                    md_f.write(
                        "| Internal Metric Name | {} |\n"
                        "| :------- | :------- |\n"
                        "| Group | {} |\n"
                        "| Description | {} |\n"
                        "| Collection Method | {} |\n"
                        "| Corresponding Measurement Type | {} |\n"
                        "| Internal Target Pattern | {} |\n".format(
                            pm_mt['internal-metric-name'],
                            pm_group['name'],
                            fix_formatting(pm_mt['description']),
                            pm_mt['collection-method'],
                            measurement_types,
                            fix_square_brackets(pm_mt.get('internal-target-pattern', "")),
                        ))
                except Exception as ex:
                    LOG.error("Can not write metric for " + pm_mt['internal-metric-name'])
                    LOG.error(ex)


def create_measurement_name_chapter(json_data, paragraph_start="#"):
    with open('test.md', "a") as md_f:
        md_f.write("\n\n{} {}\n\n".format(paragraph_start, headers['type']))
        for pm_group in sorted(json_data, key=lambda d: d['name']):
            for pm_mt in sorted(pm_group['measurement-type'], key=lambda d: d['measurement-name']):
                group_and_in = "{}/{}".format(pm_group['name'], pm_mt['internal-metric-name'])
                group_and_mn = "{}/{}".format(pm_group['name'], pm_mt['measurement-name'])
                # group_and_in id should not be equal group_and_mn to create a reference
                md_f.write("\n\n{} {}\n\n".format(
                    paragraph_start + "#",
                    group_and_mn,
                    ))
                md_f.write(
                    "| Measurement Type | {} |\n"
                    "| :------- | :------- |\n"
                    "| Corresponding Internal Name | {} |\n".format(
                        pm_mt['measurement-name'],
                        group_and_in
                    ))


def create_job_chapter(json_data, job_type, paragraph_start="#"):
    TABLE_COLUMNS = 3
    number_of_jobs = sum(map(lambda x: x["type"] == job_type, json_data))
    # Decide number of columns for table
    if number_of_jobs < TABLE_COLUMNS:
        columns = number_of_jobs
    else:
        columns = TABLE_COLUMNS

    with open('test.md', "a") as md_f:
        md_f.write("\n\n{} {}\n\n".format(paragraph_start, headers['jobs'][job_type]))
        md_f.write("{0}|\n{1}|\n".format(''.join(['| ' for _ in range(0, columns)]),
                                         ''.join(['| :---- ' for _ in range(0, columns)])))

        items = 0  # multiple items per row
        for pm_job in sorted((f for f in json_data if f["type"] == job_type), key=lambda d: d['name']):
            md_f.write("| {} ".format(
                pm_job['name']
            ))
            items += 1
            if items % columns == 0:
                md_f.write("|\n")
        if items % columns:
            while items % columns:
                md_f.write("| ")  # complete the table
                items += 1
            md_f.write("|\n")

        for pm_job in sorted((f for f in json_data if f["type"] == job_type), key=lambda d: d['name']):
            md_f.write(
                "\n\n{} {}\n\n".format(paragraph_start + "#", pm_job['name'])) #make_id_to_reference(pm_job['name'])))
            md_f.write(
                "| Name | {} |\n| :---- | :---- |\n| Type | {} |\n| Granularity Period | {} |\n| Default State | {} |\n".format(
                    pm_job['name'],
                    pm_job['type'],
                    pm_job['granularity-period'],
                    pm_job['requested-job-state']
                ))

            for mt_reader in pm_job['measurement-reader']:
                if 'threshold-monitoring' in mt_reader:
                    md_f.write(
                        "| Threshold Name | {} |\n| High Threshold | {} |\n| Low Threshold | {} |\n| Alarm Severity | "
                        "{} |\n".format(
                            mt_reader['threshold-monitoring'][0]['name'],
                            mt_reader['threshold-monitoring'][0]['threshold-high'],
                            mt_reader['threshold-monitoring'][0]['threshold-low'],
                            mt_reader['threshold-monitoring'][0]['threshold-severity'],
                        ))
                if 'measurement-type-ref' in mt_reader:
                    group_and_mt = "{}/{}".format(mt_reader['group-ref'], mt_reader['measurement-type-ref'])
                    md_f.write("| PM Group/Measurement Type | {} |\n".format(
                        group_and_mt
                    ))
                else:
                    md_f.write("| PM Group | {} |\n".format(
                        mt_reader['group-ref']
                    ))


def create_md(group_json_data, job_json_data, args):
    with open('test.md', "w") as md_f:
        md_f.write("# {}\n\n".format(headers['title']))
    create_internal_metric_chapter(group_json_data, "##")
    create_measurement_name_chapter(group_json_data, "##")

    create_job_chapter(job_json_data, "measurement-job", "##")
    create_job_chapter(job_json_data, "threshold-job", "##")
