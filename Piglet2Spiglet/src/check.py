import os

items = ["BinaryTree", "BubbleSort", "Factorial", "LinearSearch", "LinkedList", "MoreThan4", "QuickSort", "TreeVisitor"]

def check(item):
        print("-----------------TEST " + item + "--------------------")
        os.system("wget http://compilers.cs.ucla.edu/cs132/project/pg/" + item + ".pg -q")
        os.system("wget http://compilers.cs.ucla.edu/cs132/project/spg/" + item + ".spg -q")
        os.system("java Main " + item + ".pg > temp.spg")
        os.system("java -jar spp.jar < temp.spg")

        os.system("java -jar pgi.jar < temp.spg > res0")
        os.system("java -jar pgi.jar < " + item + ".spg > res1")
        os.system("diff res0 res1")
        
        os.system("rm res0")
        os.system("rm res1")
        os.system("rm " + item + ".spg")
        os.system("rm " + item + ".pg")
        os.system("rm " + "temp.spg")

for i in items:
        check(i)       