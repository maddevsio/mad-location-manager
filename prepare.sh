#!/bin/bash
SRC=$1
RES=log1sorted
TMP=log1
cut -c 23- $SRC > $TMP
sort -k1.2 $TMP > $RES 
perl -p -i -e "s/,/./g" $RES 
perl -p -i -e "s/\. /, /g" $RES 
rm $TMP
