FROM docker.elastic.co/elasticsearch/elasticsearch:9.2.6

RUN elasticsearch-plugin install --batch analysis-nori
RUN elasticsearch-plugin install --batch analysis-kuromoji
RUN elasticsearch-plugin install --batch analysis-icu
RUN elasticsearch-plugin install --batch analysis-smartcn
