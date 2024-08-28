import click as c


def success(string):
    c.echo(c.style(string, fg='green'))


def error(string):
    c.echo(c.style(string, fg='red'))


def important(string):
    c.echo(c.style(string, fg='blue'))


def checkprint(string, code):
    if code in string:
        success(string)
    else:
        error(string)


def justprint(string):
    c.echo(string)


def empty():
    c.echo("")


def separator():
    c.echo("------------------------------------------------------------------"
           "-------------------------")
    empty()
