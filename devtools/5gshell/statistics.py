import logging
import os
import printer


def get_full_list(workers):
    for worker in workers:
        cmd = "kubectl exec --namespace 5g-bsf-$USER -t " + worker + " -- curl localhost:9901/stats"
        logging.debug("Get stats for worker: %s", worker)
        stats = os.popen(cmd).read().strip()
        printer.justprint(stats)
        
def print_statistics(statistics, workers):
    stats = statistics.strip().split(",")
    for worker in workers:
        cmd = "kubectl exec --namespace 5g-bsf-$USER -t " + worker + " -- curl localhost:9901/stats | grep -w"
        for stat in stats:
            cmd = cmd + " -e \"^" + stat.strip() + "\""
        logging.debug("Get stats %s for worker: %s", cmd, worker)
        stats = os.popen(cmd).read().strip()
        printer.important(stats)

def get_statistic_number(stat, worker):
    stat.strip()
    cmd = "kubectl exec --namespace 5g-bsf-$USER -t " + worker + " -- curl localhost:9901/stats | grep -we \"^" + stat.strip() + "\" | awk {'print $2'}"
    logging.debug("Get stats %s for worker: %s", cmd, worker)
    stats = os.popen(cmd).read().strip()
    return stats

