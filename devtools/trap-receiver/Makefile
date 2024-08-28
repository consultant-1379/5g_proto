TOP := ../..
BOB := bob/bob
RULESET := rulesets/ruleset2.0-eric-trap.yaml

package:
	cd $(TOP); $(BOB) -r $(RULESET) clean init prepare package

deploy: package
	cd $(TOP); $(BOB) -r $(RULESET) deploy

undeploy:
	cd $(TOP); $(BOB) -r $(RULESET) undeploy

clean:
	cd $(TOP); $(BOB) -r $(RULESET) clean
