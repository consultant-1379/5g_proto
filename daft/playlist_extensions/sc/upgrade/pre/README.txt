This directory contains scripts and configuration files that will be executed
prior to Upgrade of the SC.

If there is nothing to execute, this directory can be left empty except for
this file.

If there is something to be executed, then one or more files with the following
file extension must be present:
    - .apply
      Files with this file extension should either be, a script file like a
      bash or perl script, or be a command that points to another file in the
      same directory.
      This file will be executed during a deployment or an upgrade.
    - .undo
      Files with this file extension should either be, a script file like a
      bash or perl script, or be a command that points to another file in the
      same directory.
      This file will be executed in case there is a failure during a deployment
      or an upgrade and will attempt to undo any changes done by the .apply
      file.
If there is a file with the same name as the .apply or .undo file but with the
extension .config then this file contains filters that might give instructions
to how the .apply or .undo file should be executed or filters that is used to
determine if the file should be executed or not depending one e.g. software base
line of the from/to release.

These .apply, .undo and .config files MUST FOLLOW a special naming convention
to indicate where the commands should be executed.
The filename should have one of the following suffixes:
    - _localhost
       or
      _DAFT
      Files with this suffix will be executed on the same computer as where
      the DAFT framework is running on.
      Example: 01_Do_Some_Cleanup_localhost.apply or 02_Printouts_DAFT.apply

Any other files in this directory will be ignored unless they are called from
the .apply or .undo files.
Observe that files that should be executed should have the -x (execute) file
permission.
