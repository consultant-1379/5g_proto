import csv
import logging

LOG = logging.getLogger(__name__)
logging.basicConfig(format="%(asctime)s %(levelname)s %(message)s", level=logging.INFO)


def create_csv_metrics(group_json_data, name="test_metric"):
    with open(name + '.csv', "w") as csv_f:
        csv_writer = csv.writer(csv_f, quoting=csv.QUOTE_ALL)
        csv_header_row = ['Internal Metric Name', 'Group', 'Description', 'Collection Method',
                          'Corresponding Measurement Type', 'Internal Target Pattern']
        csv_writer.writerow(csv_header_row)

        for pm_group in sorted(group_json_data, key=lambda d: d['name']):
            for pm_mt in pm_group['measurement-type']:
                try:
                    csv_writer.writerow([
                        pm_mt['internal-metric-name'],
                        pm_group['name'],
                        pm_mt['description'],
                        pm_mt['collection-method'],
                        pm_mt['measurement-name'],
                        pm_mt.get('internal-target-pattern', ""),
                    ])
                except Exception as ex:
                    LOG.error("Can not write metric for " + pm_mt['internal-metric-name'])
                    LOG.error(ex)
