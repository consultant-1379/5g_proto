from utils import printer
import logging


class CmdResult:

    def __init__(self, cmd_name,
                 result, resultToCheck,
                 expectedResult):
        self.cmd_name = cmd_name
        self.result = result
        self.resultToCheck = resultToCheck
        self.expectedResult = expectedResult

    def isCorrect(self):
        if self.expectedResult in self.resultToCheck:
            return True
        else:
            return False
            
    def print(self):
        printer.justprint(self.result)


class RequestResult(CmdResult):

    def __init__(self, cmd_name,
                 result, resultToCheck,
                 expectedResult, verbose):
        CmdResult.__init__(self, cmd_name,
                           result, resultToCheck,
                           expectedResult)
        self.verbose = verbose

    def print(self, withName=False):
        logging.debug(self.result)

        if not logging.getLogger().isEnabledFor(logging.DEBUG) and self.verbose:
            printer.justprint(self.result)

        if self.resultToCheck:
            if withName:
                printer.checkprint(self.cmd_name + ": " + self.resultToCheck,
                                   self.expectedResult)
            else:
                printer.checkprint(self.resultToCheck,
                                   self.expectedResult)
        else:
            if withName:
                printer.error(self.cmd_name + ": " + self.result.headers)
            else:
                printer.error(self.result.headers)