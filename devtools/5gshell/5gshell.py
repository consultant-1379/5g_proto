#!/usr/bin/python3

import logging
import sys
from shell import shellutils
from common import commands5g
import click as c
from utils import printer


def shell():
    shellutils.graffiti_time()
    shellutils.welcome()
    
    while(True):

        shellutils.set_autocomplete()
        try:
            user_command = input(shellutils.prompt)
        except KeyboardInterrupt:
            printer.empty()
            continue

        # enter key
        if user_command == "":
            continue

        comm_name, comm_params = shellutils.process_command(user_command)

        try:
            getattr(commands5g, comm_name)(comm_params, standalone_mode=False)
        except c.ClickException as e:
            printer.justprint(e.show())
        except KeyboardInterrupt:
            printer.empty()
            continue
        except AttributeError:
            printer.justprint("Unknown command.")
        

if __name__ == "__main__":
    shell()
