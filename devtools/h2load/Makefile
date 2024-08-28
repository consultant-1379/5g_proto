# vim:ts=4:sw=4:sw=4:et
#
# COPYRIGHT ERICSSON GMBH 2018
#
# The copyright to the computer program(s) herein is the property
# of Ericsson GmbH, Germany.
#
# The program(s) may be used and/or copied only with the written
# permission of Ericsson GmbH in accordance with
# the terms and conditions stipulated in the agreement/contract
# under which the program(s) have been supplied.
#
# Created on: Dec 04, 2018
#     Author: eedsvs
#

TOP := ../..
include $(TOP)/CONFIG
include $(TOP)/devtools/CONFIG
include $(TOP)/devenv/CONFIG

IMAGE_FULLNAME := $(PROJ_ID)/$(H2LOAD_IMAGE_NAME):latest
# IMAGE_DEV_FULLNAME := $(PROJ_ID)/$(DEV_USER)/$(H2LOAD_IMAGE_NAME):latest

.PHONY: all clean-all clean undeploy profile manifest

all: install

all-cicd:

# implemented as function on CentOS, use make install-ubuntu for a "real"
# installation on Ubuntu
install: image
	docker push $(IMAGE_FULLNAME)

install-ubuntu: tools compile
	cd nghttp2; sudo make install

uninstall:
	cd nghttp2; sudo make uninstall

image: builddir
	docker build -t $(IMAGE_FULLNAME) .;																\

builddir:
	mkdir -p builddir/devtools/h2load
	mkdir -p builddir/devenv
	cp Makefile builddir/devtools/h2load
	cp ../../CONFIG builddir
	cp ../../devtools/CONFIG builddir/devtools
	cp ../../devenv/CONFIG builddir/devenv

tools:
	@echo "Installing required tools to compile H2LOAD"
	@if lsb_release -a | grep -q Ubuntu; then		\
		sudo apt --yes install g++ make binutils autoconf automake autotools-dev libtool pkg-config zlib1g-dev libcunit1-dev libssl-dev libxml2-dev libev-dev libevent-dev libc-ares-dev; \
	else											\
		echo ">>> Use make install on OSes different from Ubuntu." && false; \
	fi

compile: nghttp2
	cd nghttp2; autoreconf -i
	cd nghttp2; automake
	cd nghttp2; autoconf
	cd nghttp2; ./configure --enable-app
	cd nghttp2; make

nghttp2:
	git clone https://github.com/tatsuhiro-t/nghttp2.git
	cd nghttp2; git submodule update --init

clean-all: uninstall clean

clean:
	rm -rf nghttp2 builddir

profile:
	@echo 											>> $(PROFILE)
	@echo "# h2load"			 					>> $(PROFILE)
	@echo 'function h2load() {  docker run -it --rm h2load:latest $$@; }' >> $(PROFILE)

manifest:

