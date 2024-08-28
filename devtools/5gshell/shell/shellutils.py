import readline
from common import commands5g
import click as c
from utils import printer
from pyfiglet import Figlet


COMMANDS = []
# List of commands
for f in dir(commands5g):
    func = getattr(commands5g, f)
    if isinstance(func, c.Command):
        COMMANDS.append(func)

prompt_name = "5Gshell"
prompt = prompt_name + "> "


def graffiti_time():
    figure = Figlet(font='roman')
    printer.empty()
    printer.justprint(figure.renderText('5Gshell'))


def client_help():
    print
    for command in COMMANDS:
        printer.justprint(command.name)


def welcome():
    printer.important(" *** For support please contact Konstantina Karponi,"
                      " karkon@intracom-telecom.com *** ")
    printer.empty()


def autocomplete(text, state):
    line = readline.get_line_buffer()
    if not line:
        return [c.name + " " for c in COMMANDS][state]
    else:
        return [c.name +
                " " for c in COMMANDS if c.name.startswith(line)][state]


def set_autocomplete():
    readline.set_completer_delims('\t')
    readline.parse_and_bind("tab: complete")
    readline.set_completer(autocomplete)


def process_command(command):
    """
    Cleans up user command from spaces
    and returns command's function name and parameters
    """
    comm_split = command.split(" ")
    comm_split = list(filter(None, comm_split))
    map(str.strip, comm_split)

    comm_name = comm_split[0].replace("-", "_")
    return comm_name, comm_split[1:]