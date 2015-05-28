#coding=utf-8
'''
Project Name: IME_Test

This  tool is used to test the recognition accuracy of Chinese handwriting input methods. 

@author: Liquan Qiu, czqiuliquan@gmail.com
@Created: 2014-11-12
@copyright: 2014~, SCUT HCII-Lab (http://www.hcii-lab.net)
'''
#Imports the monkeyrunner modules used by this program
from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice, MonkeyImage
import time
import os
import struct

def FromLabelCodeGetChar(LabelCode):
	try:
		back,front=LabelCode.split()   
		SbyteArray=bytearray(2)
		Tbuffer=buffer(SbyteArray,0,2)
		SbyteArray[1]=int(front)
		SbyteArray[0]=int(back)
		return unicode(Tbuffer,"gbk")
	except:
		return "--"
		
def readPoints(potFile):
	"""
	read the points that the current char contains
	it returns a dict, like [[x1,y1],[x2,y2], ... [xn,yn]]

	@param potFile  
	"""
	
	#we can get the char label here, but we don't use the data
	potFile.read(4)
	
	strokeNum=struct.unpack("<h",potFile.read(2))[0]
	pointsBuf=[]
	strokeIndex=0
	
	while strokeIndex<strokeNum: 
		x=struct.unpack("<h",potFile.read(2))[0]
		y=struct.unpack("<h",potFile.read(2))[0]
		if x==-1 and y==0:
			strokeIndex += 1
		pointsBuf.append([x,y])
	x=struct.unpack("<h",potFile.read(2))[0]
	y=struct.unpack("<h",potFile.read(2))[0]
	if x!=-1 or y!=-1:
		raise Exception("error to unpack pot file")
	return pointsBuf
	
def test(centerX, initY, Normal, filePath, deviceList, t1, t2):
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
		minX = 65535
		minY = 65535
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
		#break;
		time.sleep(t2)
	time.sleep(t2)
	# When the test is finished, we simulate drawing a horizontal line,
	# so that the client can correctly handle the last character
	for i in range(deviceCounts):
		deviceList[i].touch(int(centerX[i]-Normal/2),int(initY[i]),MonkeyDevice.DOWN)
		time.sleep(t1)
		deviceList[i].touch(int(centerX[i]+Normal/2),int(initY[i]),MonkeyDevice.MOVE)
		time.sleep(t1)
		deviceList[i].touch(int(centerX[i]+Normal/2),int(initY[i]),MonkeyDevice.UP)
		time.sleep(t1)
	f.close()

def main():
	"""test"""
	#origin coordinates
	centerX = [330,360]
	initY = [865,750]
	#normalization
	Normal = 180
	t1 = 0.06 # the interval between two points 
	t2 = 1.2 # the number of seconds to pause when all points of a char have been sent
	path = r'D:\pot\onHCCTestDB-SimpTradChar 2.pot'
	timeout = 5
	#we can use the command "adb devices" to query. see http://developer.android.com/tools/help/adb.html
	#here, we  add two devices
	deviceID = []
	deviceID.append('EIVKB6NJLVA6SGM7')
	deviceID.append('GMKFTGONPNVOZ98L')
	deviceList = []
	
	filePath = unicode(path,"utf8")
	for i in range(len(deviceID)):
		deviceList.append(MonkeyRunner.waitForConnection(timeout,deviceID[i]))
	test(centerX, initY, Normal, filePath, deviceList, t1, t2)

if __name__ == '__main__':
	main()