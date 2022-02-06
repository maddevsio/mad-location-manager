#!/bin/bash
SRC=$1
RES=log1sorted
TMP=log1
cut -c 23- $SRC | sort -k1.2 > $RES
perl -p -i -e "s/,/./g" $RES 
perl -p -i -e "s/\. /, /g" $RES 
