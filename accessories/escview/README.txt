

ESCVIEW - Bar Chart Viewer for ESC/BSF


How to use the bar chart viewer:
--------------------------------

- Pre-requisites:
	- Running BSF
	- Running BSF monitor POD
	- Address and port of the monitor

- How to configure:
	- Edit Makefile: add address + port of the monitor in the last line using the -Dexec.args parameter such as:
		JAVA_HOME=$(JAVA_HOME) mvn compile exec:java -Dexec.args="10.210.52.30 31788 8"
	- The third parameter (here: 8) determines the number of bar slots that are displayed. A dynamic change of the number of bars is not implemented yet.


- How to start:
	- Invoke make.
	- Note that, first time, a Java 11 is automatically downloaded and installed locally, which takes some time.

