#IME_Test

========

This project can be used to test the recognition accuracy of Chinese handwriting input method. It is composed of two parts: a PC and a mobile client(Android Client).

The test data named SCUT-onHCCTestDB is publicly available for academic research usage.SCUT-onHCCTestDB is an online handwritten Chinese character database. It contains five datasets that are simplified Chinese dataset (denoted as SimpleChar) in GB2312-80 standard, traditional Chinese dataset (denoted as TradChar) in Big5 standard, mixed simplified and traditional Chinese dataset (denoted as SimpTradChar),
rarely-used Chinese character dataset (denoted as RarelyUsedChar), and symbol dataset (denoted as SymbolChar). Each of the above dataset includes 5 subsets (indexed from 1 to 5) respectively. It is worth mentioning that the SymbolChar dataset comprises uppercase letters, lowercase letters, digits, punctuation,common symbols, and so on. 
For more information, please consult the web site at http://www.hcii-lab.net/data/onHCCTestdataset/onHCCTest.html


========

###How to use ?

1. Install the apk to your Android phone and put the labeling files to the folder "<sdcard>/hciiTestAccuracy"
Notice that the labeling files should be encoding by UTF-8
2. Open the Android application, choose one labeling file and click the EditView so as to pop-up input method
3. Edit ./PC/EvaluateHandWritingAccuracy.py.
We are going to change some params before testing. All those params are defined in main function.
Use the command "monkeyrunner <program_filename> " to run the python program.
For more information about Monkeyrunner, please consult the web site at
http://developer.android.com/tools/help/monkeyrunner_concepts.html
