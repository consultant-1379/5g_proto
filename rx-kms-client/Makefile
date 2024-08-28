build:
	mvn clean install -DskipTests

build-release:
	cd ..;bob/bob -r rulesets/ruleset2.0-eric-base.yaml libs:rx-kms-client-compile

push-release:
	cd ..;bob/bob -r rulesets/ruleset2.0-eric-base.yaml libs:rx-kms-client-push

push:
	mvn deploy -DskipTests -Dmaven.deploy.skip=false

test-complete:
	mvn surefire:test@InsecureTests surefire:test@SecureTests surefire:test@RobustnessTests surefire:test@StressTests
