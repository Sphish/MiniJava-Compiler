import os

items = ["BinaryTree", "BubbleSort", "Factorial", "LinearSearch", "LinkedList", "MoreThan4", "QuickSort", "TreeVisitor"]

def check(item):
        print("-----------------TEST " + item + "--------------------")
        os.system("wget http://compilers.cs.ucla.edu/cs132/project/kg/" + item + ".kg -q")
        os.system("wget http://compilers.cs.ucla.edu/cs132/project/spg/" + item + ".spg -q")
        os.system("javac Main.java")
        os.system("java Main " + item + ".spg > temp.kg")

        os.system("java -jar kgi.jar < temp.kg > res0")
        os.system("java -jar kgi.jar < " + item + ".kg > res1")
        os.system("diff res0 res1")
        
        os.system("rm res0")
        os.system("rm res1")
        os.system("rm " + item + ".spg")
        os.system("rm " + item + ".kg")
        os.system("rm " + "temp.kg")

for i in items:
        check(i)       