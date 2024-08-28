******************************************************************
* IMPORTANT INFOMRATION REGARDING THE XML FILES IN THIS DIRECTOY *
******************************************************************

Before you modify any of the existing XML files in this directory and add new parameters to these files you
should be aware of the following:

  - If you add new parameters to one or more files in this directory and you want these parameters to be still
    present whenever these files are updated again you MUST add them to the template file as well.
    If you fail to update the template file then all these parameters will be gone on the next update.

  - If you only need to change the value of one of the following attributes then you can still just update the XML file and
    there is no need to update the template file because these attributes will be automatically copied the next time these
    files are updated.
    The following attribute values can safely be changed in the XML files:
      - value=""
      - groups=""
      - initial_password=""
      - password=""
      - password_expire=""
      - user=""

    The following attribute values MUST also be changed in the template file:
      - name=""
      - description=""
      - playlist=""
      - valid_releases=""


The proper way to add new parameters or update one of the "name", "description", "playlist" or "valid_releases" attributes is by
first adding/modifying the parameters in the network config template file and then update all node files from that file with just
one command.

So the procedure would be something like this:

1. Do the changes to the “5g_proto/daft/templates/Network_Config_Template.xml” file.

    a. If you want to add new parameters, then add them in the proper place in the file.

    b. Remember to always leave the value set to: value="CHANGEME"
       in the template file unless all node files should always have the same value.

    c. When done updating the file with new parameters or changing existing parameters you MUST also update the revision and date
       of the file at the top which can easily be done by running the following command:
       5g_proto/daft/perl/bin/update_revision_information.pl 5g_proto/daft/templates/Network_Config_Template.xml

    d. Check that you don't have any syntax faults in the file by executing the following command:

       xmllint 5g_proto/daft/templates/Network_Config_Template.xml >/dev/null

       If it shows nothing then you are fine, but if it shows any text then you have an error which
       needs to be fixed before continuing.

2. Next you generate new updated node files by executing the following command:

   5g_proto/daft/network_config_files/_update_all_files.bash

   Pay attention to the status messages that is printed in case some node files have parameters that still does not exist in the
   template, because these parameters will not exist in the node files unless you first add them to the template file.

3. Next you manually modify each node file that should have a different “value” than CHANGEME for the new/updated parameters.

4. Next commit and push your changes with commands:

   git commit -am”Updated network config files with new parameters”
   git push

5. Since the “5g_proto/daft/network_config_files/_update_all_files.bash” script will create backup files before doing the changes
   you can always get back these old files from the directory called “5g_proto/daft/network_config_files/old_YYYYMMDD_HHMMSS/”,
   but if you don’t need these files after you have pushed then you should cleanup and remove all these temporary files.
   You can do this by giving the following commands:

   cd 5g_proto/daft
   make clean
