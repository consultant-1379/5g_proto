#!/usr/bin/env python3
# coding: utf-8

# # Load Envoy Config and Save it in YAML Format

import json
import yaml

with open('config.json') as json_file:
    data = json.load(json_file)

# Output config is collected in "out":
out = {}
out['static_resources'] = {}
out['static_resources']['clusters'] = []
out['static_resources']['listeners'] = []

# No idea if the order of the Envoy config is always in the same order -> go through all sections and do the right thing per section (bootstrap config, listeners, clusters, routes (ignore routes, they are also in the listener section)
for section in data['configs']:
    if ('bootstrap' in section):
        out['admin'] = section['bootstrap']['admin']
    if ('static_clusters' in section):
        for cluster in section['static_clusters']:
            out['static_resources']['clusters'].append(cluster['cluster'])
    if ('dynamic_active_clusters' in section):
        for cluster in section['dynamic_active_clusters']:
            out['static_resources']['clusters'].append(cluster['cluster'])
    if ('dynamic_active_listeners' in section):
        for listener in section['dynamic_active_listeners']:
            out['static_resources']['listeners'].append(listener['listener'])
            
# Save to file. "default_flow_style=False" saves a multi-line YAML (not everything on one line with {})
yaml.Dumper.ignore_aliases = lambda *args : True

with open('config_static.yaml', 'w') as outfile:
    yaml.dump(out, outfile, default_flow_style=False)

