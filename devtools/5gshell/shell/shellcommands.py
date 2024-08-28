import logging
import subprocess
import shlex
import io
from utils import printer
from common import commands5g
import click as c


def shellcommand(func):
    def shell_wrapper(*args, **kwargs):
        command = func(*args, **kwargs)
        logging.debug("Shell command: " + command)
        
        try:
            proc = subprocess.Popen(shlex.split(command),
                                    shell=False,
                                    stdin=subprocess.PIPE,
                                    stdout=subprocess.PIPE,
                                    stderr=subprocess.PIPE)
            res, error = proc.communicate(input=args[0].encode())
            result = res.decode()
            err = error.decode()
            logging.debug("Result: " + result)
            if err:
                printer.error("Shell error: " + err.strip())
                return ""
            return result.strip().split("\n")
        except KeyboardInterrupt:
            pass
    return shell_wrapper


@shellcommand
def awk(input, string):
    return "awk '/" + string + "/ { print $1 }'"


@shellcommand
def grep(input, string):
    return "grep \"" + string + "\""
