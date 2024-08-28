#!/usr/bin/env python
"""This module downloads all of the charts requirements locally."""


def prepareIngressValues(clusterName, namespace):
    return "--set eric-cm-mediator.ingress.hostname=ericcmmediator.{}.{}.seli.gic.ericsson.se".format(clusterName, namespace)
    

def main():
   print(prepareIngressValues("adpci07", "app-staging"))
   

if __name__ == "__main__":
    main()
