import os

items = ["BinaryTree", "BubbleSort", "Factorial", "LinearSearch", "MoreThan4", "QuickSort", "TreeVisitor"]

def check(item):
        print("-----------------TEST " + item + "--------------------")
        os.system("wget http://compilers.cs.ucla.edu/cs132/project/pg/" + item + ".pg")
        os.system("wget http://compilers.cs.ucla.edu/cs132/project/spg/" + item + ".spg")
        os.system("java Main " + item + ".pg > temp.spg")
        

        