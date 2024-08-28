import configparser
import os
import logging

class Config5g:
    """
    This class manipulates the configuration file of the 5Gshell.
    Any persistent value should be saved to this file.
    """

    __instance = None
    _conf_file = "shell.cfg"

    @staticmethod
    def getInstance():
        if Config5g.__instance is None:
            Config5g()
        return Config5g.__instance

    def __init__(self):
        if Config5g.__instance is not None:
            raise Exception("This class is a singleton!")
        else:
            Config5g.__instance = self

    def prepare_env(self):
        self._config = configparser.ConfigParser()

        if not os.path.exists(self._conf_file):
            self._config.write(open(self._conf_file, "w+"))

        self._config.read(self._conf_file)

    def get(self, attr, section=None):
        try:
            if section:
                val = self._config.get(section, attr)
            else:
                val = self._config.get('DEFAULT', attr)
            return val
        except configparser.NoOptionError:
            logging.debug("No option " + attr + " found.")
            return None

    def set(self, attr, value, section=None):
        if section:
            self._config.set(section, attr, value)
        else:
            self._config.set('DEFAULT', attr, value)

        self._config.write(open(self._conf_file, "w+"))
