#!/usr/bin/env python

import pandas as pd
import requests
import os
from urllib.parse import urljoin
from os import listdir
from os.path import isfile, join
df = pd.read_csv("metadata.csv")

#Don't include documents with non-commercial license
df.loc[df['license'] != "no-cc"]
df.loc[df["pmcid"].notnull()]
df.head()

#Return dictionary consisting of metadata required for document with cord_uid: uid
def getDict(uid):
    x = df.loc[df["cord_uid"] == uid]
    covid  = dict()
    x = x.iloc[0]
    covid["cord_uid"] = x["cord_uid"]
    covid["title"] = x["title"]
    covid["uri"] = "gs://research-papers/" + covid["cord_uid"] + ".pdf"
    covid["url"] = x["url"]
    covid["authors"] = x["authors"]
    covid_df = pd.DataFrame(covid, columns = covid.keys(), index = [0])
    return covid_df


lis= [f for f in listdir("./final") if isfile(join("./final", f))]
frames = []
for i in range(len(lis)):
    frames.append(getDict(lis[i].split(".")[0]))
result = pd.concat(frames)
result.to_csv (r'./covid_metadata.csv', index = False, header=True)




