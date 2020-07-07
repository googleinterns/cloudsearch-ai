mvn compile
export GOOGLE_APPLICATION_CREDENTIALS=/usr/local/google/home/madhuparnab/Downloads/basic-5341ab9846bf.json
mvn exec:java -Dexec.mainClass=com.google.cloudsearch.samples.BasicConnector     -Dexec.args="-Dconfig=sample-config.properties"

