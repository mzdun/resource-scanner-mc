#!/usr/bin/env python3

import os
import sys
import zipfile as z

from typing import List

def copyMember(input: z.ZipFile, output: z.ZipFile, member: z.ZipInfo):
    try:
        output.getinfo(member.filename)
        return
    except KeyError:
        pass
    if member.is_dir():
        output.mkdir(member)
        return
    with input.open(member) as inFile:
        with output.open(member, mode="w") as outFile:
            outFile.write(inFile.read())

def mergeZips(outputFileName: str, *jarFileNames: List[str]):
    with z.ZipFile(outputFileName, 'w', compression=z.ZIP_DEFLATED) as output:
        for inputName in jarFileNames:
            with z.ZipFile(inputName, 'r') as input:
                for member in input.infolist():
                    copyMember(input, output, member)

def __main__():
    libsDirName = sys.argv[1]
    scannerFileName = sys.argv[2]

    os.makedirs(libsDirName, exist_ok=True)

    for pluginFileName in sys.argv[3:]:
        basename = os.path.basename(pluginFileName)
        outputName = os.path.join(libsDirName, basename)
        print(f'Repacking {basename}')
        mergeZips(outputName, pluginFileName, scannerFileName)

if __name__ == "__main__":
    __main__()