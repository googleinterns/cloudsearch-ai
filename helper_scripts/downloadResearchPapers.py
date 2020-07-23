#!/usr/bin/env python

import pandas as pd
import requests
import os
import requests
from urllib.parse import urljoin
from bs4 import BeautifulSoup
import random
import time

df = pd.read_csv("metadata.csv")
#Don't download research papers with non-commercial license.
df.loc[df['license'] != "no-cc"]
df.loc[df["pmcid"].notnull()]

# For https documents
urls = []
for i in range(len(df["url"])) :
    try:
        if "https://www.ncbi.nlm.nih.gov/pmc/articles/" in df["url"][i] :
            urls.append((df["cord_uid"][i],df["url"][i]))
    except:
        ("")
print(len(urls))

count=0
#Download https documents from NCBI
for url in urls:
    count+=1
    if count%100 == 0:
        print(count+" Research Papers Downloaded")
    if count > 4803:
        time.sleep(random.randint(1,2))
    try:
        folder_location = './store_https'
        if os.path.exists("/usr/local/google/home/madhuparnab/Downloads/551982_1230614_bundle_archive/store_https/"+url[0]+".pdf") :
            continue
        if not os.path.exists(folder_location):os.mkdir(folder_location)
        response = requests.get(url[1])
        soup= BeautifulSoup(response.text, "html.parser")     
        for link in soup.select("a[href$='.pdf']"):
            pdf_part = ""
            if link["href"][0:4] == "/pmc":
                pdf_part = link["href"]
            else:
                continue
            l = "https://www.ncbi.nlm.nih.gov" + pdf_part
            print(l)
            filename = os.path.join(folder_location,url[0])
            filename = filename + ".pdf"
            os.system("curl " + l + " --output " + filename);
    except:
        print("error")

#For http Documents
urls = []
for i in range(len(df["url"])) :
    try:
        if  not "https" in df["url"][i] :
            urls.append((df["cord_uid"][i],df["url"][i]))
    except:
        ("")
print(len(urls))

#download http documents from medrxiv
for url in urls:
    if "medrxiv" in url[1]:
        print(url[1])
        folder_location = './store'
        if not os.path.exists(folder_location):os.mkdir(folder_location)
        response = requests.get(url[1])
        print(response)
        soup= BeautifulSoup(response.text, "html.parser")     
        for link in soup.select("a[href$='.pdf']"):
            print(link)
            print(link["href"])
            l = "https://www.medrxiv.org/" + link["href"]
            print(l)
            filename = os.path.join(folder_location,url[0])
            filename = filename + ".pdf"
            with open(filename, 'wb') as f:
                f.write(requests.get(urljoin(l,l)).content)




