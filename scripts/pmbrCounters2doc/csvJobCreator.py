import csv


def create_csv_job(job_json_data, name="test_job"):
    with open(name + '.csv', "w") as csv_f:
        csv_writer = csv.writer(csv_f, quoting=csv.QUOTE_ALL)
        csv_header_row = ['Job', 'Job Type', 'Granularity Period', 'Default State', 'PM Group', 'PM Measurement Type',
                          'Threshold Name', 'High', 'Low', 'Severity']
        csv_writer.writerow(csv_header_row)

        for job_type in {"measurement-job", "threshold-job"}:
            for pm_job in sorted((f for f in job_json_data if f["type"] == job_type), key=lambda d: d['name']):
                for mt_reader in pm_job['measurement-reader']:
                    csv_row = [
                        pm_job['name'],
                        pm_job['type'],
                        pm_job['granularity-period'],
                        pm_job['requested-job-state'],
                        mt_reader['group-ref']
                    ]
                    if 'measurement-type-ref' in mt_reader:
                        csv_row += [mt_reader['measurement-type-ref']]
                    else:
                        csv_row += [""]
                    if 'threshold-monitoring' in mt_reader:
                        csv_row += [
                            mt_reader['threshold-monitoring'][0]['name'],
                            mt_reader['threshold-monitoring'][0]['threshold-high'],
                            mt_reader['threshold-monitoring'][0]['threshold-low'],
                            mt_reader['threshold-monitoring'][0]['threshold-severity']
                        ]
                    csv_writer.writerow(csv_row)
