FROM docker.elastic.co/elasticsearch/elasticsearch:9.2.6@sha256:e5673d86bb6a41ed543329ec094fc93d5ef749d32cc87a4150a15d520ad9c670

RUN elasticsearch-plugin install --batch analysis-nori
RUN elasticsearch-plugin install --batch analysis-kuromoji
RUN elasticsearch-plugin install --batch analysis-icu
RUN elasticsearch-plugin install --batch analysis-smartcn
