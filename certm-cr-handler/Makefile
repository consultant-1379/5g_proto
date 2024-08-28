ARM_DOCKER_REGISTRY := armdocker.rnd.ericsson.se/proj-5g-bsf
TEST_IMAGE_NAME := certm-cr-handler-test
TEST_IMAGE_VERSION := 0.0.5

build:
	cd .. && mvn -s settings.xml -DskipTests -am -pl :certm-cr-handler install
	
build-test-image:
	mvn -s ../settings.xml install -DskipTests -f docker/pom.xml
	docker build docker/. -t ${ARM_DOCKER_REGISTRY}/$(USER)/${TEST_IMAGE_NAME}:${TEST_IMAGE_VERSION}

package-test-image:
	docker save --output ${TEST_IMAGE_NAME}.tar ${ARM_DOCKER_REGISTRY}/$(USER)/${TEST_IMAGE_NAME}:${TEST_IMAGE_VERSION}
	mv ${TEST_IMAGE_NAME}.tar src/test/resources/

build-release:
	cd ..;bob/bob -r rulesets/ruleset2.0-eric-base.yaml libs:certm-cr-handler-compile

test:
	cd .. && mvn -s settings.xml -Dmaven.test.skip=false -am -pl :certm-cr-handler test

test-annotations:
	cd .. && mvn -s settings.xml -Dmaven.test.skip=false -am -pl :certm-cr-handler test -Dtest=TestAnnotations

test-crs-creation:
	cd .. && mvn -s settings.xml -Dmaven.test.skip=false -am -pl :certm-cr-handler test -Dtest=TestCrsCreation

test-cm-watch:
	cd .. && mvn -s settings.xml -Dmaven.test.skip=false -am -pl :certm-cr-handler test -Dtest=TestCmWatch
