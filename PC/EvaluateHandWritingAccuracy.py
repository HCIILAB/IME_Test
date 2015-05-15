#coding=utf-8
'''
Project Name: EvaluateHandWritingAccuracy

This  tool is used to test the recognition accuracy of Chinese handwriting input methods. 

@author: Liquan Qiu, czqiuliquan@gmail.com
@Created: 2014-11-12
@copyright: 2014~, SCUT HCII-Lab (http://www.hcii-lab.net)
@ modified 
   1.Date: 2015-5-13 by Xuefeng Xiao  , xiaoxuefengchina@gmail.com
		change the delay time t1 to 40ms
		change the char normal size to 300*300

'''

from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice, MonkeyImage
import time
import os
import struct

def readPoints(potFile):
    """
    read the points that the current char contains
    it returns a dict, like [[x1,y1],[x2,y2], ... [xn,yn]]

    @param potFile  
    """
    #we can get the char label here, but we don't use the data
    potFile.read(4)
    (strokeNum,)=struct.unpack("<h",potFile.read(2))
    pointsBuf=[]
    strokeIndex=0
    while (strokeIndex<strokeNum): 
        x = struct.unpack("<h",potFile.read(2))[0]
        y = struct.unpack("<h",potFile.read(2))[0]      
        if (x==-1 and y==0):
            strokeIndex += 1
        pointsBuf.append([x,y])
    x=struct.unpack("<h",potFile.read(2))[0]
    y=struct.unpack("<h",potFile.read(2))[0]
    return pointsBuf


def test(centerX, initY,confirmFirstWordX,confirmFirstWordY,NextWordX,NextWordY, Normal, filePath, deviceList, t1, t2, t3):
    deviceCounts = len(deviceList)
    initX = list(centerX)
    
    #open pot file
    f=open(filePath,"rb")
    fileSize = os.path.getsize(filePath)
    fileSeek = 2
    
    while fileSeek < fileSize:
        sampleLength = struct.unpack("<h",f.read(2))[0]
        pointsBuf = readPoints(f)
        fileSeek += sampleLength
        
        #normalize
        maxX = 0
        maxY = 0
        minX = 32767
        minY = 32767
        for point in pointsBuf:
            if point[0]!=-1:
                if maxX < point[0]:
                    maxX = point[0]
                if maxY < point[1]:
                    maxY = point[1]
                if minX > point[0] and point[0] >= 0:
                    minX = point[0]
                if minY > point[1] and point[1] >= 0:
                    minY = point[1]
        deltaX = maxX - minX
        deltaY = maxY - minY
        if(deltaX > deltaY):
            delta = deltaX
        else:
            delta = deltaY
        halfX = deltaX*Normal/delta/2
        for i in range(deviceCounts):
            initX[i] = centerX[i] - halfX
        
        pointIndex = 0
        pointLength = len(pointsBuf)
        isStrokeStart = True
        while pointIndex<pointLength:
            x = pointsBuf[pointIndex][0]
            y = pointsBuf[pointIndex][1]
            time.sleep(t1)
        
            if(x!=-1):
                x = float((x-minX)*Normal)/delta
                y = float((y-minY)*Normal)/delta
                if(isStrokeStart):
                    for i in range(deviceCounts):
                        deviceList[i].touch(int(initX[i]+x),int(initY[i]+y),MonkeyDevice.DOWN)
                    isStrokeStart=False
                elif( pointsBuf[pointIndex+1][0] != -1 ):
                    for i in range(deviceCounts):
                        deviceList[i].touch(int(initX[i]+x),int(initY[i]+y),MonkeyDevice.MOVE)
                    isStrokeStart=False
                else:
                    for i in range(deviceCounts):
                        deviceList[i].touch(int(initX[i]+x),int(initY[i]+y),MonkeyDevice.MOVE)
                        deviceList[i].touch(int(initX[i]+x),int(initY[i]+y),MonkeyDevice.UP)
                    isStrokeStart=True
            pointIndex+=1
            
        if(not isStrokeStart):#to deal with the special case that one stroke only contain one point
            x=pointsBuf[pointIndex-2][0]
            y=pointsBuf[pointIndex-2][1]
            x=float((x-minX)*Normal)/delta
            y=float((y-minY)*Normal)/delta
            for i in range(deviceCounts):
                deviceList[i].touch(int(initX[i]+x),int(initY[i]+y),MonkeyDevice.UP)
        time.sleep(t2)
        for i in range(deviceCounts):#simulate clicking and choosing the first recognition output
            deviceList[i].touch(confirmFirstWordX[i],confirmFirstWordY[i],MonkeyDevice.DOWN_AND_UP)            
        time.sleep(t3)
        for i in range(deviceCounts):#simulate clicking the button that named NEXT WORD 
            deviceList[i].touch(NextWordX,NextWordY,MonkeyDevice.DOWN_AND_UP)
        time.sleep(t3)
        
        
    f.close()

def main():
    """
		test
		You need to change those params before testing.
	"""
    #origin coordinates
    centerX = [360,300,360]  
    initY = [850,780,720]
	#the coordinates of the first recognition output
    confirmFirstWordX = [50,50,50]
    confirmFirstWordY = [750,660,620]
	#the cooridinates of the button that named NEXT WORD
    NextWordX = 370
    NextWordY = 520
    #normalization
    Normal = 300
    t1 = 0.04 # the interval between two points 
    t2 = 1.2  # the number of seconds to pause when all points of a char have been sent
    t3 = 0.2  # the number of seconds to pause after clicking and choosing the first recognition output
    timeOut = 10
	#the pot file path
    path = 'F:\WorkSpace_eclipse\pyMonkeyTest1\onHCCTestDB-SimpleChar 1.pot'
    filePath = unicode(path,"utf8")
    
    #we can use the command "adb devices" to query. see http://developer.android.com/tools/help/adb.html
    #here, we add three devices
    deviceID = []
    deviceID.append('NVAIR8FQHEONFUUS') 
    deviceID.append('ed9e4d3a') 
    deviceID.append('EIVKB6NJLVA6SGM7') 
 
    deviceList = []
    deviceCounts =len(deviceID)
    for i in range(deviceCounts):
        deviceList.append(MonkeyRunner.waitForConnection(timeOut,deviceID[i]))
	#start testing
    test(centerX, initY,confirmFirstWordX,confirmFirstWordY,NextWordX,NextWordY, Normal, filePath, deviceList, t1, t2, t3)
    
if __name__ == '__main__':
    main()




