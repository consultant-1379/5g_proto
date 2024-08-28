The files in this directory are used by the daft_sequencer.pl script that can be
used to automate execution of multiple commands and playlists.

You can use a number of tags to control how the execution of the commands will
be done. There should be one tag per line and it should start at the first non
white space position.

The currently supported tags are as follows:

- #
  This is a comment line and anything after the # is ignored during the execution.

- BACKGROUND_COMMAND: <command with parameters>
  This will execute a command in the background, i.e. the command is started in the background
  and then execution of the next command starts immediately.
  When all commands has been executed and there are still background commands running the script
  with either, wait for the background commands to finish normally when the parameter
  --background-handling=finish is used, or it will stop the background commands when the parameter
  --background-handling=stop is used.
  The default handling is to allow all background commands to finish naturally, but the maximum
  time that it waits can be specified with the parameter --max-wait-time with a default maximum
  time of 30 minutes, if set to 0 then it waits indefinitely for the commands to finish.

  Command output is hidden from the user.

- BASH_COMMAND: <command with parameters>
  The command will be executed in the foreground from a bash wrapper file which
  might be useful if the command contains pipes or redirections, the next command
  is executed when this command has finished.

- COMMAND: <command with parameters>
  This will execute a command in the foreground, i.e. it wait until the command has finished before
  starting the next command

  Command output is hidden from the user.

- ENVIRONMENT_VARIABLE: <name>=<value>
  Specifies an environment variable that will be set before executing any of the commands.

- PLAYLIST_VARIABLE: <name>=<value>
  Specifies default playlist variables that will be added to the end of the execute_playlist.pl
  command line unless the command line already contains that variable in which case that value
  will be used instead.
  The use for the parameter is to simplify the playlist execution commands so you don't need to
  specify all parameters that should be used for all commands, i.e. it makes the command line
  shorter and easier to read.

- ENVIRONMENT_VARIABLE: <name>=<value>
  Specifies an environment variable that will be set before executing any of the commands.

- IF_ENVIRONMENT_VAR <name>="<value>" [(BACKGROUND_COMMAND|BASH_COMMAND|COMMAND|SLEEP_WITH_PROGRESS|SHOW_COMMAND):] ....
  It the environment variable <name> contents match the Perl regular expression <value> then the
  command that follows will be executed, and if not matching then the command will be skipped.
  If the environment variable does not exist then the command will be skipped.

- IF_NOT_ENVIRONMENT_VAR <name>="<value>" [(BACKGROUND_COMMAND|BASH_COMMAND|COMMAND|SLEEP_WITH_PROGRESS|SHOW_COMMAND):] ....
  It the environment variable <name> contents does not match the Perl regular expression <value> then the
  command that follows will be executed, and if matching then the command will be skipped.
  If the environment variable does not exist then the command will be executed.

- SCRIPT_VARIABLE: <name>=<value>
  Set a specific variable used by this script which migth be useful if the loaded
  file should set a variable that should change the default behavior of a command
  line parameter e.g. --stop-on-error.
  Currently allowed values are:
    - SCRIPT_VARIABLE: background-handling=(finish|stop)
    - SCRIPT_VARIABLE: background-max-wait-time=<integer>[s|m|h]
    - SCRIPT_VARIABLE: color=(bright|cyan|dark|html|no)
    - SCRIPT_VARIABLE: jobname=<string>
    - SCRIPT_VARIABLE: log-file=<file path>
    - SCRIPT_VARIABLE: stop-on-error=(1|yes|true|0|no|false)
    - SCRIPT_VARIABLE: workspace=<directory path>

- SHOW_COMMAND: <command with parameters>
  This will execute a command in the foreground, i.e. it wait until the command has finished before
  starting the next command

  Command output is shown to the user (but only after the command has finished).

- SLEEP_WITH_PROGRESS: [seconds=<integer>] [minutes=<integer>] [hours=<integer>] [progress-interval=<integer>] [message="<string>"]
  This will start a sleep timeir for the amount of time specified with a progress message every
  'progress-interval' seconds and with optionally showing a 'message'.

The command to execute must exist in the PATH or it will fail, except for the execute_playlist.pl
or download_csar.pl commands who will be found using the same path as the current executing
daft_sequencer.pl script.

Any used environment variables in the commands must either already exist in the current running
environment or must be specified with the ENVIRONMENT_VARIABLE definition in this file, or be
specified by parameter -e or --environment-variable parameters to the daft_sequencer.pl script.
If this is not the case then the script will stop with an error,
