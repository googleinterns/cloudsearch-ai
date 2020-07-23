#!/usr/bin/env python

# importing the required modules 
import PyPDF2 
import os
from os import listdir
from os.path import isfile, join

#Clip PDFs to size 300kb
def clip_PDF(pdf_file_path): 
    pdfFileObj = open(pdf_file_path, 'rb')
    b = os.path.getsize(pdf_file_path)
    pdfReader = PyPDF2.PdfFileReader(pdfFileObj)
    pages = pdfReader.numPages 
    if pages < 3:
        return
    end = 0
    if b < 300000:
        end = pages
    else:
        end = int((pages)*(300000/b))
    if end == 0:
        return
    start = 0
    pdfWriter = PyPDF2.PdfFileWriter() 
    outputpdf = './new_store/' + pdf_file_path.split('.pdf')[0] + '.pdf'
    for page in range(start,end): 
        pdfWriter.addPage(pdfReader.getPage(page)) 
    with open(outputpdf, "wb") as f: 
        pdfWriter.write(f) 
    pdfFileObj.close()
    b = os.path.getsize(outputpdf)
    if b>500000:
        os.remove(outputpdf)
        
def main(): 
    onlyfiles = [f for f in listdir("./store_https") if isfile(join("./store_https", f))]
    i=0
    for file in onlyfiles:
        i+=1
        if i%200 ==0 :
            print(i + "files clipped.")
        pdf_file_path = "store_https/"+file
        try:
            clip_PDF(pdf_file_path) 
        except:
            print("error")

if __name__ == "__main__": 
    main() 




