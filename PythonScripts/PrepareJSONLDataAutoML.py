#!/usr/bin/env python

from os import listdir
from os.path import isfile, join
import json

path = "pdf_json"
onlyfiles = [f for f in listdir(path) if isfile(join(path, f))]

wordlist = []
drug_file = open("labels/drug.txt", "r")
for line in drug_file.readlines():
    wordlist.append((line[:-1].lower(), "drugs"))
drug_file.close()

disease_file = open("labels/disease.txt", "r")
for line in disease_file.readlines():
    wordlist.append((line[:-1].lower(), "disease"))
disease_file.close()

pathogen_file = open("labels/pathogen.txt", "r")
for line in pathogen_file.readlines():
    wordlist.append((line[:-1].lower(), "pathogen"))
pathogen_file.close()

virus_file = open("labels/virus.txt", "r")
for line in virus_file.readlines():
    wordlist.append((line[:-1].lower(), "virus"))
virus_file.close()

symptoms_file = open("labels/symptoms.txt", "r")
for line in symptoms_file.readlines():
    wordlist.append((line[:-1].lower(), "symptom"))
symptoms_file.close()

labelcount = dict()
store = open("test_data.jsonl", "w+")
i=0
count = 0
for file in onlyfiles:
    i+=1
    if i< 50000:
        continue
    
    file = path + "/" + file
    file = open(file, "r")
    json_file = json.loads(file.read())

    for text in json_file["body_text"]:
        store_labels = []
        for word in wordlist:
            content = text["text"].lower()
            pos = content.find(word[0])
            if pos != -1:            
                store_labels.append((word,pos))
                
        if len(store_labels)  > 0:
            annotation = []
            for label in store_labels:
                if label[0][1] not in labelcount:
                    labelcount[label[0][1]] = 1
                else:
                    labelcount[label[0][1]] += 1
                annotation.append({"text_extraction": {"text_segment": {"end_offset":label[1]+len(label[0][0]), "start_offset":label[1]}}, "display_name": label[0][1]})
            temp = {"text_snippet" :{"content" : content}, "annotations" : annotation}
            count +=1
            json.dump(temp,store)
            store.write("\n")
        break
    if i%50 == 0:
        print(i,count, labelcount) 
    if count >= 7500:
        break




